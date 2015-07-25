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

import javax.lang.model.type.TypeMirror;

import com.sleepcamel.bsoneer.processor.domain.Property;
import com.sleepcamel.bsoneer.processor.util.NameGenerator;
import com.squareup.javapoet.CodeBlock.Builder;
import com.squareup.javapoet.TypeName;

abstract public class IterableEncodeCodeProvider implements EncodeCodeProvider {

	private NameGenerator nameGenerator;

	public IterableEncodeCodeProvider(NameGenerator nameGenerator) {
		this.nameGenerator = nameGenerator;
	}

	@Override
	public boolean applies(Property property) {
		return applies(property.getResolvedType());
	}
	
	public abstract TypeMirror getIterableCast(TypeMirror tm);
	
	public abstract TypeMirror getIterableEnclosedType(TypeMirror tm);
	
	public abstract TypeName getIterableEnclosedTypeName(TypeMirror tm);
	
	@Override
	public void putEncodeCode(Builder cb, TypeMirror iterableTypeMirror, Collection<EncodeCodeProvider> allProviders, String varName) {
		TypeMirror iterableTypeArgument = getIterableEnclosedType(iterableTypeMirror);
		TypeName iterableTypeArgumentTypeName = getIterableEnclosedTypeName(iterableTypeMirror);

		String instanceName = nameGenerator.instanceName(iterableTypeArgument);
		cb.addStatement("writer.writeStartArray()");
		cb.beginControlFlow("for ($T $L:(($T)$L))", iterableTypeArgumentTypeName, instanceName, getIterableCast(iterableTypeMirror), varName);
		
		boolean primitive = iterableTypeArgument.getKind().isPrimitive();
		
		if ( !primitive ){
			cb.beginControlFlow("if ($L == null)", instanceName);
			cb.addStatement("writer.writeNull()");
			cb.nextControlFlow("else");
		}
		for(EncodeCodeProvider provider:allProviders){
			if (provider.applies(iterableTypeArgument)){
				String newVarName = nameGenerator.instanceName(iterableTypeArgument).toLowerCase();
				provider.putEncodeCode(cb, iterableTypeArgument, allProviders, newVarName);
				break;
			}
		}
		if ( !primitive ){
			cb.endControlFlow();
		}
		cb.endControlFlow();
		cb.addStatement("writer.writeEndArray()");
		return;
	}

}
