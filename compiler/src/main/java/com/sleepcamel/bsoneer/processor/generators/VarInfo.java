package com.sleepcamel.bsoneer.processor.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.type.TypeMirror;

class VarInfo {

	final private String name;
	final private String method;
	final private TypeMirror typeMirror;
	final private boolean accessViaProperty;
	private List<String> bsonNames;
	final private TypeMirror varSource;

	public VarInfo(TypeMirror varSource, String name, String methodName, TypeMirror typeMirror,
			boolean accessViaProperty, String bsonName, String ... otherBsonNames) {
		this.varSource = varSource;
		this.name = name;
		this.method = methodName;
		this.typeMirror = typeMirror;
		this.bsonNames = new ArrayList<String>();
		bsonNames.add(bsonName);
		if (otherBsonNames != null) {
			bsonNames.addAll(Arrays.asList(otherBsonNames));
		}
		this.accessViaProperty = accessViaProperty;
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
	public boolean isMethod() {
		return !accessViaProperty;
	}

	public String getMethod() {
		return method;
	}

	public TypeMirror getTypeMirror() {
		return typeMirror;
	}

	public String getBsonName() {
		return bsonNames.get(0);
	}

	public List<String> getBsonNames() {
		return bsonNames;
	}

	public TypeMirror getVarSource() {
		return varSource;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		VarInfo other = (VarInfo) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
