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

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.sleepcamel.bsoneer.processor.util.NameGenerator;
import com.sleepcamel.bsoneer.processor.util.UtilsProvider;
import com.squareup.javapoet.CodeBlock.Builder;

public class ArrayDecodeCodeProvider extends IterableDecodeCodeProvider {

	public ArrayDecodeCodeProvider(NameGenerator nameGenerator) {
		super(nameGenerator);
	}

	@Override
	public boolean applies(TypeMirror tm) {
		return tm.getKind() == TypeKind.ARRAY;
	}
	
	@Override
	public TypeMirror getIterableType(TypeMirror tm) {
		return ((ArrayType) tm).getComponentType();
	}
	
	@Override
	public boolean supportsNull(TypeMirror iterableType) {
		return !iterableType.getKind().isPrimitive();
	}
	
	@Override
	public String getAddMethod() {
		return "add";
	}

	protected boolean initializeProperty() {
		return false;
	}

	@Override
	public TypeMirror getIterableImplementationType(TypeMirror tm) {
		TypeMirror iterableType = getIterableType(tm);
		Types types = UtilsProvider.getTypes();
		if ( iterableType.getKind().isPrimitive() ){
			iterableType = types.boxedClass((PrimitiveType) iterableType).asType();
		}
		
		Elements elements = UtilsProvider.getElements();
		TypeElement typeElement = elements.getTypeElement(ArrayList.class.getCanonicalName());
		return types.getDeclaredType(typeElement, iterableType);
	}

	@Override
	public TypeMirror getIterableDeclarationType(TypeMirror tm) {
		TypeMirror iterableType = getIterableType(tm);
		Types types = UtilsProvider.getTypes();
		if ( iterableType.getKind().isPrimitive() ){
			iterableType = types.boxedClass((PrimitiveType) iterableType).asType();
		}
		
		Elements elements = UtilsProvider.getElements();
		TypeElement typeElement = elements.getTypeElement(List.class.getCanonicalName());
		return types.getDeclaredType(typeElement, iterableType);	
	}
	
	protected void putDecodeCode(Builder cb, TypeMirror iterableTypeMirror, DecodeCodeProviders decodeProviders, String variableToUse, boolean instantiate, boolean declareVariable) {
		String temporalVariable = variableToUse+"tmp";
		super.putDecodeCode(cb, iterableTypeMirror, decodeProviders, temporalVariable, instantiate, declareVariable);
		
		TypeMirror iterableType = getIterableType(iterableTypeMirror);
		if ( !iterableType.getKind().isPrimitive() ){
			cb.addStatement("$T $L = $L.toArray(new $T[]{})", iterableTypeMirror, variableToUse, temporalVariable, iterableType);
		}else{
			cb.addStatement("$T $L = new $T[$L.size()]", iterableTypeMirror, variableToUse, iterableType, temporalVariable);
			String idxName = variableToUse+"TmpIdx";
			cb.beginControlFlow("for(int $L=0; $L < $L.length; $L++)", idxName, idxName, variableToUse, idxName);
			cb.addStatement("$L[$L] = $L.get($L)", variableToUse, idxName, temporalVariable, idxName);
			cb.endControlFlow();
		}
	}

}
