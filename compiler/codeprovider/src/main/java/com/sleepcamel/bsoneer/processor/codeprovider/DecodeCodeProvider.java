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

import javax.lang.model.type.TypeMirror;

import com.sleepcamel.bsoneer.processor.domain.Property;
import com.squareup.javapoet.CodeBlock;

public interface DecodeCodeProvider {

	/**
	 * Typically, it should call {@link applies(property.getResolvedType())}
	 * @param property
	 * @return Whether this decoder applies for that {@link com.sleepcamel.bsoneer.processor.domain.Property} 
	 */
	public boolean applies(Property property);
	
	/**
	 * @param property
	 * @return Whether this decoder applies for that {@link TypeMirror} 
	 */
	public boolean applies(TypeMirror tm);
	
	/**
	 * @return Whether this decoder has to instantiate a variable for decoding
	 */
	public boolean hasToInstantiate();
	
	/**
	 * @return The number of variables used for decoding
	 */
	public int variablesUsed();
	
	/**
	 * Returns the {@link CodeBlock} responsible for decoding the given {@link Property}.
	 * It should use the registered {@link DecodeCodeProviders} for decoding inner objects.
	 * Typically it should call {@link #putDecodeCode(com.squareup.javapoet.CodeBlock.Builder, TypeMirror, DecodeCodeProviders, String, boolean)}
	 * @param property
	 * @param decodeProviders Registered DecodeCodeProviders 
	 * @return A CodeBlock responsible for decoding the 
	 */
	public CodeBlock getDecodeCode(Property property, DecodeCodeProviders decodeProviders);
	
	/**
	 * @param cb Builder to put the decode code in
	 * @param tm TypeMirror to generate the decode code for
	 * @param decodeProviders Registered DecodeCodeProviders
	 * @param variableToUse Variable name to use for decoding
	 * @param declareVariable Whether it should declare the variable to use or not(It might have been declared already)
	 */
	public void putDecodeCode(CodeBlock.Builder cb, TypeMirror tm, DecodeCodeProviders decodeProviders, String variableToUse, boolean declareVariable);
	
}
