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
package com.sleepcamel.bsoneer.processor.codeprovider;

import java.util.List;

import javax.lang.model.type.TypeMirror;

import com.google.common.base.Joiner;
import com.sleepcamel.bsoneer.processor.domain.Property;
import com.sleepcamel.bsoneer.processor.util.NameGenerator;
import com.sleepcamel.bsoneer.processor.util.Util;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import com.squareup.javapoet.TypeName;

public abstract class IterableDecodeCodeProvider implements DecodeCodeProvider {

	private NameGenerator nameGenerator;

	public IterableDecodeCodeProvider(NameGenerator nameGenerator) {
		this.nameGenerator = nameGenerator;
	}
	
	public abstract TypeMirror getIterableType(TypeMirror tm);
	
	public abstract TypeMirror getIterableDeclarationType(TypeMirror tm);
	
	public abstract TypeMirror getIterableImplementationType(TypeMirror tm);
	
	public abstract boolean supportsNull(TypeMirror tm);

	public abstract String getAddMethod();
	
	protected boolean initializeProperty() {
		return true;
	}

	@Override
	public boolean applies(Property property) {
		return applies(property.getResolvedType());
	}
	
	@Override
	public boolean hasToInstantiate() {
		return true;
	}
	
	@Override
	public CodeBlock getDecodeCode(Property property, DecodeCodeProviders decodeProviders) {
		Builder builder = CodeBlock.builder();
		
		String getAccessName = property.getGetterCall();

		String setAccessName = property.getSetterCall();
		setAccessName += property.hasSetter() ? "($L)" : " = $L";

		TypeMirror resolvedType = property.getResolvedType();

		builder.addStatement("$T bsonType = reader.getCurrentBsonType()",  Util.bsonTypeTypeName());
		builder.beginControlFlow("if (bsonType == $T.NULL)", Util.bsonTypeTypeName());
		builder.addStatement("reader.readNull()");
		builder.addStatement("instance." + setAccessName, "null");
		builder.addStatement("return");
		builder.endControlFlow();

		if ( initializeProperty() ){
			TypeMirror iterableImplementationCollectionClass = getIterableImplementationType(resolvedType);
			TypeMirror iterableDeclarationCollectionClass = getIterableDeclarationType(resolvedType);
			builder.addStatement("$T value = instance.$L", iterableDeclarationCollectionClass, getAccessName);

			builder.beginControlFlow("if (value == null)");
			builder.addStatement("value = new $T()", TypeName.get(iterableImplementationCollectionClass));
			builder.addStatement("instance." + setAccessName, "value");
			builder.endControlFlow();
		}
		
		putDecodeCode(builder, resolvedType, decodeProviders, !initializeProperty(), "value");
		if ( !initializeProperty() ){
			builder.addStatement("instance." + setAccessName, "value");
		}

		return builder.build();
	}

	@Override
	public void putDecodeCode(Builder cb, TypeMirror iterableTypeMirror, DecodeCodeProviders decodeProviders, String variableToUse, boolean declareVariable) {
		putDecodeCode(cb, iterableTypeMirror, decodeProviders, variableToUse, true, declareVariable);
	}
	
	public void putDecodeCode(Builder cb, TypeMirror iterableTypeMirror, DecodeCodeProviders decodeProviders, boolean instantiate, String variableToUse) {
		putDecodeCode(cb, iterableTypeMirror, decodeProviders, variableToUse, instantiate, true);
	}
	
	protected void putDecodeCode(Builder cb, TypeMirror iterableTypeMirror, DecodeCodeProviders decodeProviders, String variableToUse, boolean instantiate, boolean declareVariable) {
		TypeMirror iterableType = getIterableType(iterableTypeMirror);
		String addMethod = getAddMethod();
		
		if ( instantiate ){
			TypeMirror iterableImplementationCollectionClass = getIterableImplementationType(iterableTypeMirror);
			TypeMirror iterableDeclarationCollectionClass = getIterableDeclarationType(iterableTypeMirror);
			if ( declareVariable ){
				cb.addStatement("$T $L = new $T()", iterableDeclarationCollectionClass, variableToUse, iterableImplementationCollectionClass);
			}else{
				cb.addStatement("$L = new $T()", variableToUse, iterableImplementationCollectionClass);
			}
		}

		cb.addStatement("reader.readStartArray()");
		cb.beginControlFlow("while (reader.readBsonType() != $T.END_OF_DOCUMENT)", Util.bsonTypeTypeName());
		cb.addStatement("bsonType = reader.getCurrentBsonType()",  Util.bsonTypeTypeName());
		cb.beginControlFlow("if (bsonType == $T.NULL)", Util.bsonTypeTypeName());
		cb.addStatement("reader.readNull()");
		if ( supportsNull(iterableType) ){
			cb.addStatement("$L.$L(null)", variableToUse, addMethod);
		}
		cb.nextControlFlow("else");
		Builder builder2 = CodeBlock.builder();
		if ( decodeProviders.applies(iterableType) ){
			String varName = nameGenerator.instanceName(iterableType).toLowerCase();
			decodeProviders.putDecodeCode(builder2, iterableType, varName);
			boolean hasToInstantiate = decodeProviders.hasToInstantiate(iterableType);
			if ( hasToInstantiate ){
				cb.add(builder2.build());
			}
			cb.add("$L.$L(",variableToUse, addMethod);
			if ( hasToInstantiate ){
				List<String> namesUsed = nameGenerator.generateNames(varName, decodeProviders.variablesUsed(iterableType));
				cb.add(Joiner.on(',').join(namesUsed));
			}else{
				cb.add(builder2.build());
			}
			cb.add(");\n");
		}
		cb.endControlFlow();
		cb.endControlFlow();
		cb.addStatement("reader.readEndArray()");
		return;
	}

	@Override
	public int variablesUsed() {
		return 1;
	}
}
