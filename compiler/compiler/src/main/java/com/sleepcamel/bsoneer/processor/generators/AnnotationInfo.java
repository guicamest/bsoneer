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

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;

import com.google.common.base.Strings;
import com.sleepcamel.bsoneer.processor.domain.UtilsProvider;
import com.sleepcamel.bsoneer.processor.util.Util;

public class AnnotationInfo {

	private TypeMirror tm;
	private String idProperty;
	private boolean keepNonIdProperty;
	private TypeMirror idGeneratorType;
	private boolean customGenerator;
	
	private static final TypeMirror DEFAULT_RETURN_TYPE = UtilsProvider.getElements().getTypeElement(Object.class.getCanonicalName()).asType();
	
	private List<TypeVariable> typeVariables = new ArrayList<TypeVariable>();
	private List<WildcardType> typeWildcards = new ArrayList<WildcardType>();
	private TypeMirror generatorReturnTypeMirror = DEFAULT_RETURN_TYPE;

	public AnnotationInfo(TypeMirror tm, String idProperty, boolean keepNonIdProperty,
			TypeMirror idGeneratorType, boolean customGenerator) {
		this.tm = tm;
		if (tm.getKind() != TypeKind.DECLARED) {
			throw new RuntimeException(tm + " should be declared");
		}
		DeclaredType dt = (DeclaredType) tm;
		List<? extends TypeMirror> typeArguments = dt.getTypeArguments();
		if (typeArguments != null) {
			for (TypeMirror tms : typeArguments) {
				if (tms instanceof TypeVariable) {
					typeVariables.add((TypeVariable) tms);
				} else if (tms instanceof WildcardType) {
					typeWildcards.add((WildcardType) tms);
				}
			}
		}
		this.idGeneratorType = idGeneratorType;
		this.customGenerator = customGenerator;
		this.idProperty = Strings.nullToEmpty(idProperty).trim();
		this.keepNonIdProperty = keepNonIdProperty;
		analyzeGenerator();
	}

	private void analyzeGenerator() {
		if (!hasCustomGenerator()) {
			return ;
		}
		Types types = UtilsProvider.getTypes();
		TypeElement bsoneerIdGenerator = UtilsProvider.getElements()
				.getTypeElement("com.sleepcamel.bsoneer.IdGenerator");
		
		DeclaredType idGeneratorType = (DeclaredType) getIdGeneratorType();
		if ( types.isAssignable(types.erasure(idGeneratorType),
				types.erasure(bsoneerIdGenerator.asType())) ){
			resolveGeneratorReturnType(bsoneerIdGenerator, 
					idGeneratorType,
					types);
		}
	}

	private void resolveGeneratorReturnType(TypeElement bsoneerIdGenerator,
			DeclaredType declaredGenerator, Types types) {
		
		List<ExecutableElement> methodsIn = ElementFilter.methodsIn(bsoneerIdGenerator.getEnclosedElements());
		if ( methodsIn != null ){
			for(ExecutableElement execElem:methodsIn){
				if (execElem.getSimpleName().contentEquals("generate") && !execElem.getParameters().isEmpty()) {
					ExecutableType asMemberOf = (ExecutableType) types.asMemberOf(declaredGenerator, execElem);
					TypeMirror returnType = asMemberOf.getReturnType();
					if (TypeKind.DECLARED.equals(returnType.getKind())) {
						generatorReturnTypeMirror = returnType;
					}
					return;
				}
			}
		}
	}

	public boolean hasCustomId() {
		return !idProperty.isEmpty();
	}

	public boolean hasCustomGenerator() {
		return customGenerator;
	}

	public String getIdProperty() {
		return idProperty;
	}

	public boolean isKeepNonIdProperty() {
		return keepNonIdProperty;
	}

	public TypeMirror getIdGeneratorType() {
		return idGeneratorType;
	}

	public TypeMirror getType() {
		return tm;
	}

	public TypeMirror getGeneratorReturnTypeMirror() {
		return generatorReturnTypeMirror;
	}

	public String typeAsString() {
		return Util.rawTypeToString(tm, '.');
	}
	
	public boolean customGeneratorIsBsonned() {
		return generatorReturnTypeMirror != DEFAULT_RETURN_TYPE;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((typeAsString() == null) ? 0 : typeAsString().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnnotationInfo other = (AnnotationInfo) obj;
		if (typeAsString() == null) {
			if (other.typeAsString() != null)
				return false;
		} else if (!typeAsString().equals(other.typeAsString()))
			return false;
		return true;
	}

}
