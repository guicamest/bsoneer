package com.sleepcamel.bsoneer.processor.generators;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;

import com.sleepcamel.bsoneer.processor.util.Util;
import com.squareup.javapoet.MethodSpec.Builder;

class GetterElementVisitor extends BaseVisitor {

	public GetterElementVisitor(ProcessingEnvironment processingEnv) {
		super(processingEnv, "get", false, 0);
	}

	public void writeBody(Builder p) {
		for (TypeMirror key : visitedVars.keySet()) {
			String writeMethod = passThroughMappings.get(key.toString());
			if (!key.getKind().isPrimitive()) {
				for (VarInfo vi : visitedVars.get(key)) {
					String accessName = vi.getMethod();
					accessName += vi.isMethod() ? "()" : "";
					p.beginControlFlow("if ( value.$L != null )", accessName);
					p.addStatement("writer.writeName(\"$L\")", vi.getName());
					if (Util.isEnum(key)) {
						p.addStatement("writer.writeString(value.$L.name())", accessName);
					} else {
						if (writeMethod != null) {
							p.addStatement("writer.write$L(value.$L)", writeMethod, accessName);
						} else {
							p.addStatement("Object v = value.$L", accessName);
							p.addStatement("$T c = registry.get(v.getClass())",
									Util.bsonCodecTypeName());
							p.addStatement("encoderContext.encodeWithChildContext(c, writer, v)");
						}
					}
					p.endControlFlow();
				}
			} else {
				if (writeMethod == null) {
					throw new RuntimeException("No write method for " + key.toString());
				}
				for (VarInfo vi : visitedVars.get(key)) {
					String accessName = vi.getMethod();
					accessName += vi.isMethod() ? "()" : "";
					p.addStatement("writer.write$L(\"$L\", value.$L)",
							writeMethod, vi.getName(), accessName);
				}
			}
		}
	}
}
