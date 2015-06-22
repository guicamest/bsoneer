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
package com.sleepcamel.bsoneer.processor;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;

import com.sleepcamel.bsoneer.Bsonee;
import com.sleepcamel.bsoneer.processor.generators.BsoneeCodecGenerator;
import com.sleepcamel.bsoneer.processor.generators.BsoneeCodecProviderGenerator;
import com.sleepcamel.bsoneer.processor.generators.BsoneeCodecRegistryGenerator;
import com.sleepcamel.bsoneer.processor.generators.BsoneeGenerator;
import com.sleepcamel.bsoneer.processor.util.Util;
import com.squareup.javapoet.JavaFile;

@SupportedAnnotationTypes({ "com.sleepcamel.bsoneer.Bsonee" })
public class BsonProcessor extends AbstractProcessor {

	public static final String IT_IS_NOT_A_CLASS = "It IS NOT a class";
	public static final String CANNOT_GENERATE_CODE_FOR = "Cannot generate code for ";
	public static final String NO_DEFAULT_CONSTRUCTOR = "Class does not have a default constructor or it is private";

	SimpleElementVisitor6<Boolean, Void> noArgsConstructorVisitor = new SimpleElementVisitor6<Boolean, Void>() {
		public Boolean visitExecutable(ExecutableElement t, Void p) {
			return t.getParameters().isEmpty() && !t.getModifiers().contains(Modifier.PRIVATE);
		}
	};

	private Set<String> generated = new LinkedHashSet<String>();

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		for (String c : findBsoneedClassNames(roundEnv)) {
			TypeElement type = processingEnv.getElementUtils().getTypeElement(c);
			try {
//				createBsoneeClass(type).writeTo(processingEnv.getFiler());
				createBsoneeCodecClass(type).writeTo(processingEnv.getFiler());
				generated.add(c);
			} catch (IOException e) {
				error("Code gen failed: " + e, type);
			}
		}
		if (roundEnv.processingOver() && !generated.isEmpty()) {
			try {
				createBsoneeCodecProviderClass().writeTo(processingEnv.getFiler());
				createBsoneeCodecRegistryClass().writeTo(processingEnv.getFiler());
			} catch (IOException e) {
				error("Code gen failed: " + e, null);
			}
		}
		return !generated.isEmpty();
	}

	private Set<String> findBsoneedClassNames(RoundEnvironment env) {
		Set<String> toGenerate = new LinkedHashSet<String>();
		for (Element element : env.getElementsAnnotatedWith(Bsonee.class)) {
			Map<String, Object> annotation = Util.getAnnotation(Bsonee.class,
					element);
			Object[] values = (Object[]) annotation.get("value");
			if (values.length != 0) {
				for (Object o : values) {
					TypeMirror tm = (TypeMirror) o;
					Element asElement = processingEnv.getTypeUtils().asElement(tm);
					if (!asElement.getKind().equals(ElementKind.CLASS)) {
						error(CANNOT_GENERATE_CODE_FOR + "'"
								+ tm.toString() + "'. " + IT_IS_NOT_A_CLASS
								, element);
						continue;
					}
					if (!addTypeAndSuperTypes(toGenerate, tm)) {
						error(CANNOT_GENERATE_CODE_FOR + "'" + tm.toString() + "'. "
								+ NO_DEFAULT_CONSTRUCTOR, element);
						continue;
					}
				}
			} else {
				String clazzName = Util.rawTypeToString(element.asType(), '.');
				if (!element.getKind().equals(ElementKind.CLASS)) {
					error(CANNOT_GENERATE_CODE_FOR + "'" + clazzName + "'. "
							+ IT_IS_NOT_A_CLASS, element);
					continue;
				}
				if (!addTypeAndSuperTypes(toGenerate, element.asType())) {
					error(CANNOT_GENERATE_CODE_FOR + "'" + clazzName + "'. "
							+ NO_DEFAULT_CONSTRUCTOR, element);
					continue;
				}
			}
		}
		for (String c : toGenerate) {
			if (generated.contains(c)) {
				processingEnv.getMessager().printMessage(Kind.NOTE,
					"Already generated Bson type for class '" + c
					+ "', skipping...");
			} else {
				toGenerate.add(c);
			}
		}
		return toGenerate;
	}

	private boolean addTypeAndSuperTypes(Set<String> toGenerate, TypeMirror tm) {
//		ClassName superType = Util.getSuperType(tm, processingEnv);
//		if (superType != null) {
//			Elements elementUtils = processingEnv.getElementUtils();
//			TypeElement typeElement = elementUtils
//					.getTypeElement(superType.toString());
//			hasDefaultConstructor &= addTypeAndSuperTypes(toGenerate, typeElement.asType());
//		}
		TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(tm.toString());
		List<ExecutableElement> constructorsIn = ElementFilter.constructorsIn(typeElement.getEnclosedElements());
		boolean hasDefaultConstructor = constructorsIn.isEmpty();
		for (ExecutableElement constructor : constructorsIn) {
			hasDefaultConstructor |= (noArgsConstructorVisitor.visit(constructor));
		}
		if (hasDefaultConstructor) {
			toGenerate.add(Util.rawTypeToString(tm, '.'));
		}
		return hasDefaultConstructor;
	}

	private JavaFile createBsoneeClass(TypeElement type) {
		processingEnv.getMessager().printMessage(Kind.NOTE,
				"Generating Bson type for class '" + type + "'");

		return new BsoneeGenerator(type, processingEnv).getJavaFile();
	}

	private JavaFile createBsoneeCodecClass(TypeElement type) {
		processingEnv.getMessager().printMessage(Kind.NOTE,
				"Generating Codec...", type);

		return new BsoneeCodecGenerator(type, processingEnv).getJavaFile();
	}

	private JavaFile createBsoneeCodecProviderClass() {
		processingEnv.getMessager().printMessage(Kind.NOTE,
				"Generating Codec Provider");

		return new BsoneeCodecProviderGenerator(generated, processingEnv).getJavaFile();
	}

	private JavaFile createBsoneeCodecRegistryClass() {
		processingEnv.getMessager().printMessage(Kind.NOTE,
				"Generating Codec Registry");

		return new BsoneeCodecRegistryGenerator(generated, processingEnv).getJavaFile();
	}

	static String elementToString(Element element) {
		switch (element.getKind()) {
		case FIELD:
			// fall through
		case CONSTRUCTOR:
			// fall through
		case METHOD:
			return element.getEnclosingElement() + "." + element;
		default:
			return element.toString();
		}
	}

	private void error(String msg, Element element) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg,
				element);
	}

}
