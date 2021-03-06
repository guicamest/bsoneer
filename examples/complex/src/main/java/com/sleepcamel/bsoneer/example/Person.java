package com.sleepcamel.bsoneer.example;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.BasicBSONList;

public class Person {

	String name;
	private String lastName;
	private Date date;
	private GrowthStatus growth;
	String defaultString;
	protected String protectedString;
	public String publicString;
	int age;
	int ageBis;
	double height;
	boolean male;
	Short tee;
	Integer ing;
	List<String> ls;
	ArrayList<String> als;
	Set<String> hs = new HashSet<String>();
	private HashSet<String> hss;
	BasicBSONList bbl;
	String[] strings;
	double[] doubles;
	double[][][] doublesss;

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

	public Date getDate() {
		return date;
	}

	public GrowthStatus getGrowth() {
		return growth;
	}

	public void getBla() {
	}

	private void getSmth() {
	}

	public String setCo() {
		return "";
	}

	public String setCow(String o) {
		return o;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setGrowth(GrowthStatus growth) {
		this.growth = growth;
	}

	public HashSet<String> getHss() {
		return hss;
	}

	public void setHss(HashSet<String> hss) {
		this.hss = hss;
	}

	@Override
	public String toString() {
		return "Person [name=" + name + ", lastName=" + lastName + ", date="
				+ date + ", growth=" + growth + ", defaultString="
				+ defaultString + ", protectedString=" + protectedString
				+ ", publicString=" + publicString + ", age=" + age
				+ ", ageBis=" + ageBis + ", height=" + height + ", male="
				+ male + ", tee=" + tee + ", ing=" + ing + ", ls=" + ls
				+ ", als=" + als + ", hs=" + hs + ", hss=" + hss + ", bbl="
				+ bbl + "]";
	}

}
