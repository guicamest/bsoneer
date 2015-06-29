package com.sleepcamel.bsoneer.processor.generators;

import javax.lang.model.type.TypeMirror;

import com.google.common.base.Strings;
import com.sleepcamel.bsoneer.processor.util.Util;

public class AnnotationInfo {

	private TypeMirror tm;
	private String idProperty;
	private boolean keepNonIdProperty;

	public AnnotationInfo(TypeMirror tm, String idProperty, boolean keepNonIdProperty) {
		this.tm = tm;
		this.idProperty = Strings.nullToEmpty(idProperty).trim();
		this.keepNonIdProperty = keepNonIdProperty;
	}
	
	public boolean hasCustomId() {
		return !idProperty.isEmpty();
	}
	
	public String getIdProperty() {
		return idProperty;
	}
	
	public boolean isKeepNonIdProperty() {
		return keepNonIdProperty;
	}

	public TypeMirror getType() {
		return tm;
	}

	public String typeAsString(){
		return Util.rawTypeToString(tm, '.');
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
