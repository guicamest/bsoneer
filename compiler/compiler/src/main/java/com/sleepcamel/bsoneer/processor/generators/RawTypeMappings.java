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
package com.sleepcamel.bsoneer.processor.generators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.type.TypeMirror;

public class RawTypeMappings {

	static public Map<TypeMirror,TypeMirror> getMappings(List<? extends TypeMirror> rawTypes, List<? extends TypeMirror> declaredTypes) {
		Map<TypeMirror, TypeMirror> hashMap = new HashMap<TypeMirror,TypeMirror>();
		for (int i = 0; i < rawTypes.size(); i++) {
			hashMap.put(rawTypes.get(i), declaredTypes.get(i));
		}
		return hashMap;
	}

}
