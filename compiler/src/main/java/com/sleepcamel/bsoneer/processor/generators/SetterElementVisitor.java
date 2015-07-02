package com.sleepcamel.bsoneer.processor.generators;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.google.common.base.Strings;
import com.sleepcamel.bsoneer.BsoneeBaseSetter;
import com.sleepcamel.bsoneer.processor.util.Util;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

class SetterElementVisitor extends BaseVisitor {

	public SetterElementVisitor(ProcessingEnvironment processingEnv, AnnotationInfo ai) {
		super(processingEnv, "set", true, 1, ai);
	}

	public void writeBody(TypeSpec.Builder codecBuilder, ClassName entityClassName) {
		TypeName setterIface = ParameterizedTypeName.get(ClassName.get(BsoneeBaseSetter.class), entityClassName);
		Map<String, String> setters = new HashMap<String, String>();

		for (VarInfo vi : visitedVars.values()) {
			String setterClassName = vi.getUpperName() + "Setter";
			setters.put(vi.getBsonName(), setterClassName);
			codecBuilder.addType(createSetterClass(entityClassName, setterIface, setterClassName, vi).build());
		}

		Builder setupSetterBuilder = MethodSpec.methodBuilder("setupSetters").addModifiers(Modifier.PROTECTED);
		for (Entry<String, String> entry : setters.entrySet()) {
			setupSetterBuilder.addStatement("settersByName.put(\"$L\",new $L())",
					entry.getKey(), entry.getValue());
		}
		codecBuilder.addMethod(setupSetterBuilder.build());
	}
	
	private TypeSpec.Builder createSetterClass(ClassName entityClassName, TypeName setterIface, String setterClassName, VarInfo vi) {
		TypeSpec.Builder setterBuilder = TypeSpec.classBuilder(setterClassName);
		
		TypeMirror key = getReplaceTypeIfTypeVar(vi);
		String readMethod = passThroughMappings.get(key.toString());
		
		Builder setterMethod = MethodSpec.methodBuilder("set")
				.addParameter(ParameterSpec.builder(entityClassName, "instance").build())
				.addParameter(Util.bsonReaderParameter())
				.addParameter(Util.bsonDecoderContextParameter())
				.addModifiers(Modifier.PUBLIC);
		
		if (Util.isEnum(key)) {
			addSetterCode(setterMethod, "String", vi, "$T.valueOf", ClassName.get(key));
		} else if (isJavaCollection(key)) {
			addSetterCodeForCollection(setterMethod, vi);
//		} else if (key.getKind() == TypeKind.ARRAY) { 
			// TODO Implement array deserialization
//			addSetterCodeForCollection(setterMethod, vi, k);
		} else {
			addSetterCode(setterMethod, readMethod, vi, null);
		}
		
		return setterBuilder.addMethod(setterMethod.build()).addSuperinterface(setterIface);
	}

	private void addSetterCode(Builder setterMethod, String readMethod, VarInfo vi, String wrappedCall, Object... wrappedCallArguments) {
		String accessName = vi.getMethod();
		String readerCall = "reader.read$L()";
		if (readMethod == null) {
			String cast = vi.getTypeMirror().getKind().equals(TypeKind.DECLARED)
							? "(" + vi.getTypeMirror().toString() + ")" : "";
			readerCall = cast + "defaultReader.readValue$L(reader,decoderContext)";
			readMethod = "";
		}

		boolean wrapped = !Strings.nullToEmpty(wrappedCall).isEmpty();
		if (wrapped) {
			readerCall = wrappedCall + "(" + readerCall + ")";
		}
		accessName += vi.isMethod() ? "(" + readerCall + ")" : " = " + readerCall;
		if (wrapped && wrappedCallArguments != null && wrappedCallArguments.length != 0) {
			setterMethod.addStatement("instance." + accessName, wrappedCallArguments[0], readMethod);
		} else {
			setterMethod.addStatement("instance." + accessName, readMethod);
		}
	}

	private void addSetterCodeForCollection(Builder setterMethod, VarInfo vi){
		String getAccessName = vi.getMethod();
		if ( vi.isMethod() ){
			// TODO Support isSmth() getters
			getAccessName = "get"+getAccessName.substring(3);
		}
		getAccessName += vi.isMethod() ? "()" : "";
		
		String setAccessName = vi.getMethod();
		setAccessName += vi.isMethod() ? "($L)" : " = $L";
		
		TypeMirror collectionTypeMirror = vi.getTypeMirror();
		TypeMirror collectionTypeArgument = collectionTypeArgument(vi, collectionTypeMirror);
		boolean collectionTypeArgumentIsVar = collectionTypeArgument.getKind() == TypeKind.TYPEVAR;
		
		TypeMirror javaImplementationCollectionClass = getJavaCollectionClass(vi, collectionTypeMirror, true, collectionTypeArgumentIsVar);
		TypeMirror javaDeclarationCollectionClass = getJavaCollectionClass(vi, collectionTypeMirror, false, collectionTypeArgumentIsVar);
		
		setterMethod.addStatement("$T bsonType = reader.getCurrentBsonType()",  Util.bsonTypeTypeName());
		setterMethod.beginControlFlow("if (bsonType == $T.NULL)", Util.bsonTypeTypeName());
		setterMethod.addStatement("reader.readNull()");
		setterMethod.addStatement("instance."+setAccessName,"null");
		setterMethod.addStatement("return");
		setterMethod.endControlFlow();
		
		setterMethod.addStatement("$T value = instance.$L", javaDeclarationCollectionClass, getAccessName);
		
		setterMethod.beginControlFlow("if (value == null)");
		setterMethod.addStatement("value = new $T()", TypeName.get(javaImplementationCollectionClass));
		setterMethod.addStatement("instance."+setAccessName,"value");
		setterMethod.endControlFlow();
		
		setterMethod.addStatement("reader.readStartArray()");
		
		setterMethod.beginControlFlow("while (reader.readBsonType() != $T.END_OF_DOCUMENT)", Util.bsonTypeTypeName());
		if ( !collectionTypeArgumentIsVar ){
			setterMethod.addStatement("value.add(($T)defaultReader.readValue(reader, decoderContext))", collectionTypeArgument);
		}else{
			setterMethod.addStatement("value.add(defaultReader.readValue(reader, decoderContext))");
		}
		setterMethod.endControlFlow();
		setterMethod.addStatement("reader.readEndArray()");
	}

}
