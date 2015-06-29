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

import com.sleepcamel.bsoneer.Bsonee;
import com.sleepcamel.bsoneer.processor.generators.AnnotationInfo;
import com.sleepcamel.bsoneer.processor.generators.BsoneeBsonGenerator;
import com.sleepcamel.bsoneer.processor.generators.BsoneeCodecGenerator;
import com.sleepcamel.bsoneer.processor.generators.BsoneeCodecProviderGenerator;
import com.sleepcamel.bsoneer.processor.generators.BsoneeCodecRegistryGenerator;
import com.sleepcamel.bsoneer.processor.util.Util;
import com.squareup.javapoet.JavaFile;

@SupportedAnnotationTypes({ "com.sleepcamel.bsoneer.Bsonee" })
public class BsonProcessor extends AbstractProcessor {

	public static final String IT_IS_NOT_A_CLASS = "It IS NOT a class";
	public static final String CANNOT_GENERATE_CODE_FOR = "Cannot generate code for ";
	public static final String NO_DEFAULT_CONSTRUCTOR = "Class does not have a default constructor or it is private";
	public static final String CANNOT_USE_ID_PROPERTY_AND_ID_GENERATOR_AT_THE_SAME_TIME = "Cannot use idProperty and idGenerator at the same time";
	public static final String ID_PROPERTY_NOT_FOUND = "IdProperty not found";

	SimpleElementVisitor6<Boolean, Void> noArgsConstructorVisitor = new SimpleElementVisitor6<Boolean, Void>() {
		public Boolean visitExecutable(ExecutableElement t, Void p) {
			return t.getParameters().isEmpty() && !t.getModifiers().contains(Modifier.PRIVATE);
		}
	};

	private Set<AnnotationInfo> generated = new LinkedHashSet<AnnotationInfo>();

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		for (AnnotationInfo c : findBsoneedClassNames(roundEnv)) {
			TypeElement type = processingEnv.getElementUtils().getTypeElement(c.typeAsString());
			try {
				createBsoneeCodecClass(type, c).writeTo(processingEnv.getFiler());
				generated.add(c);
			} catch (IOException e) {
				error("Code gen failed: " + e, type);
			}
		}
		if (roundEnv.processingOver() && !generated.isEmpty()) {
			try {
				createBsoneeCodecProviderClass().writeTo(processingEnv.getFiler());
				createBsoneeCodecRegistryClass().writeTo(processingEnv.getFiler());
				createBsoneeClass().writeTo(processingEnv.getFiler());
			} catch (IOException e) {
				error("Code gen failed: " + e, null);
			}
		}
		return !generated.isEmpty();
	}

	private Set<AnnotationInfo> findBsoneedClassNames(RoundEnvironment env) {
		Set<AnnotationInfo> toGenerate = new LinkedHashSet<AnnotationInfo>();
		for (Element element : env.getElementsAnnotatedWith(Bsonee.class)) {
			Map<String, Object> annotation = Util.getAnnotation(Bsonee.class,
					element);
			
			Object object = annotation.get("value");
			TypeMirror tm = null;
			if (object instanceof Class) {
				if (!element.getKind().equals(ElementKind.CLASS)) {
					String clazzName = Util.rawTypeToString(element.asType(), '.');
					error(CANNOT_GENERATE_CODE_FOR + "'" + clazzName + "'. "
							+ IT_IS_NOT_A_CLASS, element);
				} else {
					tm = element.asType();
				}
			} else if (object instanceof TypeMirror) {
				tm = (TypeMirror) object;
			}
			if (tm != null) {
				Object idGenerator = annotation.get("idGenerator");
				TypeMirror idGeneratorType = null;
				boolean customGenerator = false;
				if ( idGenerator instanceof Class ){
					// Default id generator
					idGeneratorType = processingEnv.getElementUtils().getTypeElement(((Class<?>) idGenerator).getCanonicalName()).asType();
				} else if (idGenerator instanceof TypeMirror){
					customGenerator = true;
					// Custom id generator
					idGeneratorType = (TypeMirror) idGenerator;
				} else {
					error("[Bsonee Internal Error] Unknown class for idGenerator: "+idGenerator.getClass(), element);
					continue;
				}
				AnnotationInfo annotationInfo = new AnnotationInfo(tm, (String)annotation.get("id"), (Boolean)annotation.get("keepIdProperty"), idGeneratorType, customGenerator);
				if ( annotationInfo.hasCustomId() && annotationInfo.hasCustomGenerator() ){
					String clazzName = Util.rawTypeToString(tm, '.');
					error(CANNOT_GENERATE_CODE_FOR + "'" + clazzName + "'. "
							+ CANNOT_USE_ID_PROPERTY_AND_ID_GENERATOR_AT_THE_SAME_TIME, element);
					continue;
				}
				if (!addTypeAndSuperTypes(toGenerate, annotationInfo)) {
					String clazzName = Util.rawTypeToString(tm, '.');
					error(CANNOT_GENERATE_CODE_FOR + "'" + clazzName + "'. "
							+ NO_DEFAULT_CONSTRUCTOR, element);
					continue;
				}
			}
		}
		for (AnnotationInfo c : toGenerate) {
			if (generated.contains(c)) {
				note("Already generated Bson type for class '" + c
						+ "', skipping...");
			} else {
				toGenerate.add(c);
			}
		}
		return toGenerate;
	}
	
	/*
	 * Object[] values = (Object[]) annotation.get("value");
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
	 */

	private boolean addTypeAndSuperTypes(Set<AnnotationInfo> toGenerate, AnnotationInfo annotationInfo) {
//		ClassName superType = Util.getSuperType(tm, processingEnv);
//		if (superType != null) {
//			Elements elementUtils = processingEnv.getElementUtils();
//			TypeElement typeElement = elementUtils
//					.getTypeElement(superType.toString());
//			hasDefaultConstructor &= addTypeAndSuperTypes(toGenerate, typeElement.asType());
//		}
		TypeMirror erasure = processingEnv.getTypeUtils().erasure(annotationInfo.getType());
		TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(erasure.toString());
		List<ExecutableElement> constructorsIn = ElementFilter.constructorsIn(typeElement.getEnclosedElements());
		boolean hasDefaultConstructor = constructorsIn.isEmpty();
		for (ExecutableElement constructor : constructorsIn) {
			hasDefaultConstructor |= (noArgsConstructorVisitor.visit(constructor));
		}
		if (hasDefaultConstructor) {
			toGenerate.add(annotationInfo);
		}
		return hasDefaultConstructor;
	}

	private JavaFile createBsoneeCodecClass(TypeElement type, AnnotationInfo ai) {
		note("Generating Codec...", type);
		return new BsoneeCodecGenerator(type, ai, processingEnv).getJavaFile();
	}

	private JavaFile createBsoneeCodecProviderClass() {
		note("Generating Codec Provider");
		return new BsoneeCodecProviderGenerator(generated, processingEnv).getJavaFile();
	}

	private JavaFile createBsoneeCodecRegistryClass() {
		note("Generating Codec Registry");
		return new BsoneeCodecRegistryGenerator(generated, processingEnv).getJavaFile();
	}
	
	private JavaFile createBsoneeClass() {
		note("Generating BsoneeBson");
		return new BsoneeBsonGenerator(generated, processingEnv).getJavaFile();
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
	
	private void note(String msg, Element element) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg, element);
	}
	
	private void note(String msg) {
		note(msg,null);
	}

}
