package com.sleepcamel.bsoneer;

public abstract class IdGenerator<T> implements org.bson.codecs.IdGenerator {

	private T instance;

	public void setEntity(T instance) {
		this.instance = instance;
	}

	public abstract Object generate(T instance);

	public Object generate() {return generate(instance);}
}
