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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.bson.codecs.IdGenerator;
import org.bson.codecs.ObjectIdGenerator;

/**
 * Marks class or given classes to generate a specific {@link org.bson.codecs.CollectibleCodec}
 */
@Target({ TYPE })
@Retention(CLASS)
@Documented
public @interface Bsonee {

	/**
	 * @return Class to generate a specific {@link org.bson.codecs.CollectibleCodec}.
	 * 	If it is empty, the annotated class is used for Codec generation
	 */
	Class<?> value() default Object.class;

	/**
	 * @return Name of the property used as id for this entity
	 */
	String id() default "";

	/**
	 * If entity has a custom id, this indicates whether the field used as id should be kept when serializing or not
	 * @return Whether the field used as id should be kept when serializing or not
	 */
	boolean keepIdProperty() default false;

	Class<? extends IdGenerator> idGenerator() default ObjectIdGenerator.class;
}
