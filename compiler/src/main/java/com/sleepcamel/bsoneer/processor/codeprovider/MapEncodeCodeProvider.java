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
import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.sleepcamel.bsoneer.processor.domain.Property;
import com.sleepcamel.bsoneer.processor.util.Util;
import com.sleepcamel.bsoneer.processor.util.UtilsProvider;
import com.squareup.javapoet.CodeBlock.Builder;

public class MapEncodeCodeProvider implements EncodeCodeProvider {

	@Override
	public boolean applies(Property property) {
		return applies(property.getResolvedType());
	}
	
	@Override
	public boolean applies(TypeMirror tm) {
		return Util.isJavaMap(tm);
	}
	
	public void putEncodeCode(Builder cb, TypeMirror iterableTypeMirror, Collection<EncodeCodeProvider> allProviders, String varName) {
		Elements elements = UtilsProvider.getElements();
		TypeElement typeElement = elements.getTypeElement(Map.Entry.class.getCanonicalName());
		
		DeclaredType dt = (DeclaredType)iterableTypeMirror;
		Types types = UtilsProvider.getTypes();
		DeclaredType declaredType = types.getDeclaredType(typeElement, dt.getTypeArguments().get(0), dt.getTypeArguments().get(1));
		
		typeElement = UtilsProvider.getElements().getTypeElement(Set.class.getCanonicalName());
		declaredType = UtilsProvider.getTypes().getDeclaredType(typeElement, declaredType);
		
		// Rely on set encoder
		for(EncodeCodeProvider ecp:allProviders){
			if (ecp.applies(declaredType)) {
				ecp.putEncodeCode(cb, declaredType, allProviders, varName+".entrySet()");
				break;
			}
		}
	}

}
