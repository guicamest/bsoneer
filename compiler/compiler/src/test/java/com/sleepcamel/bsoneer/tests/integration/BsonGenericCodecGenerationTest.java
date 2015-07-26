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
public final class BsonGenericCodecGenerationTest {
	@Test
	public void genericCodec() {
		JavaFileObject aSourceFile = JavaFileObjects.forSourceString(
				"A",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"class A<Q,W,E,R> {",
						"  Q q;",
						"  W w;",
						"  E e;",
						"  R r;",
						"}"));
		
		JavaFileObject bSourceFile = JavaFileObjects.forSourceString(
				"B",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"import java.lang.String;",
						"import java.lang.Integer;",
						"import java.lang.Long;",
						"class B<T,Y,R> extends A<String,Long,Integer,R> {",
						"  T t;",
						"  Y y;",
						"}"));
		
		JavaFileObject cSourceFile = JavaFileObjects.forSourceString(
				"C",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"import java.lang.String;",
						"class C<U, R> extends B<U,String,R> {",
						"  U u;",
						"}"));
		
		JavaFileObject dSourceFile = JavaFileObjects.forSourceString(
				"D",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"import java.lang.Integer;",
						"import java.lang.Long;",
						"@Bsonee",
						"class D extends C<Long,Integer> {",
						"}"));
		
		JavaFileObject eSourceFile = JavaFileObjects.forSourceString(
				"E",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"import java.lang.Boolean;",
						"@Bsonee",
						"class E extends C<Boolean, Boolean> {",
						"}"));

		JavaFileObject dExpectedCodec = JavaFileObjects
				.forSourceString(
						"DBsoneeCodec",
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
										"public class DBsoneeCodec extends BaseBsoneerCodec<D> {",
										"	public DBsoneeCodec(final CodecRegistry registry) {",
										"		super(registry, new ObjectIdGenerator());",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	public Class<D> getEncoderClass() {",
										"		return D.class;",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	@Override",
										"	@SuppressWarnings({\"unchecked\", \"rawtypes\"})",
										"	protected void encodeVariables(BsonWriter writer, D value, EncoderContext encoderContext) {",
										"		if (encoderContext.isEncodingCollectibleDocument()) {",
										"			writer.writeName(\"_id\");",
										"			Object vid = idGenerator.generate();",
										"			Codec cid = registry.get(vid.getClass());",
										"			encoderContext.encodeWithChildContext(cid, writer, vid);",
										"		}",
										"		if(value.u != null){",
										"			writer.writeName(\"u\");",
										"			writer.writeInt64(value.u);",
										"		}",
										"		if(value.t != null){",
										"			writer.writeName(\"t\");",
										"			writer.writeInt64(value.t);",
										"		}",
										"		if(value.y != null){",
										"			writer.writeName(\"y\");",
										"			writer.writeString(value.y);",
										"		}",
										"		if(value.q != null){",
										"			writer.writeName(\"q\");",
										"			writer.writeString(value.q);",
										"		}",
										"		if(value.w != null){",
										"			writer.writeName(\"w\");",
										"			writer.writeInt64(value.w);",
										"		}",
										"		if(value.e != null){",
										"			writer.writeName(\"e\");",
										"			writer.writeInt32(value.e);",
										"		}",
										"		if(value.r != null){",
										"			writer.writeName(\"r\");",
										"			writer.writeInt32(value.r);",
										"		}",
										"		super.encodeVariables(writer,value,encoderContext);",
										"	}",
										"",
										"	@Override",
										"	protected D instantiate() {",
										"		return new D();",
										"	}",
										"",
										"	protected void setupSetters() {",
										"		settersByName.put(\"u\",new USetter());",
										"		settersByName.put(\"t\",new TSetter());",
										"		settersByName.put(\"y\",new YSetter());",
										"		settersByName.put(\"q\",new QSetter());",
										"		settersByName.put(\"w\",new WSetter());",
										"		settersByName.put(\"e\",new ESetter());",
										"		settersByName.put(\"r\",new RSetter());",
										"	}",
										"",
										"	class USetter implements BsoneeBaseSetter<D> {",
										"		public void set(D instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.u = reader.readInt64();",
										"		}", "	}",
										"",
										"	class TSetter implements BsoneeBaseSetter<D> {",
										"		public void set(D instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.t = reader.readInt64();",
										"		}", "	}",
										"",
										"	class YSetter implements BsoneeBaseSetter<D> {",
										"		public void set(D instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.y = reader.readString();",
										"		}", "	}",
										"",
										"	class QSetter implements BsoneeBaseSetter<D> {",
										"		public void set(D instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.q = reader.readString();",
										"		}", "	}",
										"",
										"	class WSetter implements BsoneeBaseSetter<D> {",
										"		public void set(D instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.w = reader.readInt64();",
										"		}", "	}",
										"",
										"	class ESetter implements BsoneeBaseSetter<D> {",
										"		public void set(D instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.e = reader.readInt32();",
										"		}", "	}","",
										"",
										"	class RSetter implements BsoneeBaseSetter<D> {",
										"		public void set(D instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.r = reader.readInt32();",
										"		}", "	}",
										"", "}"));

		JavaFileObject eExpectedCodec = JavaFileObjects
				.forSourceString(
						"EBsoneeCodec",
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
										"public class EBsoneeCodec extends BaseBsoneerCodec<E> {",
										"	public EBsoneeCodec(final CodecRegistry registry) {",
										"		super(registry, new ObjectIdGenerator());",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	public Class<E> getEncoderClass() {",
										"		return E.class;",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	@Override",
										"	@SuppressWarnings({\"unchecked\", \"rawtypes\"})",
										"	protected void encodeVariables(BsonWriter writer, E value, EncoderContext encoderContext) {",
										"		if (encoderContext.isEncodingCollectibleDocument()) {",
										"			writer.writeName(\"_id\");",
										"			Object vid = idGenerator.generate();",
										"			Codec cid = registry.get(vid.getClass());",
										"			encoderContext.encodeWithChildContext(cid, writer, vid);",
										"		}",
										"		if(value.u != null){",
										"			writer.writeName(\"u\");",
										"			writer.writeBoolean(value.u);",
										"		}",
										"		if(value.t != null){",
										"			writer.writeName(\"t\");",
										"			writer.writeBoolean(value.t);",
										"		}",
										"		if(value.y != null){",
										"			writer.writeName(\"y\");",
										"			writer.writeString(value.y);",
										"		}",
										"		if(value.q != null){",
										"			writer.writeName(\"q\");",
										"			writer.writeString(value.q);",
										"		}",
										"		if(value.w != null){",
										"			writer.writeName(\"w\");",
										"			writer.writeInt64(value.w);",
										"		}",
										"		if(value.e != null){",
										"			writer.writeName(\"e\");",
										"			writer.writeInt32(value.e);",
										"		}",
										"		if(value.r != null){",
										"			writer.writeName(\"r\");",
										"			writer.writeBoolean(value.r);",
										"		}",
										"		super.encodeVariables(writer,value,encoderContext);",
										"	}",
										"",
										"	@Override",
										"	protected E instantiate() {",
										"		return new E();",
										"	}",
										"",
										"	protected void setupSetters() {",
										"		settersByName.put(\"u\",new USetter());",
										"		settersByName.put(\"t\",new TSetter());",
										"		settersByName.put(\"y\",new YSetter());",
										"		settersByName.put(\"q\",new QSetter());",
										"		settersByName.put(\"w\",new WSetter());",
										"		settersByName.put(\"e\",new ESetter());",
										"		settersByName.put(\"r\",new RSetter());",
										"	}",
										"",
										"	class USetter implements BsoneeBaseSetter<E> {",
										"		public void set(E instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.u = reader.readBoolean();",
										"		}", "	}",
										"",
										"	class TSetter implements BsoneeBaseSetter<E> {",
										"		public void set(E instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.t = reader.readBoolean();",
										"		}", "	}",
										"",
										"	class YSetter implements BsoneeBaseSetter<E> {",
										"		public void set(E instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.y = reader.readString();",
										"		}", "	}",
										"",
										"	class QSetter implements BsoneeBaseSetter<E> {",
										"		public void set(E instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.q = reader.readString();",
										"		}", "	}",
										"",
										"	class WSetter implements BsoneeBaseSetter<E> {",
										"		public void set(E instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.w = reader.readInt64();",
										"		}", "	}",
										"",
										"	class ESetter implements BsoneeBaseSetter<E> {",
										"		public void set(E instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.e = reader.readInt32();",
										"		}", "	}","",
										"",
										"	class RSetter implements BsoneeBaseSetter<E> {",
										"		public void set(E instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.r = reader.readBoolean();",
										"		}", "	}",
										"", "}"));
		
		ASSERT.about(javaSources()).that(Arrays.asList(aSourceFile, bSourceFile, cSourceFile, dSourceFile, eSourceFile))
		.processedWith(bsoneerProcessors()).compilesWithoutError().and()
		.generatesSources(dExpectedCodec, eExpectedCodec);
	}
	
	@Test
	public void genericInvertedCodec() {
		JavaFileObject cSourceFile = JavaFileObjects.forSourceString(
				"B",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"import java.lang.String;",
						"class B<U, R> {",
						"  U u;",
						"  R r;",
						"}"));
		
		JavaFileObject dSourceFile = JavaFileObjects.forSourceString(
				"C",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"@Bsonee",
						"class C<R,U> extends B<R,U> {",
						"  U uc;",
						"  R rc;",
						"}"));
		
		JavaFileObject eSourceFile = JavaFileObjects.forSourceString(
				"D",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"import java.lang.Boolean;",
						"import java.lang.Long;",
						"@Bsonee",
						"class D extends C<Boolean, Long> {",
						"}"));

		JavaFileObject expectedCodec = JavaFileObjects
				.forSourceString(
						"DBsoneeCodec",
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
										"public class DBsoneeCodec extends BaseBsoneerCodec<D> {",
										"	public DBsoneeCodec(final CodecRegistry registry) {",
										"		super(registry, new ObjectIdGenerator());",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	public Class<D> getEncoderClass() {",
										"		return D.class;",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	@Override",
										"	@SuppressWarnings({\"unchecked\", \"rawtypes\"})",
										"	protected void encodeVariables(BsonWriter writer, D value, EncoderContext encoderContext) {",
										"		if (encoderContext.isEncodingCollectibleDocument()) {",
										"			writer.writeName(\"_id\");",
										"			Object vid = idGenerator.generate();",
										"			Codec cid = registry.get(vid.getClass());",
										"			encoderContext.encodeWithChildContext(cid, writer, vid);",
										"		}",
										"		if(value.uc != null){",
										"			writer.writeName(\"uc\");",
										"			writer.writeInt64(value.uc);",
										"		}",
										"		if(value.rc != null){",
										"			writer.writeName(\"rc\");",
										"			writer.writeBoolean(value.rc);",
										"		}",
										"		if(value.u != null){",
										"			writer.writeName(\"u\");",
										"			writer.writeBoolean(value.u);",
										"		}",
										"		if(value.r != null){",
										"			writer.writeName(\"r\");",
										"			writer.writeInt64(value.r);",
										"		}",
										"		super.encodeVariables(writer,value,encoderContext);",
										"	}",
										"",
										"	@Override",
										"	protected D instantiate() {",
										"		return new D();",
										"	}",
										"",
										"	protected void setupSetters() {",
										"		settersByName.put(\"uc\",new UcSetter());",
										"		settersByName.put(\"rc\",new RcSetter());",
										"		settersByName.put(\"u\",new USetter());",
										"		settersByName.put(\"r\",new RSetter());",
										"	}",
										"",
										"	class UcSetter implements BsoneeBaseSetter<D> {",
										"		public void set(D instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.uc = reader.readInt64();",
										"		}", "	}",
										"",
										"	class RcSetter implements BsoneeBaseSetter<D> {",
										"		public void set(D instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.rc = reader.readBoolean();",
										"		}", "	}",
										"",
										"	class USetter implements BsoneeBaseSetter<D> {",
										"		public void set(D instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.u = reader.readBoolean();",
										"		}", "	}",
										"",
										"	class RSetter implements BsoneeBaseSetter<D> {",
										"		public void set(D instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.r = reader.readInt64();",
										"		}", "	}",
										"", "}"));

		ASSERT.about(javaSources()).that(Arrays.asList(cSourceFile, dSourceFile, eSourceFile))
		.processedWith(bsoneerProcessors()).compilesWithoutError().and()
		.generatesSources(expectedCodec);
	}
	
	@Test
	public void genericCollectionCodec() {
		JavaFileObject bSourceFile = JavaFileObjects.forSourceString(
				"B",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"import java.util.List;",
						"import java.util.Set;",
						"import java.util.Collection;",
						"class B<U, R> {",
						"  Collection<U> u;",
						"  List<Set<R>> r;",
						"}"));
		
		JavaFileObject cSourceFile = JavaFileObjects.forSourceString(
				"C",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"import java.util.ArrayList;",
						"import java.util.List;",
						"import java.util.HashSet;",
						"import java.lang.String;",
						"@Bsonee",
						"class C<U> extends B<String,Long> {",
						"  List<ArrayList<U>> uc;",
						"  HashSet<String> hss;",
						"}"));
		
		JavaFileObject expectedCodec = JavaFileObjects
				.forSourceString(
						"CBsoneeCodec",
						Joiner.on("\n")
								.join("// Code generated by bsoneer-compiler.  Do not edit.",
										"//",
										"import com.sleepcamel.bsoneer.BaseBsoneerCodec;",
										"import com.sleepcamel.bsoneer.BsoneeBaseSetter;",
										"import java.lang.Class;",
										"import java.lang.Long;",
										"import java.lang.Object;",
										"import java.lang.Override;",
										"import java.lang.String;",
										"import java.lang.SuppressWarnings;",
										"import java.util.ArrayList;",
										"import java.util.Collection;",
										"import java.util.HashSet;",
										"import java.util.LinkedHashSet;",
										"import java.util.List;",
										"import java.util.Set;",
										"import javax.annotation.Generated;",
										"import org.bson.BsonReader;",
										"import org.bson.BsonType;",
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
										"public class CBsoneeCodec extends BaseBsoneerCodec<C> {",
										"	public CBsoneeCodec(final CodecRegistry registry) {",
										"		super(registry, new ObjectIdGenerator());",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	public Class<C> getEncoderClass() {",
										"		return C.class;",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	@Override",
										"	@SuppressWarnings({\"unchecked\", \"rawtypes\"})",
										"	protected void encodeVariables(BsonWriter writer, C value, EncoderContext encoderContext) {",
										"		if (encoderContext.isEncodingCollectibleDocument()) {",
										"			writer.writeName(\"_id\");",
										"			Object vid = idGenerator.generate();",
										"			Codec cid = registry.get(vid.getClass());",
										"			encoderContext.encodeWithChildContext(cid, writer, vid);",
										"		}",
										"		if(value.uc != null){",
										"			writer.writeName(\"uc\");",
										"			writer.writeStartArray();",
										"			for (ArrayList<Object> _arraylist0:((List<ArrayList<Object>>)value.uc)) {",
										"				if (_arraylist0 == null) {",
										"					writer.writeNull();",
										"				} else {",
										"					writer.writeStartArray();",
										"					for (Object _object0:((ArrayList)_arraylist0)) {",
										"						if (_object0 == null) {",
										"							writer.writeNull();",
										"						} else {",
										"							Codec c = registry.get(_object0.getClass());",
										"							encoderContext.encodeWithChildContext(c, writer, _object0);",
										"						}",
										"					}",
										"					writer.writeEndArray();",
										"				}",
										"			}",
										"			writer.writeEndArray();",
										"		}",
										"		if(value.hss != null){",
										"			writer.writeName(\"hss\");",
										"			writer.writeStartArray();",
										"			for(String _string0:((HashSet<String>)value.hss)) {",
										"				if (_string0 == null) {",
										"					writer.writeNull();",
										"				} else {",
										"					writer.writeString(_string0);",
										"				}",
										"			}",
										"			writer.writeEndArray();",
										"		}",
										"		if(value.u != null){",
										"			writer.writeName(\"u\");",
										"			writer.writeStartArray();",
										"			for(String _string0:((Collection<String>)value.u)) {",
										"				if (_string0 == null) {",
										"					writer.writeNull();",
										"				} else {",
										"					writer.writeString(_string0);",
										"				}",
										"			}",
										"			writer.writeEndArray();",
										"		}",
										"		if(value.r != null){",
										"			writer.writeName(\"r\");",
										"			writer.writeStartArray();",
										"			for(Set<Long> _set0:((List<Set<Long>>)value.r)) {",
										"				if (_set0 == null) {",
										"					writer.writeNull();",
										"				} else {",
										"					writer.writeStartArray();",
										"					for(Long _long0:((Set<Long>)_set0)) {",
										"						if (_long0 == null) {",
										"							writer.writeNull();",
										"						} else {",
										"							writer.writeInt64(_long0);",
										"						}",
										"					}",
										"					writer.writeEndArray();",
										"				}",
										"			}",
										"			writer.writeEndArray();",
										"		}",
										"		super.encodeVariables(writer,value,encoderContext);",
										"	}",
										"",
										"	@Override",
										"	protected C instantiate() {",
										"		return new C();",
										"	}",
										"",
										"	protected void setupSetters() {",
										"		settersByName.put(\"uc\",new UcSetter());",
										"		settersByName.put(\"hss\",new HssSetter());",
										"		settersByName.put(\"u\",new USetter());",
										"		settersByName.put(\"r\",new RSetter());",
										"	}",
										"",
										"	class UcSetter implements BsoneeBaseSetter<C> {",
										"		public void set(C instance, BsonReader reader, DecoderContext decoderContext) {",
//										List<ArrayList<U>> uc
										"			BsonType bsonType = reader.getCurrentBsonType();",
										"			if (bsonType == BsonType.NULL) {",
										"				reader.readNull();",
										"				instance.uc = null;",
										"				return;",
										"			}",
										"			List<ArrayList<Object>> value = instance.uc;",
										"			if (value == null) {",
										"				value = new ArrayList<ArrayList<Object>>();",
										"				instance.uc = value;",
										"			}",
										"			reader.readStartArray();",
										"			while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {",
										"				bsonType = reader.getCurrentBsonType();",
										"				if ( bsonType == BsonType.NULL ){",
										"					reader.readNull();",
										"					value.add(null);",
										"				}else{",
										"					ArrayList _arraylist0 = new ArrayList();",
										"					reader.readStartArray();",
										"					while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {",
										"						bsonType = reader.getCurrentBsonType();",
										"						if ( bsonType == BsonType.NULL ){",
										"							reader.readNull();",
										"							_arraylist0.add(null);",
										"						}else{",
										"							_arraylist0.add((java.lang.Object)defaultReader.readValue(reader, decoderContext));",
										"						}",
										"					}",
										"					reader.readEndArray();",
										"					value.add(_arraylist0);",
										"				}",
										"			}",
										"			reader.readEndArray();",
										"		}", "	}",
										"",
										"	class HssSetter implements BsoneeBaseSetter<C> {",
										"		public void set(C instance, BsonReader reader, DecoderContext decoderContext) {",
//										HashSet<String> hss;
										"			BsonType bsonType = reader.getCurrentBsonType();",
										"			if (bsonType == BsonType.NULL) {",
										"				reader.readNull();",
										"				instance.hss = null;",
										"				return;",
										"			}",
										"			HashSet<String> value = instance.hss;",
										"			if (value == null) {",
										"				value = new HashSet<String>();",
										"				instance.hss = value;",
										"			}",
										"			reader.readStartArray();",
										"			while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {",
										"				bsonType = reader.getCurrentBsonType();",
										"				if ( bsonType == BsonType.NULL ){",
										"					reader.readNull();",
										"					value.add(null);",
										"				}else{",
										"					value.add(reader.readString());",
										"				}",
										"			}",
										"			reader.readEndArray();",
										"		}", "	}",
										"",
										"	class USetter implements BsoneeBaseSetter<C> {",
										"		public void set(C instance, BsonReader reader, DecoderContext decoderContext) {",
//										Collection<String> u;
										"			BsonType bsonType = reader.getCurrentBsonType();",
										"			if (bsonType == BsonType.NULL) {",
										"				reader.readNull();",
										"				instance.u = null;",
										"				return;",
										"			}",
										"			Collection<String> value = instance.u;",
										"			if (value == null) {",
										"				value = new ArrayList<String>();",
										"				instance.u = value;",
										"			}",
										"			reader.readStartArray();",
										"			while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {",
										"				bsonType = reader.getCurrentBsonType();",
										"				if ( bsonType == BsonType.NULL ){",
										"					reader.readNull();",
										"					value.add(null);",
										"				}else{",
										"					value.add(reader.readString());",
										"				}",
										"			}",
										"			reader.readEndArray();",
										"		}", "	}",
										"",
										"	class RSetter implements BsoneeBaseSetter<C> {",
										"		public void set(C instance, BsonReader reader, DecoderContext decoderContext) {",
//										List<Set<Long>> r;
										"			BsonType bsonType = reader.getCurrentBsonType();",
										"			if (bsonType == BsonType.NULL) {",
										"				reader.readNull();",
										"				instance.r = null;",
										"				return;",
										"			}",
										"			List<Set<Long>> value = instance.r;",
										"			if (value == null) {",
										"				value = new ArrayList<Set<Long>>();",
										"				instance.r = value;",
										"			}",
										"			reader.readStartArray();",
										"			while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {",
										"				bsonType = reader.getCurrentBsonType();",
										"				if ( bsonType == BsonType.NULL ){",
										"					reader.readNull();",
										"					value.add(null);",
										"				}else{",
										"					Set<Long> _set0 = new LinkedHashSet<Long>();",
										"					reader.readStartArray();",
										"					while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {",
										"						bsonType = reader.getCurrentBsonType();",
										"						if ( bsonType == BsonType.NULL ){",
										"							reader.readNull();",
										"							_set0.add(null);",
										"						}else{",
										"							_set0.add(reader.readInt64());",
										"						}",
										"					}",
										"					reader.readEndArray();",
										"					value.add(_set0);",
										"				}",
										"			}",
										"			reader.readEndArray();",
										"		}", "	}",
										"", "}"));

		ASSERT.about(javaSources()).that(Arrays.asList(bSourceFile, cSourceFile))
		.processedWith(bsoneerProcessors()).compilesWithoutError().and()
		.generatesSources(expectedCodec);
	}
}