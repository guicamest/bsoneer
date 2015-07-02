package com.sleepcamel.bsoneer.processor.generators;

import java.util.List;

import javax.lang.model.type.TypeMirror;

public class TypeMapping {

	private List<? extends TypeMirror> rawTypes;
	private List<? extends TypeMirror> declaredTypes;

	public TypeMapping(List<? extends TypeMirror> rawTypes, List<? extends TypeMirror> declaredTypes) {
		this.rawTypes = rawTypes;
		this.declaredTypes = declaredTypes;
	}

	public TypeMirror getReplacement(TypeMirror rawType) {
		for (int i = 0; i < rawTypes.size(); i++) {
			if (rawTypes.get(i).equals(rawType)) {
				return declaredTypes.get(i);
			}
		}
		return rawType;
	}

}
