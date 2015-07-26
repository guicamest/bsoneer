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
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

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
import javax.lang.model.util.Types;

import com.google.common.base.Joiner;
import com.sleepcamel.bsoneer.processor.domain.UtilsProvider;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;

public class Util {

	private static final String JAVA_LANG_ENUM = "java.lang.Enum<?>";

	private static Map<String, TypeElement> collectionMappings = new HashMap<String, TypeElement>();
	
	static private void addCollectionMapping(Class<?> a, Class<?> b) {
		addCollectionMapping(a.getCanonicalName(), b.getCanonicalName());
	}

	static private void addCollectionMapping(String aCanonicalName, String bCanonicalName) {
		Elements elementUtils = UtilsProvider.getElements();
		collectionMappings.put(UtilsProvider.getTypes().erasure(elementUtils.getTypeElement(aCanonicalName).asType()).toString(),
				elementUtils.getTypeElement(bCanonicalName));
	}
	
	static{
		addCollectionMapping(BlockingDeque.class, LinkedBlockingDeque.class);
		addCollectionMapping(BlockingQueue.class, LinkedBlockingDeque.class);
		addCollectionMapping(Deque.class, LinkedBlockingDeque.class);
		addCollectionMapping(Queue.class, LinkedBlockingDeque.class);
		addCollectionMapping(Collection.class, ArrayList.class);
		addCollectionMapping(List.class, ArrayList.class);
		addCollectionMapping(Set.class, LinkedHashSet.class);
		addCollectionMapping(SortedSet.class, TreeSet.class);
		addCollectionMapping(NavigableSet.class, TreeSet.class);
		addCollectionMapping("java.util.concurrent.TransferQueue", "java.util.concurrent.LinkedTransferQueue");
	}
	
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

	public static boolean isJavaCollection(TypeMirror key) {
		if (key.getKind() != TypeKind.DECLARED) {
			return false;
		}
		Types typeUtils = UtilsProvider.getTypes();
		Elements elementUtils = UtilsProvider.getElements();
		TypeMirror erasuredType = typeUtils.erasure(key);
		return elementUtils.getPackageElement("java.util")
				.equals(elementUtils.getPackageOf(typeUtils.asElement(erasuredType)))
				&& typeUtils.isAssignable(erasuredType,
				typeUtils.erasure(elementUtils.getTypeElement(Collection.class.getCanonicalName()).asType()));
	}

	public static TypeMirror collectionTypeArgument(TypeMirror tm) {
		if ( tm instanceof DeclaredType ){
			return ((DeclaredType) tm).getTypeArguments().get(0);
		}
		return tm;
	}

	public static TypeMirror getJavaCollectionClass(TypeMirror typeMirror,
			boolean replaceInterfaceForImplementation, boolean collectionTypeArgumentIsVar) {
		if (!isJavaCollection(typeMirror)) {
			throw new RuntimeException("Type " + typeMirror + " is not a java collection");
		}
		Types typeUtils = UtilsProvider.getTypes();
		Elements elementUtils = UtilsProvider.getElements();
		
		DeclaredType declared = (DeclaredType) typeMirror;
		TypeMirror dt = collectionTypeArgument(declared);
		TypeMirror erasured = typeUtils.erasure(typeMirror);
		TypeElement typeElem = null;
		if (ElementKind.INTERFACE.equals(declared.asElement().getKind()) && replaceInterfaceForImplementation) {
			typeElem = collectionMappings.get(erasured.toString());
		} else {
			typeElem = elementUtils.getTypeElement(erasured.toString());
		}
		if (collectionTypeArgumentIsVar || dt.toString().equals("java.lang.Object") ) {
			return typeUtils.erasure(typeElem.asType());
		}
		return typeUtils.getDeclaredType(typeElem, dt);
	}

	public static boolean isJavaMap(TypeMirror tm) {
		Elements elements = UtilsProvider.getElements();
		TypeElement typeElement = elements.getTypeElement(Map.class.getCanonicalName());
		
		Types types = UtilsProvider.getTypes();
		return types.isAssignable(types.erasure(tm), types.erasure(typeElement.asType()));
	}
}
