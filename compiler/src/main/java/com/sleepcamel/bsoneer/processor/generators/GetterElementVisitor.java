package com.sleepcamel.bsoneer.processor.generators;

import java.util.Collection;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.sleepcamel.bsoneer.processor.util.Util;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeName;

class GetterElementVisitor extends BaseVisitor {

	public GetterElementVisitor(ProcessingEnvironment processingEnv, AnnotationInfo ai) {
		// TODO Support isSmth() getters
		super(processingEnv, "get", false, 0, ai);
	}

	public void writeBody(Builder p) {
		for (TypeMirror tkey : visitedVars.keySet()) {
			for (VarInfo vi : visitedVars.get(tkey)) {
				TypeMirror key = getReplaceTypeIfTypeVar(vi);
				String writeMethod = passThroughMappings.get(key.toString());
				
				String accessName = vi.getMethod();
				accessName += vi.isMethod() ? "()" : "";
				boolean isPrimitive = key.getKind().isPrimitive();
				boolean isJavaArray = key.getKind() == TypeKind.ARRAY;
				if (Util.isEnum(key)) {
					writeMethod = "String";
					accessName += ".name()";
				}
				
				for(String bsonName:vi.getBsonNames()){
					com.squareup.javapoet.CodeBlock.Builder codeBuilder = CodeBlock.builder();
					codeBuilder.addStatement("writer.writeName(\"$L\")", bsonName);
					
					if (writeMethod != null) {
						codeBuilder.addStatement("writer.write$L(value.$L)", writeMethod, accessName);
					} else if (isJavaArray || isJavaCollection(key)) {
						// IF $L is a java.util.Collection or superclass(iface) inside java.lang or an array, call
						// protected void encode(BsonWriter writer, Collection<?> coll, EncoderContext encoderContext) {
//							encode(BsonWriter writer, Object[] coll, EncoderContext encoderContext)
						if ( isJavaArray ){
							codeBuilder.addStatement("encode(writer, value.$L, encoderContext)", accessName);
						}else{
							codeBuilder.addStatement("encode(writer, ($T)value.$L, encoderContext)", TypeName.get(Collection.class), accessName);
						}
					} else {
						if (!isPrimitive) {
							codeBuilder.addStatement("Object v = value.$L", accessName);
							codeBuilder.addStatement("$T c = registry.get(v.getClass())",
								Util.bsonCodecTypeName());
							codeBuilder.addStatement("encoderContext.encodeWithChildContext(c, writer, v)");
						} else {
							throw new RuntimeException("No write method for " + key.toString());
						}
					}
					
					CodeBlock codeBlock = codeBuilder.build();
					if ( "_id".equals(bsonName) ){
						codeBlock = writeAsId(codeBlock, false);
					}
					if ( isPrimitive ){
						p.addCode(codeBlock);
					}else{
						writeCheckingForNull(p, accessName, codeBlock);
					}
				}
			}
		}
	}

	public CodeBlock writeAsId(CodeBlock cb, boolean includeName){
		com.squareup.javapoet.CodeBlock.Builder builder = CodeBlock.builder(); 
		builder.beginControlFlow("if (encoderContext.isEncodingCollectibleDocument())");
		if (includeName) {
			builder.addStatement("writer.writeName(\"_id\")");
		}
		builder.add(cb);
		builder.endControlFlow();
		return builder.build();
	}
	
	public void writeCheckingForNull(Builder p, String tn, CodeBlock cb){
		p.beginControlFlow("if ( value.$L != null )", tn);
		p.addCode(cb);
		p.endControlFlow();
	}
}
