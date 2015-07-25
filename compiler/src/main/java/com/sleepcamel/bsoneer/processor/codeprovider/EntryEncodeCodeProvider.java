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

import java.util.Collection;
import java.util.Map;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import com.sleepcamel.bsoneer.processor.domain.Property;
import com.sleepcamel.bsoneer.processor.util.UtilsProvider;
import com.squareup.javapoet.CodeBlock.Builder;

public class EntryEncodeCodeProvider implements EncodeCodeProvider {

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
	public void putEncodeCode(Builder cb, TypeMirror tm, Collection<EncodeCodeProvider> allProviders, String varName) {
		DeclaredType entry = (DeclaredType)tm;
		TypeMirror keyArgument = entry.getTypeArguments().get(0);
		TypeMirror valueArgument = entry.getTypeArguments().get(1);
		
		cb.addStatement("writer.writeStartDocument()");
		cb.addStatement("writer.writeName(\"key\")");
		cb.beginControlFlow("if ($L == null)", varName+".getKey()");
		cb.addStatement("writer.writeNull()");
		cb.nextControlFlow("else");
		for(EncodeCodeProvider provider:allProviders){
			if (provider.applies(keyArgument)){
				provider.putEncodeCode(cb, keyArgument, allProviders, varName+".getKey()");
				break;
			}
		}
		cb.endControlFlow();
		
		cb.addStatement("writer.writeName(\"value\")");
		cb.beginControlFlow("if ($L == null)", varName+".getValue()");
		cb.addStatement("writer.writeNull()");
		cb.nextControlFlow("else");
		for(EncodeCodeProvider provider:allProviders){
			if (provider.applies(valueArgument)){
				provider.putEncodeCode(cb, valueArgument, allProviders, varName+".getValue()");
				break;
			}
		}
		cb.endControlFlow();
		cb.addStatement("writer.writeEndDocument()");
	}

}
