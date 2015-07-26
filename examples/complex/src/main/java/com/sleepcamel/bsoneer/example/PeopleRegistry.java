package com.sleepcamel.bsoneer.example;

import java.util.Date;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sleepcamel.bsoneer.Bsonee;

@Bsonee(value=Person.class)
public class PeopleRegistry {

	public static void main(String[] args) {
		MongoClient mongoClient = new MongoClient();
		MongoDatabase database = mongoClient.getDatabase("people-registry-example");

		MongoCollection<Person> collection = BsoneeCodecRegistry.to(database.getCollection("people", Person.class));
		try{
			Person oldJohnny = new Person("John", "Doe", new Date(), GrowthStatus.ALIVE);
			oldJohnny.hs.add("Some");
			oldJohnny.hs.add("Thing");
			collection.insertOne(oldJohnny);

			System.out.println("We have " + collection.count() + " person(s) registered");
			collection.find().forEach(new Block<Person>() {
				public void apply(Person t) {
					System.out.println("Registered " + t);
				}
			});

			Person replacement = new Person("John", "Dead", new Date(), GrowthStatus.DEAD);
			replacement.hs.add("Hey");
			replacement.hs.add("You");
			collection.findOneAndReplace(BsoneeBson.bson(oldJohnny),replacement);

			System.out.println("We have " + collection.count() + " person(s) registered");

			collection.find().forEach(new Block<Person>() {
				public void apply(Person t) {
					System.out.println("Registered " + t);
				}
			});
		}catch(Exception e) {
			System.out.println("Example Failed");
			e.printStackTrace();
		}finally{
			collection.drop();
			database.drop();
			mongoClient.close();
		}
	}
}
