package com.sleepcamel.bsoneer.processor.generators;

import static javax.lang.model.element.Modifier.PUBLIC;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

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

public class BsoneeCodecGenerator {

	private TypeElement type;
	private ProcessingEnvironment processingEnv;
	private AnnotationInfo ai;
	private ClassName superType;

	public BsoneeCodecGenerator(TypeElement type, AnnotationInfo ai, ProcessingEnvironment processingEnv) {
		this.type = type;
		this.ai = ai;
		this.superType = Util.getSuperType(type, processingEnv);
		this.processingEnv = processingEnv;
	}

	public JavaFile getJavaFile() {
		ClassName entityClassName = ClassName.get(type);

		TypeElement baseCodecType = processingEnv.getElementUtils()
				.getTypeElement("com.sleepcamel.bsoneer.BaseBsoneerCodec");
		TypeName extendedCodecTypeName = ParameterizedTypeName
				.get(ClassName.get(baseCodecType), entityClassName);

		ClassName bsoneerCodecClassName = Util.bsoneeName(entityClassName,
				GeneratedClasses.BSONEE_CODEC_SUFFIX);

		TypeSpec.Builder codecBuilder = TypeSpec.classBuilder(bsoneerCodecClassName.simpleName())
		        .addJavadoc(ProcessorJavadocs.GENERATED_BY_BSONEER)
		        .superclass(extendedCodecTypeName)
		        .addModifiers(PUBLIC);

//		org.bson.BsonBinarySubType
//		org.bson.BsonType

		addBaseConstructor(codecBuilder, entityClassName);
		addRegConstructor(codecBuilder, entityClassName);
		addEncoderClassMethod(codecBuilder, entityClassName);
		addEncodeMethod(codecBuilder, entityClassName);
		addDecodeCode(codecBuilder, entityClassName);

		return JavaFile.builder(bsoneerCodecClassName.packageName(), codecBuilder.build())
				.addFileComment(ProcessorJavadocs.GENERATED_BY_BSONEER)
				.indent("\t")
				.build();
	}

	private void addBaseConstructor(com.squareup.javapoet.TypeSpec.Builder codecBuilder, ClassName entityClassName) {
//		public BaseBsoneerCodec()
		codecBuilder.addMethod(MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addStatement("super()")
				.build());
	}

	private void addRegConstructor(com.squareup.javapoet.TypeSpec.Builder codecBuilder, ClassName entityClassName) {
//		public BaseBsoneerCodec(final CodecRegistry registry) {
		codecBuilder.addMethod(MethodSpec.constructorBuilder()
				.addParameter(Util.bsonRegistryParameter())
				.addModifiers(Modifier.PUBLIC).addStatement("super(registry)")
				.build());
	}

	private void addEncoderClassMethod(com.squareup.javapoet.TypeSpec.Builder codecBuilder, ClassName entityClassName) {
		TypeName clazzName = ParameterizedTypeName.get(ClassName.get(Class.class), entityClassName);
		Builder methodSpec = MethodSpec.methodBuilder("getEncoderClass")
				.addModifiers(Modifier.PUBLIC)
				.returns(clazzName)
				.addJavadoc("{@inhericDoc}\n");
		methodSpec.addStatement("return $T.class", entityClassName);
		codecBuilder.addMethod(methodSpec.build());
	}

	private void addEncodeMethod(com.squareup.javapoet.TypeSpec.Builder codecBuilder, ClassName entityClassName) {
		Builder methodSpec = MethodSpec.methodBuilder("encodeVariables")
				.addAnnotation(Override.class)
				.addAnnotation(Util.suppressWarningsAnnotation())
				.addModifiers(Modifier.PROTECTED)
				.addParameter(Util.bsonWriterParameter())
				.addParameter(ParameterSpec.builder(entityClassName, "value").build())
				.addParameter(Util.bsonEncoderContextParameter())
				.addJavadoc("{@inhericDoc}\n");

		GetterElementVisitor getterVisitor = new GetterElementVisitor(processingEnv, ai);

		for (Element ee : getFields(type)) {
			ee.accept(getterVisitor, false);
		}

		for (Element ee : getMethods(type)) {
			ee.accept(getterVisitor, false);
		}
		getterVisitor.writeBody(methodSpec);
		methodSpec.addStatement("super.encodeVariables(writer,value,encoderContext)");

		codecBuilder.addMethod(methodSpec.build());
	}

	private void addDecodeCode(com.squareup.javapoet.TypeSpec.Builder codecBuilder, ClassName entityClassName) {
		codecBuilder.addMethod(MethodSpec.methodBuilder("instantiate")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PROTECTED)
				.addStatement("return new $T()", entityClassName)
				.returns(entityClassName)
				.build());

		SetterElementVisitor setterVisitor = new SetterElementVisitor(processingEnv, ai);

		for (Element ee : getFields(type)) {
			ee.accept(setterVisitor, true);
		}

		for (Element ee : getMethods(type)) {
			ee.accept(setterVisitor, true);
		}
		setterVisitor.writeBody(codecBuilder, entityClassName);
	}

	private List<VariableElement> getFields(TypeElement type) {
		List<VariableElement> fieldsIn = ElementFilter.fieldsIn(type.getEnclosedElements());
		ClassName superType = Util.getSuperType(type, processingEnv);
		if (superType != null) {
			TypeElement superClassType = processingEnv.getElementUtils().getTypeElement(superType.toString());
			fieldsIn.addAll(getFields(superClassType));
		}
		return fieldsIn;
	}

	private List<ExecutableElement> getMethods(TypeElement type) {
		List<ExecutableElement> methodsIn = ElementFilter.methodsIn(type.getEnclosedElements());
		ClassName superType = Util.getSuperType(type, processingEnv);
		if (superType != null) {
			TypeElement superClassType = processingEnv.getElementUtils().getTypeElement(superType.toString());
			methodsIn.addAll(getMethods(superClassType));
		}
		return methodsIn;
	}
}
