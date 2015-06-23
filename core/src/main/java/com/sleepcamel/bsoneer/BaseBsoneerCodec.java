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

import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.assertions.Assertions;
import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.IdGenerator;
import org.bson.codecs.ObjectIdGenerator;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

/**
 * Base {@link org.bson.codecs.CollectibleCodec} for bsonee generated codecs
 *
 * @see org.bson.DocumentCodec
 */
public abstract class BaseBsoneerCodec<T> implements CollectibleCodec<T> {

	private static final CodecRegistry DEFAULT_REGISTRY = fromProviders(asList(new ValueCodecProvider(),
			new BsonValueCodecProvider(),
			new DocumentCodecProvider()));

	private static final String ID_FIELD_NAME = null;

	protected final CodecRegistry registry;
	protected final IdGenerator idGenerator;

	protected Map<String, BsoneeBaseSetter<T>> settersByName = new HashMap<String, BsoneeBaseSetter<T>>();

	protected DefaultReader defaultReader;

	/**
	 * Construct a new instance with a default {@link org.bson.codecs.configuration.CodecRegistry}
	 */
	public BaseBsoneerCodec() {
		this(DEFAULT_REGISTRY);
	}

	/**
	 * Construct a new instance with the given registry
	 *
	 * @param registry {@link org.bson.codecs.configuration.CodecRegistry} to be used
	 */
	public BaseBsoneerCodec(final CodecRegistry registry) {
		this.registry = Assertions.notNull("registry", registry);
		this.idGenerator = Assertions.notNull("idGenerator",
				new ObjectIdGenerator());
		defaultReader = DefaultReader.get(registry);
		setupSetters();
	}

	protected void setupSetters() {
	}

	@Override
	public boolean documentHasId(final T document) {
		return true;
		// return document.containsKey(ID_FIELD_NAME);
	}

    @Override
    public BsonValue getDocumentId(final T document) {
        if (!documentHasId(document)) {
            throw new IllegalStateException("The document does not contain an _id");
        }
        return null;

//        Object id = document.get(ID_FIELD_NAME);
//        if (id instanceof BsonValue) {
//            return (BsonValue) id;
//        }
//
//        BsonDocument idHoldingDocument = new BsonDocument();
//        BsonWriter writer = new BsonDocumentWriter(idHoldingDocument);
//        writer.writeStartDocument();
//        writer.writeName(ID_FIELD_NAME);
//        writeValue(writer, EncoderContext.builder().build(), id);
//        writer.writeEndDocument();
//        return idHoldingDocument.get(ID_FIELD_NAME);
    }

    @Override
    public T generateIdIfAbsentFromDocument(final T document) {
        if (!documentHasId(document)) {
//            document.put(ID_FIELD_NAME, idGenerator.generate());
        }
        return document;
    }

	private boolean skipField(final EncoderContext encoderContext,
			final String key) {
		return encoderContext.isEncodingCollectibleDocument()
				&& key.equals(ID_FIELD_NAME);
	}

    /**
	 * {@inhericDoc}
	 */
	public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
		writer.writeStartDocument();
		encodeVariables(writer, value, encoderContext);
		writer.writeEndDocument();
	}
	
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
		while(iterator.hasNext()){
			Object next = iterator.next();
			if ( next == null ){
				writer.writeNull();
			}else{
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
			if ( next == null ){
				writer.writeNull();
			}else{
				Codec codec = registry.get(next.getClass());
				encoderContext.encodeWithChildContext(codec, writer, next);
			}
		}
		writer.writeEndArray();
	}

    protected void encodeVariables(BsonWriter writer, T value, EncoderContext encoderContext){};

    /**
	 * {@inhericDoc}
	 */
	@Override
	public T decode(BsonReader reader, DecoderContext decoderContext) {
		T instance = instantiate();
		reader.readStartDocument();
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			String fieldName = reader.readName();
			BsonType bsonType = reader.getCurrentBsonType();
			if (bsonType == BsonType.NULL) {
				reader.readNull();
				continue;
			}
			BsoneeBaseSetter<T> bsoneeBaseSetter = settersByName.get(fieldName);
			if (bsoneeBaseSetter != null) {
				bsoneeBaseSetter.set(instance, reader, decoderContext);
			} else {
				// TODO Change for logger
				System.out.println("No setter for " + fieldName);
				if (bsonType == BsonType.OBJECT_ID) {
					reader.readObjectId();
				}
			}
		}
		reader.readEndDocument();
		return instance;
	}

	protected abstract T instantiate();
}
