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
package com.sleepcamel.bsoneer.processor.codeprovider;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

public class CodeUtil {

	private CodeUtil() {
	}

	public static ParameterSpec bsonReaderParameter() {
		return ParameterSpec.builder(bsonReaderTypeName(), "reader").build();
	}

	public static ParameterSpec bsonWriterParameter() {
		return ParameterSpec.builder(bsonWriterTypeName(), "writer").build();
	}

	public static ParameterSpec bsonDecoderContextParameter() {
		return ParameterSpec.builder(bsonDecoderContextTypeName(), "decoderContext").build();
	}

	public static ParameterSpec bsonEncoderContextParameter() {
		return ParameterSpec.builder(bsonEncoderContextTypeName(), "encoderContext").build();
	}

	public static ParameterSpec bsonCodecParameter() {
		return ParameterSpec.builder(bsonCodecTypeName(), "codec").build();
	}

	public static ParameterSpec bsonRegistryParameter() {
		return ParameterSpec.builder(bsonRegistryTypeName(), "registry", Modifier.FINAL).build();
	}

	public static TypeName bsonReaderTypeName() {
		return ClassName.get("org.bson", "BsonReader");
	}

	public static TypeName bsonWriterTypeName() {
		return ClassName.get("org.bson", "BsonWriter");
	}

	public static TypeName bsonDecoderContextTypeName() {
		return ClassName.get("org.bson.codecs", "DecoderContext");
	}

	public static TypeName bsonEncoderContextTypeName() {
		return ClassName.get("org.bson.codecs", "EncoderContext");
	}

	public static TypeName bsonCodecTypeName() {
		return ClassName.get("org.bson.codecs", "Codec");
	}

	public static TypeName bsonRegistryTypeName() {
		return ClassName.get("org.bson.codecs.configuration", "CodecRegistry");
	}

	public static TypeName bsonCodecProviderTypeName() {
		return ClassName.get("org.bson.codecs.configuration", "CodecProvider");
	}

	public static TypeName bsonTypeName() {
		return ClassName.get("org.bson.conversions", "Bson");
	}

	public static TypeName bsonTypeTypeName() {
		return ClassName.get("org.bson", "BsonType");
	}

}
