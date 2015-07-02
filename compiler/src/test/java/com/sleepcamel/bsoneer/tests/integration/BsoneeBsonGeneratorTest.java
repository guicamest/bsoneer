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
final public class BsoneeBsonGeneratorTest {

	@Test public void generationTest() {
	    JavaFileObject sourceFile = JavaFileObjects.forSourceString("Person", Joiner.on("\n").join(
	        "import com.sleepcamel.bsoneer.Bsonee;",
	        "@Bsonee",
	        "class Person {",
	        "  Person() {}",
	        "  int a;",
	        "}"));

	    JavaFileObject expectedRegistry =
	            JavaFileObjects.forSourceString("BsoneeBson", Joiner.on("\n").join(
	            		"// Code generated by bsoneer-compiler.  Do not edit.",
	            		"//",
	            		"import java.lang.Class;",
	            		"import java.lang.Override;",
	            		"import java.lang.SuppressWarnings;",
	            		"import org.bson.BsonDocument;",
	            		"import org.bson.RawBsonDocument;",
	            		"import org.bson.codecs.Codec;",
	            		"import org.bson.codecs.configuration.CodecRegistry;",
	            		"import org.bson.conversions.Bson;",
	            		"",
	            		"/**",
	            		" * Code generated by bsoneer-compiler.  Do not edit.",
	            		" */",
	            		"public final class BsoneeBson<T> implements Bson {",
	            		"  private static final BsoneeCodecProvider bcp = new BsoneeCodecProvider();",
	            		"",
	            		"  private final T wrapped;",
	            		"",
	            		"  private final Class<T> wrappedClazz;",
	            		"",
	            		"	@SuppressWarnings({\"unchecked\"})",
	            		"  private BsoneeBson(final T wrapped) {",
	            		"    this.wrapped = wrapped;",
	            		"    this.wrappedClazz = (Class<T>)wrapped.getClass();",
	            		"  }",
	            		"",
	            		"  @Override",
	            		"  public <C> BsonDocument toBsonDocument(final Class<C> documentClass, final CodecRegistry registry) {",
	            		"    Codec<T> codec = registry.get(wrappedClazz);",
	            		"    if (codec == null) {",
	            		"      codec = bcp.get(wrappedClazz, registry);",
	            		"    }",
	            		"    return new RawBsonDocument(wrapped, codec);",
	            		"  }",
	            		"",
	            		"  public static <C> Bson bson(final C entity) {",
	            		"    return new BsoneeBson<C>(entity);",
	            		"  }",
	            		"}"));

	    ASSERT.about(javaSource()).that(sourceFile).processedWith(bsoneerProcessors()).compilesWithoutError().and().generatesSources(expectedRegistry);
  }
}
