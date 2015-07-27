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
import java.util.ServiceLoader;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;

import com.sleepcamel.bsoneer.processor.codeprovider.ArrayEncodeCodeProvider;
import com.sleepcamel.bsoneer.processor.codeprovider.CollectionEncodeCodeProvider;
import com.sleepcamel.bsoneer.processor.codeprovider.DateEncodeCodeProvider;
import com.sleepcamel.bsoneer.processor.codeprovider.EncodeCodeProvider;
import com.sleepcamel.bsoneer.processor.codeprovider.EntryEncodeCodeProvider;
import com.sleepcamel.bsoneer.processor.codeprovider.EnumEncodeCodeProvider;
import com.sleepcamel.bsoneer.processor.codeprovider.MapEncodeCodeProvider;
import com.sleepcamel.bsoneer.processor.codeprovider.NameGenerator;
import com.sleepcamel.bsoneer.processor.codeprovider.PassThroughEncodeCodeProvider;
import com.sleepcamel.bsoneer.processor.domain.Bean;
import com.sleepcamel.bsoneer.processor.domain.Property;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec.Builder;

class GetterGenerator {

	Collection<EncodeCodeProvider> codeProviders;
	private Bean bean;
	
	public GetterGenerator(ProcessingEnvironment processingEnv,
			Bean b) {
		this.bean = b;
		NameGenerator nameGenerator = new NameGenerator();
		codeProviders = new ArrayList<EncodeCodeProvider>();
		ServiceLoader<EncodeCodeProvider> load = ServiceLoader.load(EncodeCodeProvider.class, getClass().getClassLoader());
		if ( load != null ){
			Iterator<EncodeCodeProvider> iterator = load.iterator();
			while(iterator.hasNext()){
				codeProviders.add(iterator.next());
			}
		}
		codeProviders.add(new DateEncodeCodeProvider());
		codeProviders.add(new EnumEncodeCodeProvider());
		codeProviders.add(new MapEncodeCodeProvider());
		codeProviders.add(new EntryEncodeCodeProvider());
		codeProviders.add(new ArrayEncodeCodeProvider(nameGenerator));
		codeProviders.add(new CollectionEncodeCodeProvider(nameGenerator));
		codeProviders.add(new PassThroughEncodeCodeProvider());
	}

	protected void writeBody(Builder p) {
		for (Property property : bean.getOwnAndInheritedProperties()) {
			TypeMirror key = property.getResolvedType();
			
			String accessName = property.getGetterCall();
			boolean isPrimitive = key.getKind().isPrimitive();

			for (String bsonName : property.getBsonNames()) {
				com.squareup.javapoet.CodeBlock.Builder codeBuilder = CodeBlock
						.builder();
				codeBuilder.addStatement("writer.writeName(\"$L\")", bsonName);
				
				writeBody(codeBuilder, key, "value." + property.getGetterCall());

				CodeBlock codeBlock = codeBuilder.build();
				if ("_id".equals(bsonName)) {
					codeBlock = writeAsId(codeBlock, false);
				}
				if (isPrimitive) {
					p.addCode(codeBlock);
				} else {
					writeCheckingForNull(p, accessName, codeBlock);
				}
			}
		}
	}
	
	protected void writeBody(com.squareup.javapoet.CodeBlock.Builder codeBuilder, TypeMirror typeMirror, String variable) {
		for(EncodeCodeProvider scp:codeProviders){
			if ( scp.applies(typeMirror) ){
				scp.putEncodeCode(codeBuilder, typeMirror, codeProviders, variable);
				break;
			}
		}
	}

	public CodeBlock writeAsId(CodeBlock cb, boolean includeName) {
		com.squareup.javapoet.CodeBlock.Builder builder = CodeBlock.builder();
		builder.beginControlFlow("if (encoderContext.isEncodingCollectibleDocument())");
		if (includeName) {
			builder.addStatement("writer.writeName(\"_id\")");
		}
		builder.add(cb);
		builder.endControlFlow();
		return builder.build();
	}

	public void writeCheckingForNull(Builder p, String tn, CodeBlock cb) {
		p.beginControlFlow("if (value.$L != null)", tn);
		p.addCode(cb);
		p.endControlFlow();
	}
}
