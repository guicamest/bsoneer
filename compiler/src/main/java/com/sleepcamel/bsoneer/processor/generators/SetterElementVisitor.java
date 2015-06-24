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

	public SetterElementVisitor(ProcessingEnvironment processingEnv) {
		super(processingEnv, "set", true, 1);
	}

	public void writeBody(TypeSpec.Builder codecBuilder, ClassName entityClassName) {
		TypeName setterIface = ParameterizedTypeName.get(ClassName.get(BsoneeBaseSetter.class), entityClassName);
		Map<String, String> setters = new HashMap<String, String>();

		for (VarInfo vi : visitedVars.values()) {
			TypeMirror key = vi.getTypeMirror();
			String setterClassName = vi.getUpperName() + "Setter";
			String readMethod = passThroughMappings.get(key.toString());
			TypeSpec.Builder setterBuilder = null;
			if (Util.isEnum(key)) {
				setterBuilder = createSetterClass(entityClassName, setterIface,
						"String", vi, setterClassName, "$T.valueOf", ClassName.get(key));
			} else if (isJavaCollection(key)) {
				setterBuilder = createSetterClassForCollection(entityClassName, setterIface, vi, setterClassName, getJavaCollectionImplementationClass(key));
			} else {
				setterBuilder = createSetterClass(entityClassName, setterIface,
						readMethod, vi, setterClassName, null);
			}
			setters.put(vi.getName(), setterClassName);
			codecBuilder.addType(setterBuilder.build());
		}

		Builder setupSetterBuilder = MethodSpec.methodBuilder("setupSetters").addModifiers(Modifier.PROTECTED);
		for (Entry<String, String> entry : setters.entrySet()) {
			setupSetterBuilder.addStatement("settersByName.put(\"$L\",new $L())",
					entry.getKey(), entry.getValue());
		}
		codecBuilder.addMethod(setupSetterBuilder.build());
	}

	private TypeSpec.Builder createSetterClass(
			ClassName entityClassName, TypeName setterIface,
			String readMethod, VarInfo vi,
			String setterClassName, String wrappedCall, Object... wrappedCallArguments) {
		TypeSpec.Builder setterBuilder = TypeSpec.classBuilder(setterClassName);
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
		Builder setterMethod = MethodSpec.methodBuilder("set")
				.addParameter(ParameterSpec.builder(entityClassName, "instance").build())
				.addParameter(Util.bsonReaderParameter())
				.addParameter(Util.bsonDecoderContextParameter())
				.addModifiers(Modifier.PUBLIC);
		if (wrapped && wrappedCallArguments != null && wrappedCallArguments.length != 0) {
			setterMethod.addStatement("instance." + accessName, wrappedCallArguments[0], readMethod);
		} else {
			setterMethod.addStatement("instance." + accessName, readMethod);
		}
		return setterBuilder.addMethod(setterMethod.build()).addSuperinterface(setterIface);
	}

	private TypeSpec.Builder createSetterClassForCollection(ClassName entityClassName, TypeName setterIface, VarInfo vi,
			String setterClassName, TypeMirror collImplClass){
		TypeSpec.Builder setterBuilder = TypeSpec.classBuilder(setterClassName);
		String getAccessName = vi.getMethod();
		if ( vi.isMethod() ){
			// TODO Support isSmth() getters
			getAccessName = "get"+getAccessName.substring(3);
		}
		getAccessName += vi.isMethod() ? "()" : "";
		
		String setAccessName = vi.getMethod();
		setAccessName += vi.isMethod() ? "(value)" : " = value";
		
		// TODO Check if collection is initialized with an implementation
		Builder setterMethod = MethodSpec.methodBuilder("set")
				.addParameter(ParameterSpec.builder(entityClassName, "instance").build())
				.addParameter(Util.bsonReaderParameter())
				.addParameter(Util.bsonDecoderContextParameter())
				.addModifiers(Modifier.PUBLIC);
		
		setterMethod.addStatement("$T value = instance.$L", vi.getTypeMirror(), getAccessName);
		
		setterMethod.beginControlFlow("if (value == null)");
		setterMethod.addStatement("value = new $T()", TypeName.get(collImplClass));
		setterMethod.addStatement("instance."+setAccessName);
		setterMethod.endControlFlow();
		
		setterMethod.addStatement("reader.readStartArray()");
		
		setterMethod.beginControlFlow("while (reader.readBsonType() != $T.END_OF_DOCUMENT)", Util.bsonTypeTypeName());
		setterMethod.addStatement("value.add(($T)defaultReader.readValue(reader, decoderContext))", typeArg(vi.getTypeMirror()));
		setterMethod.endControlFlow();
		setterMethod.addStatement("reader.readEndArray()");
		return setterBuilder.addMethod(setterMethod.build()).addSuperinterface(setterIface);		
	}

}
