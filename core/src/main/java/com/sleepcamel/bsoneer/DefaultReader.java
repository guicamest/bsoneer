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
package com.sleepcamel.bsoneer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.bson.BsonBinarySubType;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.configuration.CodecRegistry;

/**
 * Default reader used to decode values from a bson
 */
public class DefaultReader {

	private static Object lock = new Object[]{};
	private static DefaultReader instance;
	private AtomicReference<CodecRegistry> registry;
	private BsonTypeClassMap bsonTypeClassMap;

	private DefaultReader(CodecRegistry registry) {
		this(registry, new BsonTypeClassMap());
	}

	private DefaultReader(CodecRegistry registry, BsonTypeClassMap bsonTypeClassMap) {
		this.registry = new AtomicReference<CodecRegistry>(registry);
		this.bsonTypeClassMap = bsonTypeClassMap;
	}

	synchronized public static DefaultReader get(CodecRegistry registry) {
		synchronized (lock) {
			if (instance == null) {
				instance = new DefaultReader(registry, new BsonTypeClassMap());
			} else {
				instance.registry.set(registry);
			}
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	public <U> U readValue(final BsonReader reader,
			final DecoderContext decoderContext) {
		BsonType bsonType = reader.getCurrentBsonType();
		if (bsonType == BsonType.NULL) {
			reader.readNull();
			return null;
		} else if (bsonType == BsonType.ARRAY) {
			return (U) readList(reader, decoderContext);
		} else if (bsonType == BsonType.BINARY) {
			byte bsonSubType = reader.peekBinarySubType();
			if (bsonSubType == BsonBinarySubType.UUID_STANDARD.getValue()
					|| bsonSubType == BsonBinarySubType.UUID_LEGACY.getValue()) {
				return (U) registry.get().get(UUID.class).decode(reader,
						decoderContext);
			}
		}
		return (U) registry.get().get(bsonTypeClassMap.get(bsonType)).decode(reader,
				decoderContext);
	}
	
	protected <T extends Enum<T>> T readEnum(final BsonReader reader, Class<T> enumClass){
		return Enum.<T>valueOf(enumClass, reader.readString());
	}

	protected List<Object> readList(final BsonReader reader,
			final DecoderContext decoderContext) {
		List<Object> list = new ArrayList<Object>();
		reader.readStartArray();
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			list.add(readValue(reader, decoderContext));
		}
		reader.readEndArray();
		return list;
	}
}
