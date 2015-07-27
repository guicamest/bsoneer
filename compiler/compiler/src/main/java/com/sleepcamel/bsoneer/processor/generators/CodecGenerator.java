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

import static javax.lang.model.element.Modifier.PUBLIC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ServiceLoader;

import javax.annotation.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import com.sleepcamel.bsoneer.processor.BsonProcessor;
import com.sleepcamel.bsoneer.processor.GeneratedClasses;
import com.sleepcamel.bsoneer.processor.codeprovider.CodeUtil;
import com.sleepcamel.bsoneer.processor.codeprovider.DecodeCodeProvider;
import com.sleepcamel.bsoneer.processor.codeprovider.DecodeCodeProviders;
import com.sleepcamel.bsoneer.processor.codeprovider.EncodeCodeProvider;
import com.sleepcamel.bsoneer.processor.codeprovider.EnumDecodeCodeProvider;
import com.sleepcamel.bsoneer.processor.codeprovider.EnumEncodeCodeProvider;
import com.sleepcamel.bsoneer.processor.util.ProcessorJavadocs;
import com.sleepcamel.bsoneer.processor.util.Util;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class CodecGenerator {

	protected TypeElement type;
	protected ProcessingEnvironment processingEnv;
	
	private DecodeCodeProviders decodeCodeProviders;
	private Collection<EncodeCodeProvider> codeProviders;

	public CodecGenerator(TypeElement type, ProcessingEnvironment processingEnv) {
		this.type = type;
		this.processingEnv = processingEnv;
		Collection<DecodeCodeProvider> decodeProviders = new ArrayList<DecodeCodeProvider>();
		ServiceLoader<DecodeCodeProvider> load = ServiceLoader.load(DecodeCodeProvider.class, getClass().getClassLoader());
		if ( load != null ){
			Iterator<DecodeCodeProvider> iterator = load.iterator();
			while(iterator.hasNext()){
				decodeProviders.add(iterator.next());
			}
		}
		decodeProviders.add(new EnumDecodeCodeProvider());
		decodeCodeProviders = new DecodeCodeProviders(decodeProviders);
		
		codeProviders = new ArrayList<EncodeCodeProvider>();
		ServiceLoader<EncodeCodeProvider> loaded = ServiceLoader.load(EncodeCodeProvider.class, getClass().getClassLoader());
		if ( loaded != null ){
			Iterator<EncodeCodeProvider> iterator = loaded.iterator();
			while(iterator.hasNext()){
				codeProviders.add(iterator.next());
			}
		}
		codeProviders.add(new EnumEncodeCodeProvider());
	}

	public JavaFile getJavaFile() {
		ClassName entityClassName = ClassName.get(type);

		TypeElement baseCodecType = processingEnv.getElementUtils()
				.getTypeElement("org.bson.codecs.Codec");
		TypeName extendedCodecTypeName = ParameterizedTypeName
				.get(ClassName.get(baseCodecType), entityClassName);

		ClassName bsoneerCodecClassName = Util.bsoneeName(entityClassName,
				GeneratedClasses.BSONEE_CODEC_SUFFIX);

		TypeSpec.Builder codecBuilder = TypeSpec.classBuilder(bsoneerCodecClassName.simpleName())
		        .addJavadoc(ProcessorJavadocs.GENERATED_BY_BSONEER)
		        .addSuperinterface(extendedCodecTypeName)
		        .addModifiers(PUBLIC).addAnnotation(AnnotationSpec.builder(Generated.class)
		        		.addMember("value", "$S", BsonProcessor.class.getCanonicalName())
		        		.build());

		addEncoderClassMethod(codecBuilder, entityClassName);
		if (!addEncodeMethod(codecBuilder, entityClassName)) {
			return null;
		}
		if (!addDecodeCode(codecBuilder, entityClassName)) {
			return null;
		}

		return JavaFile.builder(bsoneerCodecClassName.packageName(), codecBuilder.build())
				.addFileComment(ProcessorJavadocs.GENERATED_BY_BSONEER)
				.skipJavaLangImports(false)
				.indent("\t")
				.build();
	}
	
	protected void addEncoderClassMethod(com.squareup.javapoet.TypeSpec.Builder codecBuilder, ClassName entityClassName) {
		TypeName clazzName = ParameterizedTypeName.get(ClassName.get(Class.class), entityClassName);
		Builder methodSpec = MethodSpec.methodBuilder("getEncoderClass")
				.addModifiers(Modifier.PUBLIC)
				.returns(clazzName)
				.addJavadoc("{@inhericDoc}\n");
		methodSpec.addStatement("return $T.class", entityClassName);
		codecBuilder.addMethod(methodSpec.build());
	}

	private boolean addEncodeMethod(com.squareup.javapoet.TypeSpec.Builder codecBuilder, ClassName entityClassName) {
		Builder methodSpec = MethodSpec.methodBuilder("encode")
				.addAnnotation(Override.class)
				.addAnnotation(Util.suppressWarningsAnnotation())
				.addModifiers(Modifier.PUBLIC)
				.addParameter(CodeUtil.bsonWriterParameter())
				.addParameter(ParameterSpec.builder(entityClassName, "value").build())
				.addParameter(CodeUtil.bsonEncoderContextParameter())
				.addJavadoc("{@inhericDoc}\n");

		TypeMirror typeMirror = type.asType();
		boolean applied = false;
		for(EncodeCodeProvider scp:codeProviders){
			if ( scp.applies(typeMirror) ){
				com.squareup.javapoet.CodeBlock.Builder builder = CodeBlock.builder();
				scp.putEncodeCode(builder, typeMirror, codeProviders, "value");
				methodSpec.addCode(builder.build());
				codecBuilder.addMethod(methodSpec.build());
				applied = true;
				break;
			}
		}
		return applied;
	}

	private boolean addDecodeCode(com.squareup.javapoet.TypeSpec.Builder codecBuilder, ClassName entityClassName) {
		Builder methodSpec = MethodSpec.methodBuilder("decode")
				.addAnnotation(Override.class)
				.addAnnotation(Util.suppressWarningsAnnotation())
				.addModifiers(Modifier.PUBLIC)
				.addParameter(CodeUtil.bsonReaderParameter())
				.addParameter(CodeUtil.bsonDecoderContextParameter())
				.returns(entityClassName)
				.addJavadoc("{@inhericDoc}\n");
		
		TypeMirror asType = type.asType();
		if (!decodeCodeProviders.applies(asType)) {
			return false;
		}
		com.squareup.javapoet.CodeBlock.Builder builder = CodeBlock.builder();
		if (!decodeCodeProviders.hasToInstantiate(asType)) {
			builder.add("return ");
		}
		decodeCodeProviders.putDecodeCode(builder, asType, "value");
		if (!decodeCodeProviders.hasToInstantiate(asType)) {
			builder.add(";\n");
		}
		methodSpec.addCode(builder.build());
		codecBuilder.addMethod(methodSpec.build());
		return true;
	}

	protected void warn(String msg, Element element) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg,
				element);
	}
	
	protected void error(String msg, Element element) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg,
				element);
	}
}
