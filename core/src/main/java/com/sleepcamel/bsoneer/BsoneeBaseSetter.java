package com.sleepcamel.bsoneer;

import org.bson.BsonReader;
import org.bson.codecs.DecoderContext;

/**
 * Base Interface for generated setters
 *
 * @param <T> Type of entity
 */
public interface BsoneeBaseSetter<T> {
	/**
	 * Sets a field read using the reader and decoderContext
	 * @param entity Entity which field is going to be set
	 * @param reader Reader to use
	 * @param decoderContext Decoder context to use
	 */
	void set(T entity, BsonReader reader, DecoderContext decoderContext);
}