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
import static com.sleepcamel.bsoneer.tests.integration.ProcessorTestUtils.bsoneerProcessors;
import static org.truth0.Truth.ASSERT;

import javax.tools.JavaFileObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;

@RunWith(JUnit4.class)
public final class BsonCodecGenerationTest {
	@Test
	public void basicCodec() {
		JavaFileObject sourceFile = JavaFileObjects.forSourceString(
				"Person",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"@Bsonee", "class Person {", "  int a;", "}"));

		JavaFileObject expectedCodec = JavaFileObjects
				.forSourceString(
						"PersonBsoneeCodec",
						Joiner.on("\n")
								.join("// Code generated by bsoneer-compiler.  Do not edit.",
										"//",
										"import com.sleepcamel.bsoneer.BaseBsoneerCodec;",
										"import com.sleepcamel.bsoneer.BsoneeBaseSetter;",
										"import java.lang.Class;",
										"import java.lang.Override;",
										"import java.lang.SuppressWarnings;",
										"import javax.annotation.Generated;",
										"import org.bson.BsonReader;",
										"import org.bson.BsonWriter;",
										"import org.bson.codecs.Codec;",
										"import org.bson.codecs.DecoderContext;",
										"import org.bson.codecs.EncoderContext;",
										"import org.bson.codecs.ObjectIdGenerator;",
										"import org.bson.codecs.configuration.CodecRegistry;",
										"",
										"/**",
										" * Code generated by bsoneer-compiler.  Do not edit.",
										" */",
										"@Generated(\"com.sleepcamel.bsoneer.processor.BsonProcessor\")",
										"public class PersonBsoneeCodec extends BaseBsoneerCodec<Person> {",
										"	public PersonBsoneeCodec(final CodecRegistry registry) {",
										"		super(registry, new ObjectIdGenerator());",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	public Class<Person> getEncoderClass() {",
										"		return Person.class;",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	@Override",
										"	@SuppressWarnings({\"unchecked\", \"rawtypes\"})",
										"	protected void encodeVariables(BsonWriter writer, Person value, EncoderContext encoderContext) {",
										"		if (encoderContext.isEncodingCollectibleDocument()) {",
										"			writer.writeName(\"_id\");",
										"			Object vid = idGenerator.generate();",
										"			Codec cid = registry.get(vid.getClass());",
										"			encoderContext.encodeWithChildContext(cid, writer, vid);",
										"		}",
										"		writer.writeName(\"a\");",
										"		writer.writeInt32(value.a);",
										"		super.encodeVariables(writer,value,encoderContext);",
										"	}",
										"",
										"	@Override",
										"	protected Person instantiate() {",
										"		return new Person();",
										"	}",
										"",
										"	protected void setupSetters() {",
										"		settersByName.put(\"a\",new ASetter());",
										"	}",
										"",
										"	class ASetter implements BsoneeBaseSetter<Person> {",
										"		public void set(Person instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.a = reader.readInt32();",
										"		}", "	}", "", "}"));

		JavaFileObject expectedCodecProvider = JavaFileObjects
				.forSourceString(
						"BsoneeCodecProvider",
						Joiner.on("\n")
								.join("// Code generated by bsoneer-compiler.  Do not edit.",
										"//",
										"import java.lang.Class;",
										"import java.lang.Override;",
										"import java.lang.SuppressWarnings;",
										"import javax.annotation.Generated;",
										"import org.bson.codecs.Codec;",
										"import org.bson.codecs.configuration.CodecProvider;",
										"import org.bson.codecs.configuration.CodecRegistry;",
										"",
										"/**",
										" * Code generated by bsoneer-compiler.  Do not edit.",
										" */",
										"@Generated(\"com.sleepcamel.bsoneer.processor.BsonProcessor\")",
										"public class BsoneeCodecProvider implements CodecProvider {",
										"	public BsoneeCodecProvider() {",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	@Override",
										"	@SuppressWarnings({\"unchecked\"})",
										"	public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {",
										"		if (clazz == Person.class) {",
										"			return (Codec<T>) new PersonBsoneeCodec(registry);",
										"		}", "		return null;", "	}", "}"));

		ASSERT.about(javaSource()).that(sourceFile)
				.processedWith(bsoneerProcessors()).compilesWithoutError()
				.and().generatesSources(expectedCodec, expectedCodecProvider);
	}

	@Test
	public void okBsonneCustomId() {
		JavaFileObject sourceFile = JavaFileObjects.forSourceString(
				"Person",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"@Bsonee(id=\"a\")", "class Person {", "  protected Person() {}",
						"  public Person(int e) {}", "  int a;", "}"));

