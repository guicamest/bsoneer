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

import java.util.Collection;

import javax.lang.model.type.TypeMirror;

import org.bson.BsonBinary;
import org.bson.BsonDbPointer;
import org.bson.BsonRegularExpression;
import org.bson.BsonTimestamp;
import org.bson.types.ObjectId;

import com.google.common.collect.ImmutableMap;
import com.sleepcamel.bsoneer.processor.domain.Property;
import com.sleepcamel.bsoneer.processor.util.Util;
import com.squareup.javapoet.CodeBlock;

public class PassThroughEncodeCodeProvider implements EncodeCodeProvider {

	protected ImmutableMap<String, String> passThroughMappings = ImmutableMap
			.<String, String> builder()
			.put(int.class.getCanonicalName(), "Int32")
			.put(Integer.class.getCanonicalName(), "Int32")
			.
			// put("short", "Int32").put("java.lang.Short", "Int32").
			put(long.class.getCanonicalName(), "Int64")
			.put(Long.class.getCanonicalName(), "Int64")
			.
			// put("float", "Float").put("java.lang.Float", "Float").
			put(double.class.getCanonicalName(), "Double")
			.put(Double.class.getCanonicalName(), "Double")
			.put(boolean.class.getCanonicalName(), "Boolean")
			.put(Boolean.class.getCanonicalName(), "Boolean")
			.put(String.class.getCanonicalName(), "String")
			.put(BsonBinary.class.getCanonicalName(), "BinaryData")
			.put(BsonDbPointer.class.getCanonicalName(), "DBPointer")
			.put(ObjectId.class.getCanonicalName(), "ObjectId")
			.put(BsonRegularExpression.class.getCanonicalName(),
					"RegularExpression")
			.put(BsonTimestamp.class.getCanonicalName(), "Timestamp").build();

	@Override
	public boolean applies(Property property) {
		return true;
	}

	@Override
	public boolean applies(TypeMirror tm) {
		return true;
	}

	@Override
	public void putEncodeCode(CodeBlock.Builder cb, TypeMirror tm,
			Collection<EncodeCodeProvider> allProviders, String varName) {
		boolean isPrimitive = tm.getKind().isPrimitive();

		String writeMethod = passThroughMappings.get(tm.toString());

		if (writeMethod != null) {
//			cb.addStatement("writer.write$L(($T)$L)", writeMethod, tm, varName);
//			cb.addStatement("$T $L = $L", tm, "alooooo", varName);
//			cb.addStatement("writer.write$L($L)", writeMethod, "alooooo");
			cb.addStatement("writer.write$L($L)", writeMethod, varName);
		} else {
			if (!isPrimitive) {
				cb.addStatement("$T c = registry.get($L.getClass())",
						Util.bsonCodecTypeName(), varName);
				cb.addStatement("encoderContext.encodeWithChildContext(c, writer, $L)", varName);
			} else {
				throw new RuntimeException("No write method for "
						+ tm.toString());
			}
		}
	}

}
