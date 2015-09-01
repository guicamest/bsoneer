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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import com.sleepcamel.bsoneer.processor.BsonProcessor;
import com.sleepcamel.bsoneer.processor.GeneratedClasses;
import com.sleepcamel.bsoneer.processor.codeprovider.CodeUtil;
import com.sleepcamel.bsoneer.processor.domain.Bean;
import com.sleepcamel.bsoneer.processor.domain.Property;
import com.sleepcamel.bsoneer.processor.resolver.PropertyResolvers;
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

public class BsoneeCodecGenerator extends CodecGenerator {

	private AnnotationInfo ai;
	private PropertyResolvers resolvers = new PropertyResolvers();
	private Set<TypeElement> generatedCodecs;
	private Map<TypeElement, JavaFile> codecsToGenerate;

	public BsoneeCodecGenerator(TypeElement type, ProcessingEnvironment processingEnv, AnnotationInfo ai, Map<TypeElement, JavaFile> codecsToGenerate, Set<TypeElement> generatedCodecs) {
		super(type, processingEnv);
		this.ai = ai;
		this.codecsToGenerate = codecsToGenerate;
		this.generatedCodecs = generatedCodecs;
	}

	public JavaFile getJavaFile() {
		ClassName entityClassName = ClassName.get(type);

		TypeElement baseCodecType = processingEnv.getElementUtils()
				.getTypeElement("com.sleepcamel.bsoneer.BaseBsoneerCodec");
		TypeName extendedCodecTypeName = ParameterizedTypeName
				.get(ClassName.get(baseCodecType), entityClassName);

		ClassName bsoneerCodecClassName = Util.bsoneeName(entityClassName,
				GeneratedClasses.BSONEE_COLLECTIBLE_CODEC_SUFFIX);

		TypeSpec.Builder codecBuilder = TypeSpec.classBuilder(bsoneerCodecClassName.simpleName())
		        .addJavadoc(ProcessorJavadocs.GENERATED_BY_BSONEER)
		        .superclass(extendedCodecTypeName)
		        .addModifiers(PUBLIC).addAnnotation(AnnotationSpec.builder(Generated.class)
		        		.addMember("value", "$S", BsonProcessor.class.getCanonicalName())
		        		.build());;

		Bean bean = new Bean(type);
		bean.resolveHierarchyRawTypes();
		resolvers.resolveProperties(bean);
		if ( ai.hasCustomId() ){
			Property property = bean.getProperty(ai.getIdProperty());
			if ( property == null ){
				error(BsonProcessor.ID_PROPERTY_NOT_FOUND, type);
				return null;
			}
			List<String> bsonNames = property.getBsonNames();
			if ( !ai.isKeepNonIdProperty() ){
				bsonNames.clear();
			}
			bsonNames.add("_id");
		}
		
		for(Property p:bean.getProperties().values()){
			if ( !p.hasSetterAndGetter() ){
				warn(String.format(BsonProcessor.PROPERTY_DOES_NOT_HAVE_GETTER_SETTER_PAIR, p.getName()), type);
			}
		}

		addRegConstructor(codecBuilder, entityClassName);
		addEncoderClassMethod(codecBuilder, entityClassName);
		
		addEncodeMethod(codecBuilder, entityClassName, bean);
		addDecodeCode(codecBuilder, entityClassName, bean);
		
		for(Property p:bean.getProperties().values()){
			TypeMirror resolvedType = p.getResolvedType();
			if ( resolvedType instanceof DeclaredType ){
				Element dt = ((DeclaredType) resolvedType).asElement();
				if ( dt != null && dt instanceof TypeElement && !generatedCodecs.contains(dt) && !codecsToGenerate.containsKey(dt) ){
					TypeElement te = (TypeElement) dt;
					codecsToGenerate.put(te, new CodecGenerator(te, processingEnv).getJavaFile());
				}
			}
		}

		return JavaFile.builder(bsoneerCodecClassName.packageName(), codecBuilder.build())
				.addFileComment(ProcessorJavadocs.GENERATED_BY_BSONEER)
				.skipJavaLangImports(false)
				.indent("\t")
				.build();
	}

	private void addRegConstructor(com.squareup.javapoet.TypeSpec.Builder codecBuilder, ClassName entityClassName) {
//		public BaseBsoneerCodec(final CodecRegistry registry, final IdGenerator generator) {
		codecBuilder.addMethod(MethodSpec.constructorBuilder()
				.addParameter(CodeUtil.bsonRegistryParameter())
				.addModifiers(Modifier.PUBLIC)
				.addStatement("super(registry, new $T())", ClassName.get(ai.getIdGeneratorType()))
				.build());
	}

	private void addEncodeMethod(com.squareup.javapoet.TypeSpec.Builder codecBuilder, ClassName entityClassName, Bean bean) {
		Builder methodSpec = MethodSpec.methodBuilder("encodeVariables")
				.addAnnotation(Override.class)
				.addAnnotation(Util.suppressWarningsAnnotation())
				.addModifiers(Modifier.PROTECTED)
				.addParameter(CodeUtil.bsonWriterParameter())
				.addParameter(ParameterSpec.builder(entityClassName, "value").build())
				.addParameter(CodeUtil.bsonEncoderContextParameter())
				.addJavadoc("{@inhericDoc}\n");

		GetterGenerator getterGenerator = new GetterGenerator(processingEnv, bean);
		if (!ai.hasCustomId()) {
			com.squareup.javapoet.CodeBlock.Builder cb = CodeBlock.builder();
			cb.beginControlFlow("");
			TypeMirror generatorReturnTypeMirror = ai.getGeneratorReturnTypeMirror();
			cb.add("$T vid = ", generatorReturnTypeMirror);
			if (ai.customGeneratorIsBsonned()) {
				cb.add("(($T)idGenerator).generate(value)", ClassName.get(ai.getIdGeneratorType()));
			} else {
				cb.add("idGenerator.generate()");
			}
			cb.add(";\n");
			getterGenerator.writeBody(cb, generatorReturnTypeMirror, "vid");
			cb.endControlFlow();
			methodSpec.addCode(getterGenerator.writeAsId(cb.build(), true));
		}

		getterGenerator.writeBody(methodSpec);
		methodSpec.addStatement("super.encodeVariables(writer,value,encoderContext)");

		codecBuilder.addMethod(methodSpec.build());
	}

	private void addDecodeCode(com.squareup.javapoet.TypeSpec.Builder codecBuilder, ClassName entityClassName, Bean bean) {
		codecBuilder.addMethod(MethodSpec.methodBuilder("instantiate")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PROTECTED)
				.addStatement("return new $T()", entityClassName)
				.returns(entityClassName)
				.build());

		SetterGenerator setterGenerator = new SetterGenerator(processingEnv, bean);
		setterGenerator.writeBody(codecBuilder, entityClassName);
	}

}
