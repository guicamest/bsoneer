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

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.bson.BsonBinary;
import org.bson.BsonDbPointer;
import org.bson.BsonRegularExpression;
import org.bson.BsonTimestamp;
import org.bson.types.ObjectId;

import com.google.common.collect.ImmutableMap;
import com.sleepcamel.bsoneer.processor.domain.Property;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

public class PassThroughDecodeCodeProvider extends SingleStatementDecodeCodeProvider {

	protected ImmutableMap<String, String> passThroughMappings = ImmutableMap.<String, String>builder().
			put(int.class.getCanonicalName(), "Int32").put(Integer.class.getCanonicalName(), "Int32").
			//put("short", "Int32").put("java.lang.Short", "Int32").
			put(long.class.getCanonicalName(), "Int64").put(Long.class.getCanonicalName(), "Int64").
//			put("float", "Float").put("java.lang.Float", "Float").
			put(double.class.getCanonicalName(), "Double").put(Double.class.getCanonicalName(), "Double").
			put(boolean.class.getCanonicalName(), "Boolean").put(Boolean.class.getCanonicalName(), "Boolean").
			put(String.class.getCanonicalName(), "String").
			put(BsonBinary.class.getCanonicalName(), "BinaryData").
			put(BsonDbPointer.class.getCanonicalName(), "DBPointer").
			put(ObjectId.class.getCanonicalName(), "ObjectId").
			put(BsonRegularExpression.class.getCanonicalName(), "RegularExpression").
			put(BsonTimestamp.class.getCanonicalName(), "Timestamp").
			build();
	
	@Override
	public boolean applies(Property property) {
		return true;
	}
	
	@Override
	public boolean applies(TypeMirror tm) {
		return true;
	}

	@Override
	public void putDecodeCode(CodeBlock.Builder cb, TypeMirror tm, DecodeCodeProviders decodeProviders, String variableName, boolean declareVariable) {
		String readerCall = "reader.read$L()";
		
		String readMethod = passThroughMappings.get(tm.toString());
		if (readMethod == null) {
			String cast = tm.getKind().equals(TypeKind.DECLARED)
							? "(" + tm.toString() + ")" : "";
			// We don't need $L here, we put it to have a 2nd argument for addStatement
			readerCall = cast + "defaultReader.readValue$L(reader,decoderContext)";
			readMethod = "";
		}
		cb.add(readerCall, readMethod, ClassName.get(tm));
	}

}
