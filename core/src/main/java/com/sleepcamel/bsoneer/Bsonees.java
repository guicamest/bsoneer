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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Provides classes to generate a specific {@link org.bson.codecs.CollectibleCodec}
 */
@Target({ TYPE, METHOD })
@Retention(CLASS)
@Documented
public @interface Bsonees {

	/**
	 * Specified Bsonees must specify the value attribute
	 * @return {@link com.sleepcamel.bsoneer.Bsonee}s to generate a specific {@link org.bson.codecs.CollectibleCodec}.
	 */
	Bsonee[] value() default { };
}
