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

import javax.lang.model.type.TypeMirror;

import com.sleepcamel.bsoneer.processor.domain.Property;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;

abstract public class SingleStatementDecodeCodeProvider implements DecodeCodeProvider {

	public boolean applies(Property property) {
		return applies(property.getResolvedType());
	}
	
	@Override
	public CodeBlock getDecodeCode(Property property, DecodeCodeProviders decodeProviders) {
		Builder builder = CodeBlock.builder();
		TypeMirror tm = property.getResolvedType();
		
		String accessName = property.getSetterCall();
		accessName += property.hasSetter() ? "(" : " = ";
		builder.add("instance." + accessName);
		putDecodeCode(builder, tm, decodeProviders, null, true);
		if ( property.hasSetter() ){
			builder.add(")");
		}
		builder.add(";\n");
		return builder.build();
	}
	
	@Override
	public int variablesUsed() {
		return 1;
	}

	@Override
	public boolean hasToInstantiate() {
		return false;
	}
}
