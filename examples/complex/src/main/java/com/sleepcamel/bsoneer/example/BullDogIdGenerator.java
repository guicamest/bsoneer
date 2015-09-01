package com.sleepcamel.bsoneer.example;

import org.bson.types.ObjectId;

import com.sleepcamel.bsoneer.IdGenerator;

public class BullDogIdGenerator extends IdGenerator<BullDog, ObjectId> {

	@Override
	public ObjectId generate(BullDog instance) {
		return new ObjectId();
	}

}
