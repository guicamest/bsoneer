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
public final class BsonArrayCodecGenerationTest {
	@Test
	public void genericCodec() {
		JavaFileObject aSourceFile = JavaFileObjects.forSourceString(
				"A",
				Joiner.on("\n").join(
						"class A<Q> {",
						"  String q;",
						"  Q[] qs;",
						"  Q[][] qss;",
						"}"));
		
		JavaFileObject bSourceFile = JavaFileObjects.forSourceString(
				"B",
				Joiner.on("\n").join("import com.sleepcamel.bsoneer.Bsonee;",
						"import java.lang.String;",
						"@Bsonee",
						"class B<T> extends A<String> {",
						"  double[] ds;",
						"  T[] ts;",
						"  T[][] tss;",
						"  Double[] dds;",
						"}"));
		
		JavaFileObject bExpectedCodec = JavaFileObjects
				.forSourceString(
						"BBsoneeCodec",
						Joiner.on("\n")
								.join("// Code generated by bsoneer-compiler.  Do not edit.",
										"//",
										"import com.sleepcamel.bsoneer.BaseBsoneerCodec;",
										"import com.sleepcamel.bsoneer.BsoneeBaseSetter;",
										"import java.lang.Class;",
										"import java.lang.Double;",
										"import java.lang.Object;",
										"import java.lang.Override;",
										"import java.lang.String;",
										"import java.lang.SuppressWarnings;",
										"import java.util.ArrayList;",
										"import java.util.List;",
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
										"public class BBsoneeCodec extends BaseBsoneerCodec<B> {",
										"	public BBsoneeCodec(final CodecRegistry registry) {",
										"		super(registry, new ObjectIdGenerator());",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	public Class<B> getEncoderClass() {",
										"		return B.class;",
										"	}",
										"",
										"	/**",
										"	 * {@inhericDoc}",
										"	 */",
										"	@Override",
										"	@SuppressWarnings({\"unchecked\", \"rawtypes\"})",
										"	protected void encodeVariables(BsonWriter writer, B value, EncoderContext encoderContext) {",
										"		if (encoderContext.isEncodingCollectibleDocument()) {",
										"			writer.writeName(\"_id\");",
										"			Object vid = idGenerator.generate();",
										"			Codec cid = registry.get(vid.getClass());",
										"			encoderContext.encodeWithChildContext(cid, writer, vid);",
										"		}",
//										double[] ds
										"		if(value.ds != null){",
										"			writer.writeName(\"ds\");",
										"			writer.writeStartArray();",
										"			for(double _double0:((double[])value.ds)) {",
										"				writer.writeDouble(_double0);",
										"			}",
										"			writer.writeEndArray();",
										"		}",
//										T[] ts
										"		if(value.ts != null){",
										"			writer.writeName(\"ts\");",
										"			writer.writeStartArray();",
										"			for(Object _object0:((Object[])value.ts)) {",
										"				if (_object0 == null) {",
										"					writer.writeNull();",
										"				} else {",
										"					Codec c = registry.get(_object0.getClass());",
										"					encoderContext.encodeWithChildContext(c, writer, _object0);",
										"				}",
										"			}",
										"			writer.writeEndArray();",
										"		}",
//										T[][] tss
										"		if(value.tss != null){",
										"			writer.writeName(\"tss\");",
										"			writer.writeStartArray();",
										"			for(Object[] _objectarray0:((Object[][])value.tss)) {",
										"				if (_objectarray0 == null) {",
										"					writer.writeNull();",
										"				} else {",
										"					writer.writeStartArray();",
										"					for(Object _object0:((Object[])_objectarray0)) {",
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
//										Double[] dds
										"		if(value.dds != null){",
										"			writer.writeName(\"dds\");",
										"			writer.writeStartArray();",
										"			for(Double _double1:((Double[])value.dds)) {",
										"				if (_double1 == null) {",
										"					writer.writeNull();",
										"				} else {",
										"					writer.writeDouble(_double1);",
										"				}",
										"			}",
										"			writer.writeEndArray();",
										"		}",
//										String q
										"		if(value.q != null){",
										"			writer.writeName(\"q\");",
										"			writer.writeString(value.q);",
										"		}",
//										String[] qs
										"		if(value.qs != null){",
										"			writer.writeName(\"qs\");",
										"			writer.writeStartArray();",
										"			for(String _string0:((String[])value.qs)) {",
										"				if (_string0 == null) {",
										"					writer.writeNull();",
										"				} else {",
										"					writer.writeString(_string0);",
										"				}",
										"			}",
										"			writer.writeEndArray();",
										"		}",
//										String[][] qss
										"		if(value.qss != null){",
										"			writer.writeName(\"qss\");",
										"			writer.writeStartArray();",
										"			for(String[] _stringarray0:((String[][])value.qss)) {",
										"				if (_stringarray0 == null) {",
										"					writer.writeNull();",
										"				} else {",
										"					writer.writeStartArray();",
										"					for(String _string0:((String[])_stringarray0)) {",
										"						if (_string0 == null) {",
										"							writer.writeNull();",
										"						} else {",
										"							writer.writeString(_string0);",
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
										"	protected B instantiate() {",
										"		return new B();",
										"	}",
										"",
										"	protected void setupSetters() {",
										"		settersByName.put(\"ds\",new DsSetter());",
										"		settersByName.put(\"ts\",new TsSetter());",
										"		settersByName.put(\"tss\",new TssSetter());",
										"		settersByName.put(\"dds\",new DdsSetter());",
										"		settersByName.put(\"q\",new QSetter());",
										"		settersByName.put(\"qs\",new QsSetter());",
										"		settersByName.put(\"qss\",new QssSetter());",
										"	}",
										"",
										"	class DsSetter implements BsoneeBaseSetter<B> {",
										"		public void set(B instance, BsonReader reader, DecoderContext decoderContext) {",
										"			BsonType bsonType = reader.getCurrentBsonType();",
										"			if (bsonType == BsonType.NULL) {",
										"				reader.readNull();",
										"				instance.ds = null;",
										"				return;",
										"			}",
										"			List<Double> valuetmp = new ArrayList<Double>();",
										"			reader.readStartArray();",
										"			while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {",
										"				bsonType = reader.getCurrentBsonType();",
										"				if (bsonType == BsonType.NULL) {",
										"					reader.readNull();",
										"				} else {",
										"					valuetmp.add(reader.readDouble());",
										"				}",
										"			}",
										"			reader.readEndArray();",
										"			double[] value = new double[valuetmp.size()];",
										"			for(int valueTmpIdx=0; valueTmpIdx < value.length; valueTmpIdx++) {",
										"				value[valueTmpIdx] = valuetmp.get(valueTmpIdx);",
										"			}",
										"			instance.ds = value;",
										"		}",
										"	}",

										"	class TsSetter implements BsoneeBaseSetter<B> {",
										"		public void set(B instance, BsonReader reader, DecoderContext decoderContext) {",
										"			BsonType bsonType = reader.getCurrentBsonType();",
										"			if (bsonType == BsonType.NULL) {",
										"				reader.readNull();",
										"				instance.ts = null;",
										"				return;",
										"			}",
										"			List<Object> valuetmp = new ArrayList<Object>();",
										"			reader.readStartArray();",
										"			while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {",
										"				bsonType = reader.getCurrentBsonType();",
										"				if (bsonType == BsonType.NULL) {",
										"					reader.readNull();",
										"					valuetmp.add(null);",
										"				} else {",
										"					valuetmp.add((java.lang.Object)defaultReader.readValue(reader,decoderContext));",
										"				}",
										"			}",
										"			reader.readEndArray();",
										"			Object[] value = valuetmp.toArray(new Object[]{});",
										"			instance.ts = value;",
										"		}",
										"	}",

										"	class TssSetter implements BsoneeBaseSetter<B> {",
										"		public void set(B instance, BsonReader reader, DecoderContext decoderContext) {",
										"			BsonType bsonType = reader.getCurrentBsonType();",
										"			if (bsonType == BsonType.NULL) {",
										"				reader.readNull();",
										"				instance.tss = null;",
										"				return;",
										"			}",
										"			List<Object[]> valuetmp = new ArrayList<Object[]>();",
										"			reader.readStartArray();",
										"			while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {",
										"				bsonType = reader.getCurrentBsonType();",
										"				if (bsonType == BsonType.NULL) {",
										"					reader.readNull();",
										"					valuetmp.add(null);",
										"				} else {",
										"					List<Object> _objectarray0tmp = new ArrayList<Object>();",
										"					reader.readStartArray();",
										"					while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {",
										"						bsonType = reader.getCurrentBsonType();",
										"						if (bsonType == BsonType.NULL) {",
										"							reader.readNull();",
										"							_objectarray0tmp.add(null);",
										"						} else {",
										"							_objectarray0tmp.add((java.lang.Object)defaultReader.readValue(reader,decoderContext));",
										"						}",
										"					}",
										"					reader.readEndArray();",
										"					Object[] _objectarray0 = _objectarray0tmp.toArray(new Object[]{});",
										"					valuetmp.add(_objectarray0);",
										"				}",
										"			}",
										"			reader.readEndArray();",
										"			Object[][] value = valuetmp.toArray(new Object[][]{});",
										"			instance.tss = value;",
										"		}",
										"	}",

										"	class DdsSetter implements BsoneeBaseSetter<B> {",
										"		public void set(B instance, BsonReader reader, DecoderContext decoderContext) {",
										"			BsonType bsonType = reader.getCurrentBsonType();",
										"			if (bsonType == BsonType.NULL) {",
										"				reader.readNull();",
										"				instance.dds = null;",
										"				return;",
										"			}",
										"			List<Double> valuetmp = new ArrayList<Double>();",
										"			reader.readStartArray();",
										"			while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {",
										"				bsonType = reader.getCurrentBsonType();",
										"				if (bsonType == BsonType.NULL) {",
										"					reader.readNull();",
										"					valuetmp.add(null);",
										"				} else {",
										"					valuetmp.add(reader.readDouble());",
										"				}",
										"			}",
										"			reader.readEndArray();",
										"			Double[] value = valuetmp.toArray(new Double[]{});",
										"			instance.dds = value;",
										"		}",
										"	}",

										"	class QSetter implements BsoneeBaseSetter<B> {",
										"		public void set(B instance, BsonReader reader, DecoderContext decoderContext) {",
										"			instance.q = reader.readString();",
										"		}",
										"	}",

										"	class QsSetter implements BsoneeBaseSetter<B> {",
										"		public void set(B instance, BsonReader reader, DecoderContext decoderContext) {",
										"			BsonType bsonType = reader.getCurrentBsonType();",
										"			if (bsonType == BsonType.NULL) {",
										"				reader.readNull();",
										"				instance.qs = null;",
										"				return;",
										"			}",
										"			List<String> valuetmp = new ArrayList<String>();",
										"			reader.readStartArray();",
										"			while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {",
										"				bsonType = reader.getCurrentBsonType();",
										"				if (bsonType == BsonType.NULL) {",
										"					reader.readNull();",
										"					valuetmp.add(null);",
										"				} else {",
										"					valuetmp.add(reader.readString());",
										"				}",
										"			}",
										"			reader.readEndArray();",
										"			String[] value = valuetmp.toArray(new String[]{});",
										"			instance.qs = value;",
										"		}",
										"	}",

										"	class QssSetter implements BsoneeBaseSetter<B> {",
										"		public void set(B instance, BsonReader reader, DecoderContext decoderContext) {",
										"			BsonType bsonType = reader.getCurrentBsonType();",
										"			if (bsonType == BsonType.NULL) {",
										"				reader.readNull();",
										"				instance.qss = null;",
										"				return;",
										"			}",
										"			List<String[]> valuetmp = new ArrayList<String[]>();",
										"			reader.readStartArray();",
										"			while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {",
										"				bsonType = reader.getCurrentBsonType();",
										"				if (bsonType == BsonType.NULL) {",
										"					reader.readNull();",
										"					valuetmp.add(null);",
										"				} else {",
										"					List<String> _stringarray0tmp = new ArrayList<String>();",
										"					reader.readStartArray();",
										"					while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {",
										"						bsonType = reader.getCurrentBsonType();",
										"						if (bsonType == BsonType.NULL) {",
										"							reader.readNull();",
										"							_stringarray0tmp.add(null);",
										"						} else {",
										"							_stringarray0tmp.add(reader.readString());",
										"						}",
										"					}",
										"					reader.readEndArray();",
										"					String[] _stringarray0 = _stringarray0tmp.toArray(new String[]{});",
										"					valuetmp.add(_stringarray0);",
										"				}",
										"			}",
										"			reader.readEndArray();",
										"			String[][] value = valuetmp.toArray(new String[][]{});",
										"			instance.qss = value;",
										"		}",
										"	}",
										"", "}"));

		ASSERT.about(javaSources()).that(Arrays.asList(aSourceFile, bSourceFile))
		.processedWith(bsoneerProcessors()).compilesWithoutError().and()
		.generatesSources(bExpectedCodec);
	}
	
}
