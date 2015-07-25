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
package com.sleepcamel.bsoneer.processor.util;

import static java.util.Locale.ENGLISH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class NameGenerator {

	private Map<Object, String> valueToName;
	private Map<String, Integer> nameToCount;

	public NameGenerator() {
		valueToName = new IdentityHashMap<Object, String>();
		nameToCount = new HashMap<String, Integer>();
	}

	/**
	 * Clears the name cache. Should be called to near the end of the encoding
	 * cycle.
	 */
	public void clear() {
		valueToName.clear();
		nameToCount.clear();
	}

	/**
	 * Returns the root name of the class.
	 */
	@SuppressWarnings("rawtypes")
	public static String unqualifiedClassName(Class type) {
		if (type.isArray()) {
			return unqualifiedClassName(type.getComponentType()) + "Array";
		}
		String name = type.getName();
		return name.substring(name.lastIndexOf('.') + 1);
	}
	
	public static String unqualifiedClassName(TypeMirror tm) {
		if (tm.getKind() == TypeKind.ARRAY) {
			return unqualifiedClassName(((ArrayType)tm).getComponentType()) + "Array";
		}
		if (tm.getKind() == TypeKind.DECLARED && !((DeclaredType)tm).getTypeArguments().isEmpty() ){
			tm = UtilsProvider.getTypes().erasure(tm);
		}
		String name = tm.toString();
		return "_"+name.substring(name.lastIndexOf('.') + 1);
	}

	/**
	 * Returns a String which capitalizes the first letter of the string.
	 */
	public static String capitalize(String name) {
		if (name == null || name.length() == 0) {
			return name;
		}
		return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
	}

	/**
	 * Returns a unique string which identifies the object instance. Invocations
	 * are cached so that if an object has been previously passed into this
	 * method then the same identifier is returned.
	 *
	 * @param instance
	 *            object used to generate string
	 * @return a unique string representing the object
	 */
	public String instanceName(Object instance) {
		if (instance == null) {
			return "null";
		}
		if (instance instanceof Class) {
			return unqualifiedClassName((Class<?>) instance);
		} else {
			String result = valueToName.get(instance);
			if (result != null) {
				return result;
			}
			Class<?> type = instance.getClass();
			String className = unqualifiedClassName(type);

			Integer size = nameToCount.get(className);
			int instanceNumber = (size == null) ? 0 : (size).intValue() + 1;
			nameToCount.put(className, new Integer(instanceNumber));

			result = className + instanceNumber;
			valueToName.put(instance, result);
			return result;
		}
	}

	/**
	 * Returns a unique string which identifies the object instance. Invocations
	 * are cached so that if an object has been previously passed into this
	 * method then the same identifier is returned.
	 *
	 * @param instance
	 *            object used to generate string
	 * @return a unique string representing the object
	 */
	public String instanceName(TypeMirror tm) {
		if (tm == null) {
			return "null";
		}
		String result = valueToName.get(tm);
		if (result != null) {
			return result;
		}
		String className = unqualifiedClassName(tm).toLowerCase();

		Integer size = nameToCount.get(className);
		int instanceNumber = (size == null) ? 0 : (size).intValue() + 1;
		nameToCount.put(className, new Integer(instanceNumber));

		result = className + instanceNumber;
		valueToName.put(tm, result);
		return result;
	}

	public List<String> generateNames(String varName, int qty) {
		if ( qty == 1 ){
			return Arrays.asList(varName);
		}
		List<String> names = new ArrayList<String>();
		for(int i=0; i < qty; i++){
			names.add(varName+i);
		}
		return names;
	}
}
