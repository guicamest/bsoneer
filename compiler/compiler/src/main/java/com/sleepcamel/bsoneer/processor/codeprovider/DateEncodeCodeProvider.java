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
import java.util.Date;

import javax.lang.model.type.TypeMirror;

import com.sleepcamel.bsoneer.processor.domain.Property;
import com.squareup.javapoet.CodeBlock;

public class DateEncodeCodeProvider implements EncodeCodeProvider {

	@Override
	public boolean applies(Property property) {
		return applies(property.getResolvedType());
	}
	
	@Override
	public boolean applies(TypeMirror tm) {
		return Date.class.getCanonicalName().equals(tm.toString());
	}
	
	@Override
	public void putEncodeCode(CodeBlock.Builder cb, TypeMirror tm, Collection<EncodeCodeProvider> allProviders, String varName) {
		cb.addStatement("writer.writeDateTime($L.getTime())", varName);
	}

}
