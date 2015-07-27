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

@RunWith(JUnit4.class)
public final class CodecGenerationTest {
	@Test
	public void basicCodec() {
		JavaFileObject enumFile = JavaFileObjects.forSourceString(
				"SomeEnum",
				Joiner.on("\n").join("enum SomeEnum {", "  HI, BYE", "}"));
		
		JavaFileObject sourceFile = JavaFileObjects.forSourceString(
				"Person",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"@Bsonee", "class Person {", "  SomeEnum se;", "}"));

		JavaFileObject expectedEnumCodec = JavaFileObjects
				.forSourceString(
						"SomeEnumBsoneeCodec",
						Joiner.on("\n")
								.join("// Code generated by bsoneer-compiler.  Do not edit.",
										"//",
										"import java.lang.Class;",
										"import java.lang.Override;",
										"import java.lang.SuppressWarnings;",
										"import javax.annotation.Generated;",
										"import org.bson.BsonReader;",
										"import org.bson.BsonWriter;",
										"import org.bson.codecs.Codec;",
										"import org.bson.codecs.DecoderContext;",
										"import org.bson.codecs.EncoderContext;",
										"",
										"/**",
										" * Code generated by bsoneer-compiler.  Do not edit.",
										" */",
										"@Generated(\"com.sleepcamel.bsoneer.processor.BsonProcessor\")",
										"public class SomeEnumBsoneeCodec implements Codec<SomeEnum> {",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	public Class<SomeEnum> getEncoderClass() {",
										"		return SomeEnum.class;",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	@Override",
										"	@SuppressWarnings({\"unchecked\", \"rawtypes\"})",
										"	public void encode(BsonWriter writer, SomeEnum value, EncoderContext encoderContext) {",
										"		writer.writeString(value.name());",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	@Override",
										"	@SuppressWarnings({\"unchecked\", \"rawtypes\"})",
										"	public SomeEnum decode(BsonReader reader, DecoderContext decoderContext) {",
										"		return SomeEnum.valueOf(reader.readString());",
										"	}",
										"	", "}"));
		
		JavaFileObject expectedPersonCodec = JavaFileObjects
				.forSourceString(
						"PersonBsoneeCollectibleCodec",
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
										"public class PersonBsoneeCollectibleCodec extends BaseBsoneerCodec<Person> {",
										"	public PersonBsoneeCollectibleCodec(final CodecRegistry registry) {",
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
										"		if (value.se != null) {",
										"			writer.writeName(\"se\");",
										"			writer.writeString(value.se.name());",
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
										"		settersByName.put(\"se\",new SeSetter());",
										"	}",
										"",
										"	class SeSetter implements BsoneeBaseSetter<Person> {",
										"		public void set(Person instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.se = SomeEnum.valueOf(reader.readString());",
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
										"			return (Codec<T>) new PersonBsoneeCollectibleCodec(registry);",
										"		}",
										"		if (clazz == SomeEnum.class) {",
										"			return (Codec<T>) new SomeEnumBsoneeCodec();",
										"		}",
										"		return null;", "	}", "}"));

		ASSERT.about(javaSources()).that(Arrays.asList(enumFile, sourceFile))
				.processedWith(bsoneerProcessors()).compilesWithoutError()
				.and().generatesSources(expectedEnumCodec, expectedPersonCodec, expectedCodecProvider);
	}

}