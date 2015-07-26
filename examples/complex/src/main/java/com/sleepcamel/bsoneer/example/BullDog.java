package com.sleepcamel.bsoneer.example;

import com.sleepcamel.bsoneer.Bsonee;

@Bsonee(idGenerator=BullDogIdGenerator.class)
public class BullDog extends Dog<String,String> {

	String bullString;
	
	private boolean baby;

	public boolean isBaby() {
		return baby;
	}

	public void setBaby(boolean baby) {
		this.baby = baby;
	}
}
