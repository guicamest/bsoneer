package com.sleepcamel.bsoneer.processor.generators;

import static javax.lang.model.element.Modifier.PUBLIC;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import com.sleepcamel.bsoneer.processor.GeneratedClasses;
import com.sleepcamel.bsoneer.processor.util.ProcessorJavadocs;
import com.sleepcamel.bsoneer.processor.util.Util;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

public class BsoneeCodecProviderGenerator {
	private Set<ClassName> generated = new HashSet<ClassName>();
	private final String basePackage;

	public BsoneeCodecProviderGenerator(Set<AnnotationInfo> generated, ProcessingEnvironment processingEnv) {
		String basePackage = null;
		for (AnnotationInfo generatedClass : generated) {
			ClassName entityClassName = ClassName.bestGuess(generatedClass.typeAsString());
			if (basePackage == null || basePackage.length() > entityClassName.packageName().length()) {
				basePackage = entityClassName.packageName();
			}
			this.generated.add(entityClassName);
		}
		this.basePackage = basePackage;
	}

	public JavaFile getJavaFile() {
		ClassName bsoneerCodecProviderClassName = ClassName.get(basePackage, "BsoneeCodecProvider");

		TypeSpec.Builder codecProviderBuilder = TypeSpec.classBuilder(bsoneerCodecProviderClassName.simpleName())
		        .addJavadoc(ProcessorJavadocs.GENERATED_BY_BSONEER)
		        .addSuperinterface(Util.bsonCodecProviderTypeName())
		        .addModifiers(PUBLIC);

		addBaseConstructor(codecProviderBuilder);
		addGetCodecMethod(codecProviderBuilder);

		return JavaFile.builder(bsoneerCodecProviderClassName.packageName(), codecProviderBuilder.build())
				.addFileComment(ProcessorJavadocs.GENERATED_BY_BSONEER)
				.indent("\t")
				.build();
	}

	private void addBaseConstructor(com.squareup.javapoet.TypeSpec.Builder codecBuilder) {
		codecBuilder.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build());
	}

	private void addGetCodecMethod(com.squareup.javapoet.TypeSpec.Builder codecProviderBuilder) {
		TypeVariableName typeVariableName = TypeVariableName.get("T");
		TypeName clazzName = ParameterizedTypeName.get(ClassName.get(Class.class), typeVariableName);
		TypeName codecName = ParameterizedTypeName.get((ClassName) Util.bsonCodecTypeName(), typeVariableName);

		Builder methodSpec = MethodSpec.methodBuilder("get")
				.addAnnotation(Override.class)
				.addAnnotation(Util.suppressWarningsAnnotation(true, false))
				.addModifiers(Modifier.PUBLIC)
				.addParameter(ParameterSpec.builder(clazzName, "clazz", Modifier.FINAL).build())
				.addParameter(Util.bsonRegistryParameter())
				.addTypeVariable(typeVariableName)
				.returns(codecName)
				.addJavadoc("{@inhericDoc}\n");

		for (ClassName entityClassName : generated) {
			ClassName bsoneerCodecClassName = Util.bsoneeName(entityClassName, GeneratedClasses.BSONEE_CODEC_SUFFIX);
			methodSpec.beginControlFlow("if (clazz == $T.class)", entityClassName);
			methodSpec.addStatement("return (Codec<T>) new $T(registry)", bsoneerCodecClassName);
			methodSpec.endControlFlow();
		}

		methodSpec.addStatement("return null");
		codecProviderBuilder.addMethod(methodSpec.build());
	}

}
