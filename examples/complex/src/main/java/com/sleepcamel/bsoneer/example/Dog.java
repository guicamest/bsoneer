package com.sleepcamel.bsoneer.example;

import java.util.List;

import com.sleepcamel.bsoneer.Bsonee;

@Bsonee
public class Dog<Q,U> {

	Q q;
	U u;

	// TODO Do some magic to handle this case in BullDog
	List<U> us;
}