		JavaFileObject expectedCodec = JavaFileObjects
				.forSourceString(
						"PersonBsoneeCodec",
						Joiner.on("\n")
								.join("// Code generated by bsoneer-compiler.  Do not edit.",
										"//",
										"import com.sleepcamel.bsoneer.BaseBsoneerCodec;",
										"import com.sleepcamel.bsoneer.BsoneeBaseSetter;",
										"import java.lang.Class;",
										"import java.lang.Override;",
										"import java.lang.SuppressWarnings;",
										"import javax.annotation.Generated;",
										"import org.bson.BsonReader;",
										"import org.bson.BsonWriter;",
										"import org.bson.codecs.DecoderContext;",
										"import org.bson.codecs.EncoderContext;",
										"import org.bson.codecs.ObjectIdGenerator;",
										"import org.bson.codecs.configuration.CodecRegistry;",
										"",
										"/**",
										" * Code generated by bsoneer-compiler.  Do not edit.",
										" */",
										"@Generated(\"com.sleepcamel.bsoneer.processor.BsonProcessor\")",
										"public class PersonBsoneeCodec extends BaseBsoneerCodec<Person> {",
										"	public PersonBsoneeCodec(final CodecRegistry registry) {",
										"		super(registry, new ObjectIdGenerator());",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	public Class<Person> getEncoderClass() {",
										"		return Person.class;",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	@Override",
										"	@SuppressWarnings({\"unchecked\", \"rawtypes\"})",
										"	protected void encodeVariables(BsonWriter writer, Person value, EncoderContext encoderContext) {",
										"		if (encoderContext.isEncodingCollectibleDocument()) {",
										"			writer.writeName(\"_id\");",
										"			writer.writeInt32(value.a);",
										"		}",
										"		super.encodeVariables(writer,value,encoderContext);",
										"	}",
										"",
										"	@Override",
										"	protected Person instantiate() {",
										"		return new Person();",
										"	}",
										"",
										"	protected void setupSetters() {",
										"		settersByName.put(\"_id\",new ASetter());",
										"	}",
										"",
										"	class ASetter implements BsoneeBaseSetter<Person> {",
										"		public void set(Person instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.a = reader.readInt32();",
										"		}", "	}", "", "}"));
		ASSERT.about(javaSource()).that(sourceFile)
			.processedWith(bsoneerProcessors()).compilesWithoutError().and().generatesSources(expectedCodec);
	}

	@Test
	public void okBsonneCustomIdKeepIdProperty() {
		JavaFileObject sourceFile = JavaFileObjects.forSourceString(
				"Person",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"@Bsonee(id=\"a\", keepIdProperty=true)", "class Person {", "  protected Person() {}",
						"  public Person(int e) {}", "  int a;", "}"));
		JavaFileObject expectedCodec = JavaFileObjects
				.forSourceString(
						"PersonBsoneeCodec",
						Joiner.on("\n")
								.join("// Code generated by bsoneer-compiler.  Do not edit.",
										"//",
										"import com.sleepcamel.bsoneer.BaseBsoneerCodec;",
										"import com.sleepcamel.bsoneer.BsoneeBaseSetter;",
										"import java.lang.Class;",
										"import java.lang.Override;",
										"import java.lang.SuppressWarnings;",
										"import javax.annotation.Generated;",
										"import org.bson.BsonReader;",
										"import org.bson.BsonWriter;",
										"import org.bson.codecs.DecoderContext;",
										"import org.bson.codecs.EncoderContext;",
										"import org.bson.codecs.ObjectIdGenerator;",
										"import org.bson.codecs.configuration.CodecRegistry;",
										"",
										"/**",
										" * Code generated by bsoneer-compiler.  Do not edit.",
										" */",
										"@Generated(\"com.sleepcamel.bsoneer.processor.BsonProcessor\")",
										"public class PersonBsoneeCodec extends BaseBsoneerCodec<Person> {",
										"	public PersonBsoneeCodec(final CodecRegistry registry) {",
										"		super(registry, new ObjectIdGenerator());",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	public Class<Person> getEncoderClass() {",
										"		return Person.class;",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	@Override",
										"	@SuppressWarnings({\"unchecked\", \"rawtypes\"})",
										"	protected void encodeVariables(BsonWriter writer, Person value, EncoderContext encoderContext) {",
										"		writer.writeName(\"a\");",
										"		writer.writeInt32(value.a);",
										"		if (encoderContext.isEncodingCollectibleDocument()) {",
										"			writer.writeName(\"_id\");",
										"			writer.writeInt32(value.a);",
										"		}",
										"		super.encodeVariables(writer,value,encoderContext);",
										"	}",
										"",
										"	@Override",
										"	protected Person instantiate() {",
										"		return new Person();",
										"	}",
										"",
										"	protected void setupSetters() {",
										"		settersByName.put(\"a\",new ASetter());",
										"	}",
										"",
										"	class ASetter implements BsoneeBaseSetter<Person> {",
										"		public void set(Person instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.a = reader.readInt32();",
										"		}", "	}", "", "}"));
		ASSERT.about(javaSource()).that(sourceFile)
			.processedWith(bsoneerProcessors()).compilesWithoutError().and().generatesSources(expectedCodec);
	}

