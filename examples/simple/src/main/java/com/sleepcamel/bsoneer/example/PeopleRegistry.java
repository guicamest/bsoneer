package com.sleepcamel.bsoneer.example;

import java.util.Date;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sleepcamel.bsoneer.Bsonee;

public class PeopleRegistry {

	@Bsonee({Person.class})
	public static void main(String[] args) {
		MongoClient mongoClient = new MongoClient();
		MongoDatabase database = mongoClient.getDatabase("people-registry-example");
		
		MongoCollection<Person> collection = BsoneeCodecRegistry.to(database.getCollection("people", Person.class));
		try{
			Person oldJohnny = new Person("John", "Doe", new Date(), GrowthStatus.ALIVE);
			collection.insertOne(oldJohnny);
			
			System.out.println("We have "+collection.count()+" person(s) registered");
			collection.find().forEach(new Block<Person>() {
				public void apply(Person t) {
					System.out.println("Registered "+t);
				}
			});
			
			collection.findOneAndReplace(BsoneeBson.bson(oldJohnny),new Person("Johnny", "Dead", new Date(), GrowthStatus.DEAD));
			
			System.out.println("We have "+collection.count()+" person(s) registered");
			
			collection.find().forEach(new Block<Person>() {
				public void apply(Person t) {
					System.out.println("Registered "+t);
				}
			});
		}catch(Exception e){
			System.out.println("Example Failed");
			e.printStackTrace();
		}finally{
			collection.drop();
			database.drop();
			mongoClient.close();
		}
	}
}
