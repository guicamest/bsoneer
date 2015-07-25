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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class ElementExt {

	static public boolean isStatic(Element e) {
		return e.getModifiers().contains(Modifier.STATIC);
	}
	
	private static boolean isTransient(Element e) {
		return e.getModifiers().contains(Modifier.TRANSIENT);
	}

	static public boolean isField(Element e) {
		return e.getKind().equals(ElementKind.FIELD);
	}

	static public boolean isMethod(Element e) {
		return e.getKind().equals(ElementKind.METHOD);
	}

	static public boolean isGetter(Element e) {
		if (!isMethod(e)) {
			return false;
		}

		ExecutableElement ex = (ExecutableElement) e;

		if (ex.getParameters().size() > 0) {
			return false;
		}

		if (ex.getReturnType().getKind().equals(TypeKind.VOID)) {
			return false;
		}

		String name = getName(e);
		boolean get = name.startsWith("get") && name.length() > 3
				&& Character.isUpperCase(name.charAt(3));
		boolean is = name.startsWith("is") && name.length() > 2
				&& Character.isUpperCase(name.charAt(2));

		return get || is;
	}

	static public boolean isSetter(Element e) {
		if (!isMethod(e)) {
			return false;
		}

		ExecutableElement ex = (ExecutableElement) e;

		if (ex.getParameters().size() != 1) {
			return false;
		}

		// TODO, might be a builder setter, allow it to return enclosing type 
		if (!ex.getReturnType().getKind().equals(TypeKind.VOID)) {
			return false;
		}

		String name = getName(e);
		return name.startsWith("set") && name.length() > 3
				&& Character.isUpperCase(name.charAt(3));
	}

	static public boolean isProperty(Element e) {
		return !isStatic(e) && !isTransient(e) && (isField(e) || isGetter(e) || isSetter(e));
	}
	
	static public String getPropertyName(Element e) {
		String name = getName(e);
		if (isField(e)) {
			return name;
		} else if (isGetter(e) || isSetter(e)) {
			int cut = name.startsWith("is") ? 2 : 3;
			String prefix = name.substring(cut, cut + 1).toLowerCase();
			String suffix = name.substring(cut + 1);
			return prefix + suffix;
		} else {
			throw new IllegalStateException();
		}
	}

	static public TypeMirror getPropertyType(Element e) {
		if (isField(e)) {
			return e.asType();
		} else if (isGetter(e) || isSetter(e)) {
			return ((ExecutableElement) e).getReturnType();
		} else {
			throw new IllegalStateException();
		}
	}

	static public String getName(Element e) {
		return e.getSimpleName().toString();
	}

	static public String resolveType(Element e) {
		switch (e.getKind()) {
		case FIELD:
			return e.asType().accept(new TypeResolver(), null);
		case METHOD:
			return ((ExecutableElement) e).getReturnType().accept(
					new TypeResolver(), null);
		default:
			throw new IllegalArgumentException(
					"Can only resolve types of fields or methods. Unsupported elment: "
							+ e);
		}
	}

	static public boolean hasAnnotation(Element e, String annotationType) {
		for (AnnotationMirror mirror : e.getAnnotationMirrors()) {
			String typeName = ((TypeElement) mirror.getAnnotationType()
					.asElement()).getQualifiedName().toString();
			if (typeName.equals(annotationType)) {
				return true;
			}
		}
		return false;
	}

	static public Visibility getVisibility(Element e) {
		if (e.getModifiers().contains(Modifier.PUBLIC)) {
			return Visibility.PUBLIC;
		} else if (e.getModifiers().contains(Modifier.PROTECTED)) {
			return Visibility.PROTECTED;
		} else if (e.getModifiers().contains(Modifier.PRIVATE)) {
			return Visibility.PRIVATE;
		} else {
			return Visibility.DEFAULT;
		}
	}

	public static boolean isTopLevel(Element e) {
		return e.getEnclosingElement().getKind().equals(ElementKind.PACKAGE);
	}
}