	@Test
	public void transientFieldsNotPersisted() {
		JavaFileObject sourceFile = JavaFileObjects
				.forSourceString(
						"Person",
						Joiner.on("\n").join(
								"import com.sleepcamel.bsoneer.Bsonee;",
								"@Bsonee", "class Person {",
								"  transient int a;", "}"));

		JavaFileObject expectedCodec = JavaFileObjects
				.forSourceString(
						"PersonBsoneeCodec",
						Joiner.on("\n")
								.join("// Code generated by bsoneer-compiler.  Do not edit.",
										"//",
										"import com.sleepcamel.bsoneer.BaseBsoneerCodec;",
										"import java.lang.Class;",
										"import java.lang.Override;",
										"import java.lang.SuppressWarnings;",
										"import javax.annotation.Generated;",
										"import org.bson.BsonWriter;",
										"import org.bson.codecs.Codec;",
										"import org.bson.codecs.EncoderContext;",
										"import org.bson.codecs.ObjectIdGenerator;",
										"import org.bson.codecs.configuration.CodecRegistry;",
										"",
										"/**",
										" * Code generated by bsoneer-compiler.  Do not edit.",
										" */",
										"@Generated(\"com.sleepcamel.bsoneer.processor.BsonProcessor\")",
										"public class PersonBsoneeCodec extends BaseBsoneerCodec<Person> {",
										"	public PersonBsoneeCodec(final CodecRegistry registry) {",
										"		super(registry, new ObjectIdGenerator());",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	public Class<Person> getEncoderClass() {",
										"		return Person.class;",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	@Override",
										"	@SuppressWarnings({\"unchecked\", \"rawtypes\"})",
										"	protected void encodeVariables(BsonWriter writer, Person value, EncoderContext encoderContext) {",
										"		if (encoderContext.isEncodingCollectibleDocument()) {",
										"			writer.writeName(\"_id\");",
										"			Object vid = idGenerator.generate();",
										"			Codec cid = registry.get(vid.getClass());",
										"			encoderContext.encodeWithChildContext(cid, writer, vid);",
										"		}",
										"		super.encodeVariables(writer,value,encoderContext);",
										"	}", "", "	@Override",
										"	protected Person instantiate() {",
										"		return new Person();", "	}", "",
										"	protected void setupSetters() {",
										"	}", "", "}"));

		ASSERT.about(javaSource()).that(sourceFile)
				.processedWith(bsoneerProcessors()).compilesWithoutError()
				.and().generatesSources(expectedCodec);
	}
	
	@Test
	public void bsonIsGetterCodec() {
		JavaFileObject sourceFile = JavaFileObjects.forSourceString(
				"Person",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"@Bsonee", "class Person {",
						"  private boolean smth;",
						"  public boolean isSmth(){return smth;}",
						"  public void setSmth(boolean smth){this.smth = smth;}",
						"}"));

		JavaFileObject expectedCodec = JavaFileObjects
				.forSourceString(
						"PersonBsoneeCodec",
						Joiner.on("\n")
								.join("// Code generated by bsoneer-compiler.  Do not edit.",
										"//",
										"import com.sleepcamel.bsoneer.BaseBsoneerCodec;",
										"import com.sleepcamel.bsoneer.BsoneeBaseSetter;",
										"import java.lang.Class;",
										"import java.lang.Override;",
										"import java.lang.SuppressWarnings;",
										"import javax.annotation.Generated;",
										"import org.bson.BsonReader;",
										"import org.bson.BsonWriter;",
										"import org.bson.codecs.Codec;",
										"import org.bson.codecs.DecoderContext;",
										"import org.bson.codecs.EncoderContext;",
										"import org.bson.codecs.ObjectIdGenerator;",
										"import org.bson.codecs.configuration.CodecRegistry;",
										"",
										"/**",
										" * Code generated by bsoneer-compiler.  Do not edit.",
										" */",
										"@Generated(\"com.sleepcamel.bsoneer.processor.BsonProcessor\")",
										"public class PersonBsoneeCodec extends BaseBsoneerCodec<Person> {",
										"	public PersonBsoneeCodec(final CodecRegistry registry) {",
										"		super(registry, new ObjectIdGenerator());",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	public Class<Person> getEncoderClass() {",
										"		return Person.class;",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	@Override",
										"	@SuppressWarnings({\"unchecked\", \"rawtypes\"})",
										"	protected void encodeVariables(BsonWriter writer, Person value, EncoderContext encoderContext) {",
										"		if (encoderContext.isEncodingCollectibleDocument()) {",
										"			writer.writeName(\"_id\");",
										"			Object vid = idGenerator.generate();",
										"			Codec cid = registry.get(vid.getClass());",
										"			encoderContext.encodeWithChildContext(cid, writer, vid);",
										"		}",
										"		writer.writeName(\"smth\");",
										"		writer.writeBoolean(value.isSmth());",
										"		super.encodeVariables(writer,value,encoderContext);",
										"	}",
										"",
										"	@Override",
										"	protected Person instantiate() {",
										"		return new Person();",
										"	}",
										"",
										"	protected void setupSetters() {",
										"		settersByName.put(\"smth\",new SmthSetter());",
										"	}",
										"",
										"	class SmthSetter implements BsoneeBaseSetter<Person> {",
										"		public void set(Person instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.setSmth(reader.readBoolean());",
										"		}", "	}", "", "}"));

		ASSERT.about(javaSource()).that(sourceFile)
				.processedWith(bsoneerProcessors()).compilesWithoutError()
				.and().generatesSources(expectedCodec);
	}
}
