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
package com.sleepcamel.bsoneer.processor.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import com.google.common.base.Optional;
import com.sleepcamel.bsoneer.processor.generators.RawTypeMappings;

public class Bean {
	private final TypeElement element;

	private final Map<String, Property> properties = new LinkedHashMap<String, Property>();

	private Bean superclass;

	private Map<TypeMirror, TypeMirror> rawTypeMappings = new HashMap<TypeMirror, TypeMirror>();

	public Bean(TypeElement element) {
		this(element, ((DeclaredType) element.getSuperclass()).getTypeArguments(), new HashMap<TypeMirror, TypeMirror>());
	}
	
	private Bean(TypeElement element, Map<TypeMirror, TypeMirror> rawTypeMappings) {
		this(element, ((DeclaredType) element.getSuperclass()).getTypeArguments(), rawTypeMappings);
	}
	
	private Bean(TypeElement element, List<? extends TypeMirror> declaredTypeArguments, Map<TypeMirror, TypeMirror> rawTypeMappings) {
		this.element = element;
		this.rawTypeMappings = rawTypeMappings;
		Optional<TypeElement> accept = element.getSuperclass().accept(new ElementResolver(), null);
		if (accept != null && accept.isPresent()){
			TypeElement typeElement = accept.get();
			List<? extends TypeMirror> rawTypeArguments = ((DeclaredType) typeElement.asType()).getTypeArguments();
			superclass = new Bean(typeElement, RawTypeMappings.getMappings(rawTypeArguments, declaredTypeArguments));
		}
	}
	
	public void resolveHierarchyRawTypes() {
		if ( superclass != null ){
			for(Entry<TypeMirror, TypeMirror> mapping:superclass.rawTypeMappings.entrySet()){
				TypeMirror value = mapping.getValue();
				if ( rawTypeMappings.containsKey(value) ){
					superclass.rawTypeMappings.put(mapping.getKey(), rawTypeMappings.get(value));
				}
			}
			superclass.resolveHierarchyRawTypes();
		}
	}

	public TypeElement getElement() {
		return element;
	}

	public Map<String, Property> getProperties() {
		return properties;
	}
	
	public List<Property> getOwnAndInheritedProperties() {
		List<Property> list = new ArrayList<Property>();
		list.addAll(properties.values());
		if(superclass != null){
			list.addAll(superclass.getOwnAndInheritedProperties());
		}
		return list;
	}

	public String getName() {
		return element.getQualifiedName().toString();
	}

	public Visibility getVisibility() {
		return Visibility.of(element);
	}

	public static final <T> Collection<T> copyValues(Map<?, T> map) {
		return new ArrayList<T>(map.values());
	}

	public void remove(Property property) {
		Property removed = properties.remove(property.getName());
		if (removed == null) {
			throw new IllegalStateException(
					"Attempted to remove property that was not part of the bean. Property name: "
							+ property.getName());
		}
	}

	public Optional<Bean> getSuperclass() {
		return Optional.fromNullable(superclass);
	}

	public void setSuperclass(Bean superclass) {
		this.superclass = superclass;
	}
	
	public Property getProperty(String idProperty) {
		return properties.get(idProperty);
	}

	public Property property(String name) {
		Property property = properties.get(name);
		if (property == null) {
			property = new Property(name);
			properties.put(name, property);
		}
		return property;
	}

	public void resolveRawProperties() {
		for(Property p:properties.values()){
			p.resolveType(rawTypeMappings);
		}
		if ( superclass != null ){
			superclass.resolveRawProperties();
		}
	}

}
