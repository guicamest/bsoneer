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
package com.sleepcamel.bsoneer.processor.generators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import com.sleepcamel.bsoneer.BsoneeBaseSetter;
import com.sleepcamel.bsoneer.processor.codeprovider.ArrayDecodeCodeProvider;
import com.sleepcamel.bsoneer.processor.codeprovider.CollectionDecodeCodeProvider;
import com.sleepcamel.bsoneer.processor.codeprovider.DateDecodeCodeProvider;
import com.sleepcamel.bsoneer.processor.codeprovider.DecodeCodeProvider;
import com.sleepcamel.bsoneer.processor.codeprovider.DecodeCodeProviders;
import com.sleepcamel.bsoneer.processor.codeprovider.EntryDecodeCodeProvider;
import com.sleepcamel.bsoneer.processor.codeprovider.EnumDecodeCodeProvider;
import com.sleepcamel.bsoneer.processor.codeprovider.MapDecodeCodeProvider;
import com.sleepcamel.bsoneer.processor.codeprovider.PassThroughDecodeCodeProvider;
import com.sleepcamel.bsoneer.processor.domain.Bean;
import com.sleepcamel.bsoneer.processor.domain.Property;
import com.sleepcamel.bsoneer.processor.util.NameGenerator;
import com.sleepcamel.bsoneer.processor.util.Util;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

class SetterGenerator {

	private Bean bean;
	private DecodeCodeProviders decodeCodeProviders;
	
	public SetterGenerator(ProcessingEnvironment processingEnv, Bean b) {
		this.bean = b;
		NameGenerator nameGenerator = new NameGenerator();
		Collection<DecodeCodeProvider> codeProviders = new ArrayList<DecodeCodeProvider>();
		ServiceLoader<DecodeCodeProvider> load = ServiceLoader.load(DecodeCodeProvider.class, getClass().getClassLoader());
		if ( load != null ){
			Iterator<DecodeCodeProvider> iterator = load.iterator();
			while(iterator.hasNext()){
				codeProviders.add(iterator.next());
			}
		}
		codeProviders.add(new ArrayDecodeCodeProvider(nameGenerator));
		codeProviders.add(new DateDecodeCodeProvider());
		codeProviders.add(new EnumDecodeCodeProvider());
		codeProviders.add(new MapDecodeCodeProvider(nameGenerator));
		codeProviders.add(new EntryDecodeCodeProvider());
		codeProviders.add(new CollectionDecodeCodeProvider(nameGenerator));
		codeProviders.add(new PassThroughDecodeCodeProvider());
		decodeCodeProviders = new DecodeCodeProviders(codeProviders);
	}

	public void writeBody(TypeSpec.Builder codecBuilder, ClassName entityClassName) {
		TypeName setterIface = ParameterizedTypeName.get(ClassName.get(BsoneeBaseSetter.class), entityClassName);
		Map<String, String> setters = new LinkedHashMap<String, String>();

		for (Property property : bean.getOwnAndInheritedProperties()) {
			String setterClassName = property.getUpperName() + "Setter";
			setters.put(property.getBsonNames().iterator().next(), setterClassName);
			codecBuilder.addType(createSetterClass(entityClassName, setterIface, setterClassName, property).build());
		}

		Builder setupSetterBuilder = MethodSpec.methodBuilder("setupSetters").addModifiers(Modifier.PROTECTED);
		for (Entry<String, String> entry : setters.entrySet()) {
			setupSetterBuilder.addStatement("settersByName.put(\"$L\",new $L())",
					entry.getKey(), entry.getValue());
		}
		codecBuilder.addMethod(setupSetterBuilder.build());
	}

	private TypeSpec.Builder createSetterClass(ClassName entityClassName, TypeName setterIface, String setterClassName, Property property) {
		TypeSpec.Builder setterBuilder = TypeSpec.classBuilder(setterClassName);

		Builder setterMethod = MethodSpec.methodBuilder("set")
				.addParameter(ParameterSpec.builder(entityClassName, "instance").build())
				.addParameter(Util.bsonReaderParameter())
				.addParameter(Util.bsonDecoderContextParameter())
				.addModifiers(Modifier.PUBLIC);

		setterMethod.addCode(decodeCodeProviders.getDecodeCode(property));
		
		return setterBuilder.addMethod(setterMethod.build()).addSuperinterface(setterIface);
	}

}
