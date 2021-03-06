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
package com.sleepcamel.bsoneer.processor.generators;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

import java.util.Set;

import javax.annotation.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import com.sleepcamel.bsoneer.processor.BsonProcessor;
import com.sleepcamel.bsoneer.processor.codeprovider.CodeUtil;
import com.sleepcamel.bsoneer.processor.util.ProcessorJavadocs;
import com.sleepcamel.bsoneer.processor.util.Util;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

public class BsoneeBsonGenerator {
	private final String basePackage;

	public BsoneeBsonGenerator(Set<AnnotationInfo> generated, ProcessingEnvironment processingEnv) {
		String basePackage = null;
		for (AnnotationInfo generatedClass : generated) {
			ClassName entityClassName = ClassName.bestGuess(generatedClass.typeAsString());
			if (basePackage == null || basePackage.length() > entityClassName.packageName().length()) {
				basePackage = entityClassName.packageName();
			}
		}
		this.basePackage = basePackage;
	}

	public JavaFile getJavaFile() {
		ClassName bsoneerCodecProviderClassName = ClassName.get(basePackage, "BsoneeCodecProvider");
		ClassName bsonDocumentClassName = ClassName.bestGuess("org.bson.BsonDocument");

		ClassName bsoneerBsonName = ClassName.get(basePackage, "BsoneeBson");
		TypeVariableName typeVariable = TypeVariableName.get("T");

		TypeSpec.Builder bsoneeBuilder = TypeSpec.classBuilder(bsoneerBsonName.simpleName())
		        .addJavadoc(ProcessorJavadocs.GENERATED_BY_BSONEER)
		        .addSuperinterface(CodeUtil.bsonTypeName())
		        .addTypeVariable(typeVariable)
		        .addModifiers(PUBLIC, FINAL)
		        .addAnnotation(AnnotationSpec.builder(Generated.class)
		        		.addMember("value", "$S", BsonProcessor.class.getCanonicalName())
		        		.build());

		bsoneeBuilder.addField(typeVariable, "wrapped", Modifier.PRIVATE, Modifier.FINAL);
		TypeName clazzName = ParameterizedTypeName.get(ClassName.get(Class.class), typeVariable);
		bsoneeBuilder.addField(clazzName, "wrappedClazz", Modifier.PRIVATE, Modifier.FINAL);
		bsoneeBuilder.addField(FieldSpec.builder(bsoneerCodecProviderClassName, "bcp",
				Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
				.initializer("new $T()", bsoneerCodecProviderClassName)
				.build());

		bsoneeBuilder.addMethod(MethodSpec.constructorBuilder()
				.addAnnotation(Util.suppressWarningsAnnotation(true, false))
				.addModifiers(Modifier.PRIVATE)
				.addParameter(typeVariable, "wrapped", Modifier.FINAL)
				.addStatement("this.wrapped = wrapped")
				.addStatement("this.wrappedClazz = ($T)wrapped.getClass()",
						clazzName)
				.build());

		TypeVariableName cTypeName = TypeVariableName.get("C");
		ParameterizedTypeName paramTypeName = ParameterizedTypeName.get(
				ClassName.get(Class.class), cTypeName);
		Builder toBsonDocumentMethod = MethodSpec.methodBuilder("toBsonDocument")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Override.class)
				.returns(bsonDocumentClassName)
				.addTypeVariable(cTypeName)
				.addParameter(paramTypeName, "documentClass", Modifier.FINAL)
				.addParameter(CodeUtil.bsonRegistryParameter());
		addBodyToMethod(toBsonDocumentMethod);
		bsoneeBuilder.addMethod(toBsonDocumentMethod.build());

		TypeName bsoneeParameterized = ParameterizedTypeName.get(
				bsoneerBsonName, cTypeName);

		Builder bsonMethodBuilder = MethodSpec.methodBuilder("bson")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.returns(CodeUtil.bsonTypeName())
				.addTypeVariable(cTypeName)
				.addParameter(cTypeName, "entity", Modifier.FINAL)
				.addStatement("return new $T(entity)", bsoneeParameterized);
		bsoneeBuilder.addMethod(bsonMethodBuilder.build());

	    return JavaFile.builder(bsoneerBsonName.packageName(), bsoneeBuilder.build())
	            .addFileComment(ProcessorJavadocs.GENERATED_BY_BSONEER)
	            .build();
	}

	private void addBodyToMethod(Builder toBsonDocumentMethod) {
		toBsonDocumentMethod.addStatement("$T<T> codec = registry.get(wrappedClazz)",
				CodeUtil.bsonCodecTypeName());
		toBsonDocumentMethod.beginControlFlow("if (codec == null)");
		toBsonDocumentMethod.addStatement("codec = bcp.get(wrappedClazz, registry)");
		toBsonDocumentMethod.endControlFlow();
		toBsonDocumentMethod.addStatement("return new $T(wrapped, codec)",
				ClassName.bestGuess("org.bson.RawBsonDocument"));
	}
}
