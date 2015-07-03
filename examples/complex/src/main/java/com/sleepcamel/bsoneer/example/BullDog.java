package com.sleepcamel.bsoneer.example;

import com.sleepcamel.bsoneer.Bsonee;

@Bsonee(idGenerator=BullDogIdGenerator.class)
public class BullDog extends Dog<String,String> {

	String bullString;
}
