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
			if (Util.isEnum(key)) {
				setters.put(vi.getName(), setterClassName);

				TypeSpec.Builder setterBuilder = createSetterClass(entityClassName, setterIface,
						"String", vi, setterClassName, "$T.valueOf", ClassName.get(key));
				codecBuilder.addType(setterBuilder.build());
			} else {
				setters.put(vi.getName(), setterClassName);
				TypeSpec.Builder setterBuilder = createSetterClass(entityClassName, setterIface,
						readMethod, vi, setterClassName, null);
				codecBuilder.addType(setterBuilder.build());
			}
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
		setterBuilder.addMethod(setterMethod.build()).addSuperinterface(setterIface);
		return setterBuilder;
	}

}
