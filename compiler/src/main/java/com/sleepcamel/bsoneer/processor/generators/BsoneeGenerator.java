package com.sleepcamel.bsoneer.processor.generators;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import com.sleepcamel.bsoneer.processor.GeneratedClasses;
import com.sleepcamel.bsoneer.processor.util.ProcessorJavadocs;
import com.sleepcamel.bsoneer.processor.util.Util;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

public class BsoneeGenerator {

	private TypeElement type;
	private ProcessingEnvironment processingEnv;

	public BsoneeGenerator(TypeElement type, ProcessingEnvironment processingEnv) {
		this.type = type;
		this.processingEnv = processingEnv;
	}

	public JavaFile getJavaFile() {
		Elements elementUtils = processingEnv.getElementUtils();
		TypeElement bsonDocumentType = elementUtils.getTypeElement("org.bson.BsonDocument");
		TypeElement bsonType = elementUtils.getTypeElement("org.bson.conversions.Bson");
		TypeElement codecRegType = elementUtils.getTypeElement("org.bson.codecs.configuration.CodecRegistry");
		ClassName bsonClassName = ClassName.get(bsonType);
		ClassName bsonDocumentClassName = ClassName.get(bsonDocumentType);
		ClassName baseEntityClassName = ClassName.get(type);
		ClassName bsoneerClassName = Util.bsoneeName(baseEntityClassName, GeneratedClasses.BSONEE_WRAPPER_SUFFIX);

		TypeSpec.Builder adapterBuilder = TypeSpec.classBuilder(bsoneerClassName.simpleName())
		        .addOriginatingElement(type)
		        .addJavadoc(ProcessorJavadocs.GENERATED_BY_BSONEER)
		        .superclass(baseEntityClassName)
		        .addSuperinterface(bsonClassName)
		        .addModifiers(PUBLIC, FINAL);

//		org.bson.BsonBinarySubType
//		org.bson.BsonType

		TypeVariableName cTypeName = TypeVariableName.get("C");
		ParameterizedTypeName paramTypeName = ParameterizedTypeName.get(ClassName.get(Class.class), cTypeName);
		Builder toBsonDocumentMethod = MethodSpec.methodBuilder("toBsonDocument")
				.addParameter(paramTypeName, "documentClass", Modifier.FINAL)
				.addParameter(TypeName.get(codecRegType.asType()), "codecRegistry", Modifier.FINAL)
				.returns(bsonDocumentClassName)
				.addTypeVariable(cTypeName)
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Override.class);
		addBodyToMethod(type, toBsonDocumentMethod);
		adapterBuilder.addMethod(toBsonDocumentMethod.build());

	    return JavaFile.builder(bsoneerClassName.packageName(), adapterBuilder.build())
	            .addFileComment(ProcessorJavadocs.GENERATED_BY_BSONEER)
	            .build();
	}

	private void addBodyToMethod(TypeElement type, Builder toBsonDocumentMethod) {
		toBsonDocumentMethod.addStatement("return null");
	}
}
