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
import com.squareup.javapoet.CodeBlock.Builder;

public class DecodeCodeProviders {

	private Collection<DecodeCodeProvider> allProviders;

	public DecodeCodeProviders(Collection<DecodeCodeProvider> allProviders) {
		this.allProviders = allProviders;
	}
	
	public boolean applies(TypeMirror tm){
		for(DecodeCodeProvider provider:allProviders){
			if (provider.applies(tm)){
				return true;
			}
		}
		return false;
	}

	public CodeBlock getDecodeCode(Property property) {
		for(DecodeCodeProvider provider:allProviders){
			if (provider.applies(property)){
				return provider.getDecodeCode(property, this);
			}
		}
		return null;
	}

	public void putDecodeCode(Builder cb, TypeMirror tm, String variableToUse) {
		putDecodeCode(cb, tm, variableToUse, true);
	}
	
	public void putDecodeCode(Builder cb, TypeMirror tm, String variableToUse, boolean declareVariable) {
		for(DecodeCodeProvider provider:allProviders){
			if (provider.applies(tm)){
				provider.putDecodeCode(cb, tm, this, variableToUse, declareVariable);
				break;
			}
		}
	}

	public boolean hasToInstantiate(TypeMirror tm) {
		for(DecodeCodeProvider provider:allProviders){
			if (provider.applies(tm)){
				return provider.hasToInstantiate();
			}
		}
		return false;
	}
	
	public int variablesUsed(TypeMirror tm){
		for(DecodeCodeProvider provider:allProviders){
			if (provider.applies(tm)){
				return provider.variablesUsed();
			}
		}
		return 0;
	}
}
