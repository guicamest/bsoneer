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

import com.sleepcamel.bsoneer.processor.domain.Property;
import com.squareup.javapoet.CodeBlock;

public interface EncodeCodeProvider {

	/**
	 * Typically, it should call {@link applies(property.getResolvedType())}
	 * @param property
	 * @return Whether this encoder applies for that {@link com.sleepcamel.bsoneer.processor.domain.Property} 
	 */
	@Deprecated
	public boolean applies(Property property);
	
	/**
	 * @param property
	 * @return Whether this encoder applies for that {@link TypeMirror} 
	 */
	public boolean applies(TypeMirror tm);
	
	/**
	 * Puts the encode code for the given {@link TypeMirror} using the {@link CodeBlock.Builder}, the registered
	 * {@link EncodeCodeProvider}s, and assigning the value to <code>varName</code> if it has to assign the result
	 * @param cb Builder to put the encode code in
	 * @param tm TypeMirror to generate the encode code for
	 * @param allProviders Collection of registered EncodeCodeProviders
	 * @param varName Variable name to use when assigning the result(if applies)
	 */
	public void putEncodeCode(CodeBlock.Builder cb, TypeMirror tm, Collection<EncodeCodeProvider> allProviders, String varName);

}
