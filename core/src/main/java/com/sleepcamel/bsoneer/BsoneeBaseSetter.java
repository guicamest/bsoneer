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