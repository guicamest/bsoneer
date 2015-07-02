/*
 * Copyright (C) 2015 Sleepcamel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sleepcamel.bsoneer.processor.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.SimpleElementVisitor6;

import com.google.common.base.Joiner;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

public class Util {

	private static final String JAVA_LANG_ENUM = "java.lang.Enum<?>";

	private Util() {
	}

	public static PackageElement getPackage(Element type) {
		while (type.getKind() != ElementKind.PACKAGE) {
			type = type.getEnclosingElement();
		}
		return (PackageElement) type;
	}

	/**
	 * Returns a string for the raw type of {@code type}. Primitive types are
	 * always boxed.
	 */
	public static String rawTypeToString(TypeMirror type,
			char innerClassSeparator) {
		if (!(type instanceof DeclaredType)) {
			throw new IllegalArgumentException("Unexpected type: " + type);
		}
		StringBuilder result = new StringBuilder();
		DeclaredType declaredType = (DeclaredType) type;
		rawTypeToString(result, (TypeElement) declaredType.asElement(),
				innerClassSeparator);
		return result.toString();
	}

	static void rawTypeToString(StringBuilder result, TypeElement type,
			char innerClassSeparator) {
		String packageName = getPackage(type).getQualifiedName().toString();
		String qualifiedName = type.getQualifiedName().toString();
		if (packageName.isEmpty()) {
			result.append(qualifiedName.replace('.', innerClassSeparator));
		} else {
			result.append(packageName);
			result.append('.');
			result.append(qualifiedName.substring(packageName.length() + 1)
					.replace('.', innerClassSeparator));
		}
	}

	/**
	 * Returns the annotation on {@code element} formatted as a Map. This
	 * returns a Map rather than an instance of the annotation interface to
	 * work-around the fact that Class and Class[] fields won't work at code
	 * generation time. See
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5089128
	 */
	public static Map<String, Object> getAnnotation(Class<?> annotationType,
			Element element) {
		for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
			if (!rawTypeToString(annotation.getAnnotationType(), '$').equals(
					annotationType.getName())) {
				continue;
			}

			return parseAnnotationMirror(annotationType, annotation);
		}
		return null; // Annotation not found.
	}

	private static Map<String, Object> parseAnnotationMirror(Class<?> annClass, AnnotationMirror annotation) {
		if (annClass == null) {
			// Do our best...
			try {
				annClass = Class.forName(annotation.getAnnotationType().toString());
			} catch (ClassNotFoundException e1) {
			}
		}
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		for (Method m : annClass.getMethods()) {
			result.put(m.getName(), m.getDefaultValue());
		}
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : annotation
				.getElementValues().entrySet()) {
			String name = e.getKey().getSimpleName().toString();
			Object value = e.getValue().accept(VALUE_EXTRACTOR, null);
			Object defaultValue = result.get(name);
			if (!lenientIsInstance(defaultValue.getClass(), value)) {
//				throw new IllegalStateException(
//						String.format(
//								"Value of %s.%s is a %s but expected a %s\n    value: %s",
//								annClass,
//								name,
//								value.getClass().getName(),
//								defaultValue.getClass().getName(),
//								value instanceof Object[] ? Arrays
//										.toString((Object[]) value) : value));
			}
			result.put(name, value);
		}
		return result;
	}

	private static final AnnotationValueVisitor<Object, Void> VALUE_EXTRACTOR =
			new SimpleAnnotationValueVisitor6<Object, Void>() {
			@Override
			public Object visitString(String s, Void p) {
				if ("<error>".equals(s)) {
					throw new RuntimeException("Unknown type returned as <error>.");
				} else if ("<any>".equals(s)) {
					throw new RuntimeException("Unknown type returned as <any>.");
				}
				return s;
			}

			public Object visitAnnotation(AnnotationMirror a, Void p) {
				return parseAnnotationMirror(null, a);
			};

			@Override
			public Object visitType(TypeMirror t, Void p) {
				return t;
			}

			@Override
			protected Object defaultAction(Object o, Void v) {
				return o;
			}

			@Override
			public Object visitArray(List<? extends AnnotationValue> values, Void v) {
				Object[] result = new Object[values.size()];
				for (int i = 0; i < values.size(); i++) {
					result[i] = values.get(i).accept(this, null);
				}
				return result;
			}
	};

	/**
	 * Returns true if {@code value} can be assigned to {@code expectedClass}.
	 * Like {@link Class#isInstance} but more lenient for {@code Class<?>}
	 * values.
	 */
	private static boolean lenientIsInstance(Class<?> expectedClass,
			Object value) {
		if (expectedClass.isArray()) {
			Class<?> componentType = expectedClass.getComponentType();
			if (!(value instanceof Object[])) {
				return false;
			}
			for (Object element : (Object[]) value) {
				if (!lenientIsInstance(componentType, element))
					return false;
			}
			return true;
		} else if (expectedClass == Class.class) {
			return value instanceof TypeMirror;
		} else {
			return expectedClass == value.getClass();
		}
	}

	public static ClassName bsoneeName(ClassName type,
			String suffix) {
		return ClassName.get(type.packageName(),
		        Joiner.on('$').join(type.simpleNames()) + suffix);
	}

	public static boolean isEnum(TypeMirror key) {
		if (key instanceof DeclaredType) {
			DeclaredType type = (DeclaredType) key;
			Element element = type.asElement();
			if (element instanceof TypeElement) {
				TypeElement typeElement = (TypeElement) element;
				TypeMirror superclass = typeElement.getSuperclass();
				if (superclass instanceof DeclaredType) {
					DeclaredType superclassDeclaredType = (DeclaredType) superclass;
					if (JAVA_LANG_ENUM.equals(Util.getCanonicalTypeName(superclassDeclaredType))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static String getCanonicalTypeName(DeclaredType declaredType) {
		List<? extends TypeMirror> typeArguments = declaredType
				.getTypeArguments();
		if (!typeArguments.isEmpty()) {
			StringBuilder typeString = new StringBuilder(declaredType
					.asElement().toString());
			typeString.append('<');
			for (int i = 0; i < typeArguments.size(); i++) {
				if (i > 0) {
					typeString.append(',');
				}
				typeString.append('?');
			}
			typeString.append('>');

			return typeString.toString();
		} else {
			return declaredType.toString();
		}
	}

	public static ClassName getSuperType(TypeElement type, ProcessingEnvironment processingEnv) {
		TypeMirror superclass = type.getSuperclass();
		CharSequence scName = Util.rawTypeToString(superclass, '.');
		if (superclass.getKind() != TypeKind.NONE
				&& !Object.class.getCanonicalName().equals(scName)) {
			return ClassName.get(processingEnv.getElementUtils().getTypeElement(scName));
		}
		return null;
	}

	public static ClassName getSuperType(TypeMirror type, ProcessingEnvironment processingEnv) {
		Elements utils = processingEnv.getElementUtils();
		return getSuperType(utils.getTypeElement(Util.rawTypeToString(type, '.')), processingEnv);
	}

	public static ParameterSpec bsonReaderParameter() {
		return ParameterSpec.builder(bsonReaderTypeName(), "reader").build();
	}

	public static ParameterSpec bsonWriterParameter() {
		return ParameterSpec.builder(bsonWriterTypeName(), "writer").build();
	}

	public static ParameterSpec bsonDecoderContextParameter() {
		return ParameterSpec.builder(bsonDecoderContextTypeName(), "decoderContext").build();
	}

	public static ParameterSpec bsonEncoderContextParameter() {
		return ParameterSpec.builder(bsonEncoderContextTypeName(), "encoderContext").build();
	}

	public static ParameterSpec bsonCodecParameter() {
		return ParameterSpec.builder(bsonCodecTypeName(), "codec").build();
	}

	public static ParameterSpec bsonRegistryParameter() {
		return ParameterSpec.builder(bsonRegistryTypeName(), "registry", Modifier.FINAL).build();
	}

	public static TypeName bsonReaderTypeName() {
		return ClassName.get("org.bson", "BsonReader");
	}

	public static TypeName bsonWriterTypeName() {
		return ClassName.get("org.bson", "BsonWriter");
	}

	public static TypeName bsonDecoderContextTypeName() {
		return ClassName.get("org.bson.codecs", "DecoderContext");
	}

	public static TypeName bsonEncoderContextTypeName() {
		return ClassName.get("org.bson.codecs", "EncoderContext");
	}

	public static TypeName bsonCodecTypeName() {
		return ClassName.get("org.bson.codecs", "Codec");
	}

	public static TypeName bsonRegistryTypeName() {
		return ClassName.get("org.bson.codecs.configuration", "CodecRegistry");
	}

	public static TypeName bsonCodecProviderTypeName() {
		return ClassName.get("org.bson.codecs.configuration", "CodecProvider");
	}

	public static TypeName bsonTypeName() {
		return ClassName.get("org.bson.conversions", "Bson");
	}

	public static TypeName bsonTypeTypeName() {
		return ClassName.get("org.bson", "BsonType");
	}

	public static AnnotationSpec suppressWarningsAnnotation() {
		return suppressWarningsAnnotation(true, true);
	}

	public static AnnotationSpec suppressWarningsAnnotation(boolean includeUnchecked, boolean includeRawtypes) {
		List<String> vals = new ArrayList<String>();
		if (includeUnchecked) {
			vals.add("\"unchecked\"");
		}
		if (includeRawtypes) {
			vals.add("\"rawtypes\"");
		}
		return AnnotationSpec.builder(SuppressWarnings.class)
			.addMember("value", "{" + Joiner.on(", ").join(vals) + "}")
			.build();
	}

	public static boolean hasDefaultConstructor(TypeMirror idGeneratorType) {
		if (idGeneratorType.getKind() != TypeKind.DECLARED) {
			return false;
		}

		DeclaredType dt = (DeclaredType) idGeneratorType;
		Element asElement = dt.asElement();

		List<ExecutableElement> constructorsIn = ElementFilter.constructorsIn(asElement.getEnclosedElements());
		boolean hasDefaultConstructor = constructorsIn.isEmpty();
		SimpleElementVisitor6<Boolean, Void> noArgsConstructorVisitor = new SimpleElementVisitor6<Boolean, Void>() {
			public Boolean visitExecutable(ExecutableElement t, Void p) {
				return t.getParameters().isEmpty() && t.getModifiers().contains(Modifier.PUBLIC);
			}
		};
		for (ExecutableElement constructor : constructorsIn) {
			hasDefaultConstructor |= (noArgsConstructorVisitor.visit(constructor));
		}
		return hasDefaultConstructor;
	}

	public static List<? extends TypeMirror> getSuperTypes(TypeElement type,
			ProcessingEnvironment processingEnv) {
		List<? extends TypeMirror> directSupertypes = processingEnv.getTypeUtils().directSupertypes(type.asType());
		if (directSupertypes.size() == 1 && directSupertypes.get(0).toString().equals(Object.class.getCanonicalName())) {
			return Collections.emptyList();
		}
		return directSupertypes;
	}

}
