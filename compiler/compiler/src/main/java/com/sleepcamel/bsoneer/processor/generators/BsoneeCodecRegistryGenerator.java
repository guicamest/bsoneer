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
import static javax.lang.model.element.Modifier.STATIC;

import java.util.Set;

import javax.annotation.Generated;
import javax.annotation.processing.ProcessingEnvironment;

import com.sleepcamel.bsoneer.processor.BsonProcessor;
import com.sleepcamel.bsoneer.processor.codeprovider.CodeUtil;
import com.sleepcamel.bsoneer.processor.util.ProcessorJavadocs;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

public class BsoneeCodecRegistryGenerator {
	private final String basePackage;

	public BsoneeCodecRegistryGenerator(Set<AnnotationInfo> generated, ProcessingEnvironment processingEnv) {
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
		ClassName bsoneerCodecRegistryClassName = ClassName.get(basePackage, "BsoneeCodecRegistry");

		ClassName codecRegistriesName = ClassName.bestGuess("org.bson.codecs.configuration.CodecRegistries");
		ClassName optionsBuilderName = ClassName.bestGuess("com.mongodb.MongoClientOptions.Builder");
		ClassName mongoClientName = ClassName.bestGuess("com.mongodb.MongoClient");
		ClassName mongoDatabaseName = ClassName.bestGuess("com.mongodb.client.MongoDatabase");

		ClassName mongoCollectionName = ClassName.bestGuess("com.mongodb.client.MongoCollection");
		TypeVariableName typeVariableName = TypeVariableName.get("T");
		ParameterizedTypeName parameterizedCollection = ParameterizedTypeName.get(mongoCollectionName, typeVariableName);

		TypeSpec.Builder codecProviderBuilder = TypeSpec.classBuilder(bsoneerCodecRegistryClassName.simpleName())
		        .addJavadoc(ProcessorJavadocs.GENERATED_BY_BSONEER)
		        .addModifiers(PUBLIC).addAnnotation(AnnotationSpec.builder(Generated.class)
		        		.addMember("value", "$S", BsonProcessor.class.getCanonicalName())
		        		.build());;

		codecProviderBuilder.addMethod(MethodSpec.methodBuilder("to").addModifiers(PUBLIC, STATIC)
				.addParameter(CodeUtil.bsonRegistryParameter())
				.addStatement("return $T.fromRegistries(registry, registry())", codecRegistriesName)
				.returns(CodeUtil.bsonRegistryTypeName())
				.build());

		codecProviderBuilder.addMethod(MethodSpec.methodBuilder("to").addModifiers(PUBLIC, STATIC)
				.addParameter(ParameterSpec.builder(
						optionsBuilderName, "mcob",
						FINAL).build())
				.addStatement("return mcob.codecRegistry($T.fromRegistries("
						+ "$T.getDefaultCodecRegistry(), registry()))",
						codecRegistriesName, mongoClientName)
				.returns(optionsBuilderName)
				.build());

		codecProviderBuilder.addMethod(MethodSpec.methodBuilder("to").addModifiers(PUBLIC, STATIC)
				.addParameter(ParameterSpec.builder(
						mongoDatabaseName, "db",
						FINAL).build())
				.addStatement("return db.withCodecRegistry($T.fromRegistries("
						+ "db.getCodecRegistry(), registry()))",
						codecRegistriesName)
				.returns(mongoDatabaseName)
				.build());

		codecProviderBuilder.addMethod(MethodSpec.methodBuilder("to").addModifiers(PUBLIC, STATIC)
				.addParameter(ParameterSpec.builder(
						parameterizedCollection,
						"mc", FINAL).build())
				.addStatement("return mc.withCodecRegistry($T.fromRegistries("
						+ "mc.getCodecRegistry(), registry()))",
						codecRegistriesName)
				.returns(parameterizedCollection)
				.addTypeVariable(typeVariableName)
				.build());

		codecProviderBuilder.addMethod(MethodSpec.methodBuilder("registry").addModifiers(PUBLIC, STATIC)
				.addStatement("return $T.fromProviders(new $T())",
						codecRegistriesName, bsoneerCodecProviderClassName)
				.returns(CodeUtil.bsonRegistryTypeName())
				.build());

		return JavaFile.builder(bsoneerCodecRegistryClassName.packageName(), codecProviderBuilder.build())
				.addFileComment(ProcessorJavadocs.GENERATED_BY_BSONEER)
				.indent("\t")
				.build();
	}

}
