/*
 * Copyright (C) 2015 Sleepcamel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sleepcamel.bsoneer.tests.integration;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static com.sleepcamel.bsoneer.tests.integration.ProcessorTestUtils.bsoneerProcessors;
import static org.truth0.Truth.ASSERT;

import java.util.Arrays;

import javax.tools.JavaFileObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import com.sleepcamel.bsoneer.processor.BsonProcessor;

@RunWith(JUnit4.class)
final public class BsonGeneratorTest {

	@Test
	public void errorBsonneIsInterface() {
		JavaFileObject sourceFile = JavaFileObjects.forSourceString(
				"Person",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"@Bsonee", "interface Person {",
						"  public void setBla();", "}"));
		ASSERT.about(javaSource()).that(sourceFile)
				.processedWith(bsoneerProcessors()).failsToCompile()
				.withErrorContaining(BsonProcessor.IT_IS_NOT_A_CLASS);
	}

	@Test
	public void errorBsonneNoDefaultConstructor() {
		JavaFileObject sourceFile = JavaFileObjects.forSourceString(
				"Person",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"@Bsonee", "class Person {", "  Person(long u){}",
						"  int a;", "}"));
		ASSERT.about(javaSource()).that(sourceFile)
				.processedWith(bsoneerProcessors()).failsToCompile()
				.withErrorContaining(BsonProcessor.NO_DEFAULT_CONSTRUCTOR);
	}
	
	@Test
	public void errorBsonnePrivateDefaultConstructor() {
		JavaFileObject sourceFile = JavaFileObjects.forSourceString(
				"Person",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"@Bsonee", "class Person {", "  private Person(){}",
						"  int a;", "}"));
		ASSERT.about(javaSource()).that(sourceFile)
				.processedWith(bsoneerProcessors()).failsToCompile()
				.withErrorContaining(BsonProcessor.NO_DEFAULT_CONSTRUCTOR);
	}

	@Test
	public void okBsonneProtectedDefaultConstructorAndOther() {
		JavaFileObject sourceFile = JavaFileObjects.forSourceString(
				"Person",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"@Bsonee", "class Person {", "  protected Person(){}",
						"  public Person(int e){}", "  int a;", "}"));
		ASSERT.about(javaSource()).that(sourceFile)
				.processedWith(bsoneerProcessors()).compilesWithoutError();

		sourceFile = JavaFileObjects.forSourceString(
				"Person",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"@Bsonee", "class Person {",
						"  public Person(int e){}", "  protected Person(){}",
						"  int a;", "}"));
		ASSERT.about(javaSource()).that(sourceFile)
				.processedWith(bsoneerProcessors()).compilesWithoutError();
	}

	@Test
	public void annotationWithSameTypeAndWithoutAnnotationCompilationsAreEqual() {
		JavaFileObject sourceFile = JavaFileObjects.forSourceString(
				"Person",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"@Bsonee", "class Person {", "  protected Person(){}",
						"  public Person(int e){}", "  int a;", "}"));
		JavaFileObject sourceFileWithAnnotation = JavaFileObjects.forSourceString(
				"Person",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"@Bsonee(Person.class)", "class Person {", "  protected Person(){}",
						"  public Person(int e){}", "  int a;", "}"));
		
		ASSERT.about(javaSource()).that(sourceFile)
				.processedWith(bsoneerProcessors()).compilesWithoutError();
		ASSERT.about(javaSource()).that(sourceFileWithAnnotation)
				.processedWith(bsoneerProcessors()).compilesWithoutError();
	}

	@Test
	public void cannotUseIdPropertyWithIdGenerator_fail() {
		JavaFileObject idGeneratorFile = JavaFileObjects.forSourceString(
				"CustomIdGenerator",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"import org.bson.codecs.IdGenerator;",
						"class CustomIdGenerator implements IdGenerator {",
						"  public CustomIdGenerator(){}",
						"public Object generate(){return null;}",
						"}"));
		
		JavaFileObject sourceFile = JavaFileObjects.forSourceString(
				"Person",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"@Bsonee(id=\"a\", idGenerator=CustomIdGenerator.class)", "class Person {", "  protected Person(){}",
						"  int a;", "}"));
		ASSERT.about(javaSources()).that(Arrays.asList(idGeneratorFile, sourceFile))
				.processedWith(bsoneerProcessors()).failsToCompile()
				.withErrorContaining(BsonProcessor.CANNOT_USE_ID_PROPERTY_AND_ID_GENERATOR_AT_THE_SAME_TIME);
	}
	
	@Test
	public void cannotUseIdGeneratorWithoutPublicDefaultConstructor_fail() {
		JavaFileObject idGeneratorFile = JavaFileObjects.forSourceString(
				"CustomIdGenerator",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"import org.bson.codecs.IdGenerator;",
						"class CustomIdGenerator implements IdGenerator {",
						"  public CustomIdGenerator(String a){}",
						"  protected CustomIdGenerator(){}",
						"public Object generate(){return null;}",
						"}"));
		
		JavaFileObject sourceFile = JavaFileObjects.forSourceString(
				"Person",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"@Bsonee(idGenerator=CustomIdGenerator.class)", "class Person {", "  protected Person(){}",
						"  int a;", "}"));
		ASSERT.about(javaSources()).that(Arrays.asList(idGeneratorFile, sourceFile))
				.processedWith(bsoneerProcessors()).failsToCompile()
				.withErrorContaining(BsonProcessor.ID_GENERATOR_MUST_HAVE_DEFAULT_PUBLIC_CONSTRUCTOR);
	}
	
	@Test
	public void idPropertyNotFound_fail() {
		JavaFileObject sourceFile = JavaFileObjects.forSourceString(
				"Person",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"@Bsonee(id=\"b\")", "class Person {", "  protected Person(){}",
						"  int a;", "}"));
		ASSERT.about(javaSource()).that(sourceFile)
				.processedWith(bsoneerProcessors()).failsToCompile()
				.withErrorContaining(BsonProcessor.ID_PROPERTY_NOT_FOUND);
	}
}
