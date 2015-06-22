package com.sleepcamel.bsoneer.processor.generators;

import javax.lang.model.type.TypeMirror;

class VarInfo {

	private String name;
	private String method;
	private TypeMirror typeMirror;

	public VarInfo(String name, String methodName, TypeMirror typeMirror) {
		this.name = name;
		this.method = methodName;
		this.typeMirror = typeMirror;
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
	public void setName(String name) {
		this.name = name;
	}
	public boolean isMethod() {
		return !method.equals(name);
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
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

	public TypeMirror getTypeMirror() {
		return typeMirror;
	}
}
