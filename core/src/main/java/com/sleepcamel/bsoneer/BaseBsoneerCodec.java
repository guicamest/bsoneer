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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.assertions.Assertions;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.IdGenerator;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base {@link org.bson.codecs.CollectibleCodec} for bsonee generated codecs
 *
 * @see org.bson.DocumentCodec
 */
public abstract class BaseBsoneerCodec<T> implements CollectibleCodec<T> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	protected final CodecRegistry registry;
	protected final IdGenerator idGenerator;

	protected Map<String, BsoneeBaseSetter<T>> settersByName = new HashMap<String, BsoneeBaseSetter<T>>();

	protected DefaultReader defaultReader;

	/**
	 * Construct a new instance with the given registry
	 *
	 * @param registry {@link org.bson.codecs.configuration.CodecRegistry} to be used
	 * @param idGenerator {@link org.bson.codecs.IdGenerator} to be used
	 */
	public BaseBsoneerCodec(final CodecRegistry registry, final IdGenerator generator) {
		this.registry = Assertions.notNull("registry", registry);
		this.idGenerator = Assertions.notNull("idGenerator",
				generator);
		defaultReader = DefaultReader.get(registry);
		setupSetters();
	}

	protected void setupSetters() {
	}

	@Override
	public boolean documentHasId(final T entity) {
		// Not called by codec api... WTF?
		return true;
	}

    @Override
    public BsonValue getDocumentId(final T entity) {
		// Not called by codec api... WTF?
        return null;
    }

    @Override
    public T generateIdIfAbsentFromDocument(final T entity) {
    	// Nothing to set here...
        return entity;
    }

    /**
	 * {@inhericDoc}
	 */
	public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
		writer.writeStartDocument();
		encodeVariables(writer, value, encoderContext);
		writer.writeEndDocument();
	}

	protected void encodeVariables(BsonWriter writer, T value, EncoderContext encoderContext) {};

	/**
	 * To be used if and only if it belongs to {@link java.util} package
	 * @param writer
	 * @param coll
	 * @param encoderContext
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void encode(BsonWriter writer, Collection<?> coll, EncoderContext encoderContext) {
		writer.writeStartArray();
		Iterator<?> iterator = coll.iterator();
		while(iterator.hasNext()) {
			Object next = iterator.next();
			if (next == null) {
				writer.writeNull();
			} else {
				Codec codec = registry.get(next.getClass());
				encoderContext.encodeWithChildContext(codec, writer, next);
			}
		}
		writer.writeEndArray();
	}

	/**
	 * To be used if and only if it is an array []
	 * @param writer
	 * @param coll
	 * @param encoderContext
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void encode(BsonWriter writer, Object[] coll, EncoderContext encoderContext) {
		writer.writeStartArray();
		for (Object next : coll) {
			if (next == null) {
				writer.writeNull();
			} else {
				Codec codec = registry.get(next.getClass());
				encoderContext.encodeWithChildContext(codec, writer, next);
			}
		}
		writer.writeEndArray();
	}

	protected void encode(BsonWriter writer, boolean[] coll, EncoderContext encoderContext) {
		writer.writeStartArray();
		for (boolean next : coll) {
			writer.writeBoolean(next);
		}
		writer.writeEndArray();
	}

	protected void encode(BsonWriter writer, int[] coll, EncoderContext encoderContext) {
		writer.writeStartArray();
		for (int next : coll) {
			writer.writeInt32(next);
		}
		writer.writeEndArray();
	}

	protected void encode(BsonWriter writer, long[] coll, EncoderContext encoderContext) {
		writer.writeStartArray();
		for (long next : coll) {
			writer.writeInt64(next);
		}
		writer.writeEndArray();
	}

	protected void encode(BsonWriter writer, double[] coll, EncoderContext encoderContext) {
		writer.writeStartArray();
		for (double next : coll) {
			writer.writeDouble(next);
		}
		writer.writeEndArray();
	}

    /**
	 * {@inhericDoc}
	 */
	@Override
	public T decode(BsonReader reader, DecoderContext decoderContext) {
		T instance = instantiate();
		reader.readStartDocument();
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			String fieldName = reader.readName();
			BsoneeBaseSetter<T> bsoneeBaseSetter = settersByName.get(fieldName);
			if (bsoneeBaseSetter != null) {
				bsoneeBaseSetter.set(instance, reader, decoderContext);
			} else {
				logger.warn("No setter for " + fieldName);
				BsonType bsonType = reader.getCurrentBsonType();
				if (bsonType == BsonType.NULL) {
					reader.readNull();
					continue;
				}
				if (bsonType == BsonType.OBJECT_ID) {
					reader.readObjectId();
				} else {
					logger.error("No setter for " + fieldName + " and cannot handle bsonType " + bsonType);
				}
			}
		}
		reader.readEndDocument();
		return instance;
	}

	protected abstract T instantiate();
}
