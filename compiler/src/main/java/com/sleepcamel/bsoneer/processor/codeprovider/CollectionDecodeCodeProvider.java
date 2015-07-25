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

import com.sleepcamel.bsoneer.processor.util.NameGenerator;
import com.sleepcamel.bsoneer.processor.util.Util;

public class CollectionDecodeCodeProvider extends IterableDecodeCodeProvider {

	public CollectionDecodeCodeProvider(NameGenerator nameGenerator) {
		super(nameGenerator);
	}

	@Override
	public boolean applies(TypeMirror tm) {
		return Util.isJavaCollection(tm);
	}
	
	@Override
	public TypeMirror getIterableType(TypeMirror tm) {
		return Util.collectionTypeArgument(tm);
	}
	
	@Override
	public boolean supportsNull(TypeMirror tm) {
		return true;
	}

	@Override
	public String getAddMethod() {
		return "add";
	}

	@Override
	public TypeMirror getIterableImplementationType(TypeMirror tm) {
		return Util.getJavaCollectionClass(tm, true, false);
	}

	@Override
	public TypeMirror getIterableDeclarationType(TypeMirror tm) {
		return Util.getJavaCollectionClass(tm, false, false);		
	}

}
