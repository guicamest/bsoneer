package com.sleepcamel.bsoneer.processor.generators;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import com.sleepcamel.bsoneer.processor.util.ProcessorJavadocs;
import com.sleepcamel.bsoneer.processor.util.Util;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterizedTypeName;
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
		        .addSuperinterface(Util.bsonTypeName())
		        .addTypeVariable(typeVariable)
		        .addModifiers(PUBLIC, FINAL);

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
				.addParameter(Util.bsonRegistryParameter());
		addBodyToMethod(toBsonDocumentMethod);
		bsoneeBuilder.addMethod(toBsonDocumentMethod.build());

		TypeName bsoneeParameterized = ParameterizedTypeName.get(
				bsoneerBsonName, cTypeName);

		Builder bsonMethodBuilder = MethodSpec.methodBuilder("bson")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.returns(Util.bsonTypeName())
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
				Util.bsonCodecTypeName());
		toBsonDocumentMethod.beginControlFlow("if (codec == null)");
		toBsonDocumentMethod.addStatement("codec = bcp.get(wrappedClazz, registry)");
		toBsonDocumentMethod.endControlFlow();
		toBsonDocumentMethod.addStatement("return new $T(wrapped, codec)",
				ClassName.bestGuess("org.bson.RawBsonDocument"));
	}
}
