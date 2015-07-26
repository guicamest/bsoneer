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

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.squareup.javapoet.TypeName;

public class ArrayEncodeCodeProvider extends IterableEncodeCodeProvider {

	public ArrayEncodeCodeProvider(NameGenerator nameGenerator) {
		super(nameGenerator);
	}

	@Override
	public boolean applies(TypeMirror tm) {
		return tm.getKind() == TypeKind.ARRAY;
	}

	@Override
	public TypeMirror getIterableEnclosedType(TypeMirror tm) {
		return ((ArrayType) tm).getComponentType();
	}

	@Override
	public TypeName getIterableEnclosedTypeName(TypeMirror tm) {
		return TypeName.get(getIterableEnclosedType(tm));
	}

	@Override
	public TypeMirror getIterableCast(TypeMirror tm) {
		return tm;
	}
	
}
