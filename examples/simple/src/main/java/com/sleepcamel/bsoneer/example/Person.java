package com.sleepcamel.bsoneer.example;

import java.util.Date;

public class Person {

	String name;
	private String lastName;
	private Date date;
	private GrowthStatus growth;

	protected Person() {}

	public Person(String name, String lastName, Date date, GrowthStatus growth) {
		this.name = name;
		this.lastName = lastName;
		this.date = date;
		this.growth = growth;
	}

	public String getName() {
		return name;
	}

	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Date getDate() {
		return date;
	}

	public GrowthStatus getGrowth() {
		return growth;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setGrowth(GrowthStatus growth) {
		this.growth = growth;
	}
	
	@Override
	public String toString() {
		return name + " " + lastName;
	}

}
