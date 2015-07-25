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

import java.util.Map;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import com.sleepcamel.bsoneer.processor.domain.Property;
import com.sleepcamel.bsoneer.processor.util.Util;
import com.sleepcamel.bsoneer.processor.util.UtilsProvider;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;

public class EntryDecodeCodeProvider implements DecodeCodeProvider {

	public EntryDecodeCodeProvider() {
	}
	
	@Override
	public boolean applies(Property property) {
		return applies(property.getResolvedType());
	}

	@Override
	public boolean applies(TypeMirror tm) {
		Types types = UtilsProvider.getTypes();
		return Map.Entry.class.getCanonicalName().equals(types.erasure(tm).toString());
	}

	@Override
	public CodeBlock getDecodeCode(Property property, DecodeCodeProviders decodeProviders) {
		return null;
	}

	@Override
	public boolean hasToInstantiate() {
		return true;
	}

	@Override
	public void putDecodeCode(Builder cb, TypeMirror tm, DecodeCodeProviders decodeProviders, String variableToUse, boolean declareVariable) {
		DeclaredType entry = (DeclaredType)tm;
		TypeMirror keyArgument = entry.getTypeArguments().get(0);
		TypeMirror valueArgument = entry.getTypeArguments().get(1);
		
		cb.addStatement("reader.readStartDocument()");
		cb.addStatement("reader.readName(\"key\")");
		cb.addStatement("$T $L = null", keyArgument, variableToUse+"0");
		cb.addStatement("$T $L = null", valueArgument, variableToUse+"1");
		cb.addStatement("bsonType = reader.getCurrentBsonType()",  Util.bsonTypeTypeName());
		cb.beginControlFlow("if (bsonType == $T.NULL)", Util.bsonTypeTypeName());
			cb.addStatement("reader.readNull()");
			cb.addStatement("reader.skipName()");
			cb.addStatement("reader.skipValue()");
		cb.nextControlFlow("else");
			Builder builder = CodeBlock.builder();
			decodeProviders.putDecodeCode(builder, keyArgument, variableToUse+"0", false);
			boolean hasToInstantiate = decodeProviders.hasToInstantiate(keyArgument);
			if ( hasToInstantiate ){
				cb.add(builder.build());
			}else{
				cb.add("$L = ", variableToUse+"0");
				cb.add(builder.build()+";\n");
			}
			cb.addStatement("reader.readName(\"value\")");
			builder = CodeBlock.builder();
			decodeProviders.putDecodeCode(builder, valueArgument, variableToUse+"1", false);
			hasToInstantiate = decodeProviders.hasToInstantiate(valueArgument);
			if ( hasToInstantiate ){
				cb.add(builder.build());
			}else{
				cb.add("$L = ", variableToUse+"1");
				cb.add(builder.build()+";\n");
			}
		cb.endControlFlow();
		cb.addStatement("reader.readEndDocument()");		
	}

	@Override
	public int variablesUsed() {
		return 2;
	}

}
