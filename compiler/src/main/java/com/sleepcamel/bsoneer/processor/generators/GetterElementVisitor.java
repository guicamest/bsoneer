package com.sleepcamel.bsoneer.processor.generators;

import java.util.Collection;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;

import com.sleepcamel.bsoneer.processor.util.Util;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeName;

class GetterElementVisitor extends BaseVisitor {

	public GetterElementVisitor(ProcessingEnvironment processingEnv, AnnotationInfo ai) {
		// TODO Support isSmth() getters
		super(processingEnv, "get", false, 0, ai);
	}

	public void writeBody(Builder p) {
		for (TypeMirror key : visitedVars.keySet()) {
			String writeMethod = passThroughMappings.get(key.toString());
			// TODO REFACTOR THIS
			if (!key.getKind().isPrimitive()) {
				for (VarInfo vi : visitedVars.get(key)) {
					String accessName = vi.getMethod();
					accessName += vi.isMethod() ? "()" : "";
					for(String bsonName:vi.getBsonNames()){
						p.beginControlFlow("if ( value.$L != null )", accessName);
						p.addStatement("writer.writeName(\"$L\")", bsonName);
						if (Util.isEnum(key)) {
							p.addStatement("writer.writeString(value.$L.name())", accessName);
						} else {
							if (writeMethod != null) {
								p.addStatement("writer.write$L(value.$L)", writeMethod, accessName);
							} else if (isJavaCollection(key)) {
								// IF $L is a java.util.Collection or superclass(iface) inside java.lang or an array, call
								// protected void encode(BsonWriter writer, Collection<?> coll, EncoderContext encoderContext) {
	//							encode(BsonWriter writer, Collection<?> coll, EncoderContext encoderContext)
								p.addStatement("encode(writer, ($T)value.$L, encoderContext)", TypeName.get(Collection.class), accessName);
							} else {
								p.addStatement("Object v = value.$L", accessName);
								p.addStatement("$T c = registry.get(v.getClass())",
										Util.bsonCodecTypeName());
								p.addStatement("encoderContext.encodeWithChildContext(c, writer, v)");
							}
						}
						p.endControlFlow();
					}
				}
			} else {
				if (writeMethod == null) {
					throw new RuntimeException("No write method for " + key.toString());
				}
				for (VarInfo vi : visitedVars.get(key)) {
					for(String bsonName:vi.getBsonNames()){
						String accessName = vi.getMethod();
						accessName += vi.isMethod() ? "()" : "";
						p.addStatement("writer.write$L(\"$L\", value.$L)",
								writeMethod, bsonName, accessName);
					}
				}
			}
		}
	}

}
