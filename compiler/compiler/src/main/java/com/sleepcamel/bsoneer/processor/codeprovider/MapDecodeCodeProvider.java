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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.sleepcamel.bsoneer.processor.domain.UtilsProvider;
import com.sleepcamel.bsoneer.processor.util.Util;

public class MapDecodeCodeProvider extends IterableDecodeCodeProvider {

	public MapDecodeCodeProvider(NameGenerator nameGenerator) {
		super(nameGenerator);
	}

	@Override
	public boolean applies(TypeMirror tm) {
		return Util.isJavaMap(tm);
	}

	@Override
	public boolean supportsNull(TypeMirror tm) {
		return false;
	}

	@Override
	public String getAddMethod() {
		return "put";
	}
	
	@Override
	public TypeMirror getIterableType(TypeMirror tm) {
		Elements elements = UtilsProvider.getElements();
		TypeElement typeElement = elements.getTypeElement(Map.Entry.class.getCanonicalName());
		
		DeclaredType dt = (DeclaredType)tm;
		Types types = UtilsProvider.getTypes();
		return types.getDeclaredType(typeElement, dt.getTypeArguments().get(0), dt.getTypeArguments().get(1));
	}

	@Override
	public TypeMirror getIterableImplementationType(TypeMirror tm) {
		Elements elements = UtilsProvider.getElements();
		TypeElement typeElement = elements.getTypeElement(LinkedHashMap.class.getCanonicalName());
		DeclaredType dt = (DeclaredType)tm;
		Types types = UtilsProvider.getTypes();
		return types.getDeclaredType(typeElement, dt.getTypeArguments().get(0), dt.getTypeArguments().get(1));
	}

	@Override
	public TypeMirror getIterableDeclarationType(TypeMirror tm) {
		DeclaredType dt = (DeclaredType)tm;
		TypeElement asElement = (TypeElement) (dt.asElement());
		Types types = UtilsProvider.getTypes();
		return types.getDeclaredType(asElement, dt.getTypeArguments().get(0), dt.getTypeArguments().get(1));
	}

}
