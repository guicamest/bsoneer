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
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Types;

public class Property {
	private final String name;

	/**
	 * name of the field that will hold the property definition in the meta
	 * class
	 */
	private String handle;
	private Visibility visibility;
	private Element field;
	private Element getter;
	private Element setter;
	private boolean deprecated = false;

	private TypeMirror resolvedType;

	private List<String> bsonNames = new ArrayList<String>();

	public Property(String name) {
		this.name = name;
		this.handle = name;
		this.bsonNames.add(name);
	}
	
	public List<String> getBsonNames() {
		return bsonNames;
	}

	public Element getField() {
		return field;
	}

	public void setField(Element field) {
		this.field = field;
	}

	public Element getGetter() {
		return getter;
	}

	public void setGetter(Element getter) {
		this.getter = getter;
	}

	public Element getSetter() {
		return setter;
	}

	public void setSetter(Element setter) {
		this.setter = setter;
	}

	public String getName() {
		return name;
	}
	
	public String getUpperName() {
		if (name.length() > 1) {
			return name.substring(0, 1).toUpperCase()
					+ name.substring(1);
		}
		return name.toUpperCase();
	}

	public TypeElement getContainer() {
		return (TypeElement) getAccessor().getEnclosingElement();
	}

	public TypeMirror getType() {
		if ( getter != null ){
			return ((ExecutableElement)getter).getReturnType();
		}
		return field.asType();
	}
	
	public TypeMirror getResolvedType() {
		return resolvedType;
	}

	public Element getAccessor() {
		return (getter != null) ? getter : field;
	}
	
	public String getGetterCall() {
		return (getter != null) ? getter.getSimpleName().toString()+"()" : name;
	}
	
	public String getSetterCall() {
		return (setter != null) ? setter.getSimpleName().toString() : name;
	}
	
	public boolean hasSetter(){
		return getSetter() != null;
	}
	
	public boolean hasSetterAndGetter() {
		return getAccessor() == field || ( getSetter() != null && getGetter() != null );
	}

	public Visibility getVisibility() {
		if (visibility != null) {
			return visibility;
		}

		// use the most relaxed visibility
		visibility = Visibility.PRIVATE;
		if (getGetter() != null) {
			visibility = Visibility.max(visibility, Visibility.of(getGetter()));
		}
		if (getSetter() != null) {
			visibility = Visibility.max(visibility, Visibility.of(getSetter()));
		}
		if (getField() != null) {
			visibility = Visibility.max(visibility, Visibility.of(getField()));
		}

		return visibility;
	}

	public void setVisibility(Visibility visibility) {
		this.visibility = visibility;
	}

	public String getHandle() {
		return handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public boolean isDeprecated() {
		return deprecated;
	}

	public void setDeprecated(boolean deprecated) {
		this.deprecated = deprecated;
	}

	public void resolveType(Map<TypeMirror, TypeMirror> rawTypeMappings) {
		resolvedType = resolveType(getType(), rawTypeMappings);
	}

	static public TypeMirror resolveType(TypeMirror tm, Map<TypeMirror, TypeMirror> rawTypeMappings) {
		TypeMirror resolvedType = tm;
		TypeMirror typeMirror = rawTypeMappings.get(resolvedType);
		if (typeMirror != null) {
			resolvedType = typeMirror;
		} else {
			Types types = UtilsProvider.getTypes();
			if (resolvedType instanceof TypeVariable || resolvedType instanceof ErrorType) {
				resolvedType = types.getDeclaredType(UtilsProvider.getElements().getTypeElement(Object.class.getCanonicalName()));
			} else if (resolvedType instanceof ArrayType) {
				TypeMirror componentType = ((ArrayType)resolvedType).getComponentType();
				resolvedType = types.getArrayType(resolveType(componentType, rawTypeMappings));
			} else if (resolvedType instanceof DeclaredType) {
				DeclaredType dt = (DeclaredType) resolvedType;
				List<? extends TypeMirror> typeArguments = dt.getTypeArguments();
				if ( typeArguments != null && !typeArguments.isEmpty() ){
					int size = typeArguments.size();
					TypeMirror[] bakArguments = new TypeMirror[size];
					for(int i=0; i < size; i++){
						bakArguments[i] = resolveType(typeArguments.get(i), rawTypeMappings);
					}
					resolvedType = types.getDeclaredType((TypeElement)dt.asElement(), bakArguments);
				}
			}
		}
		return resolvedType;
	}
}
