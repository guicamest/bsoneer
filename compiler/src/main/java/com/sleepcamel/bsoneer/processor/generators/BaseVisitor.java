package com.sleepcamel.bsoneer.processor.generators;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.lang.model.util.Types;

import org.bson.BsonBinary;
import org.bson.BsonDbPointer;
import org.bson.BsonRegularExpression;
import org.bson.BsonTimestamp;
import org.bson.BsonType;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.types.ObjectId;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

abstract class BaseVisitor extends SimpleElementVisitor6<Void, Boolean> {

	private String execPrefix;
	private boolean canReturnVoid;
	private int argQty;
	protected BiMap<TypeMirror, BsonType> mappings = HashBiMap.create();
	private Set<String> visits = new HashSet<String>();

	protected Multimap<TypeMirror, VarInfo> visitedVars = MultimapBuilder.hashKeys().hashSetValues().build();

	protected ImmutableMap<String, String> passThroughMappings = ImmutableMap.<String, String>builder().
			put(int.class.getCanonicalName(), "Int32").put(Integer.class.getCanonicalName(), "Int32").
			//put("short", "Int32").put("java.lang.Short", "Int32").
			put(long.class.getCanonicalName(), "Int64").put(Long.class.getCanonicalName(), "Int64").
//			put("float", "Float").put("java.lang.Float", "Float").
			put(double.class.getCanonicalName(), "Double").put(Double.class.getCanonicalName(), "Double").
			put(boolean.class.getCanonicalName(), "Boolean").put(Boolean.class.getCanonicalName(), "Boolean").
			put(String.class.getCanonicalName(), "String").
			put(BsonBinary.class.getCanonicalName(), "BinaryData").
			put(BsonDbPointer.class.getCanonicalName(), "DBPointer").
			put(ObjectId.class.getCanonicalName(), "ObjectId").
			put(BsonRegularExpression.class.getCanonicalName(), "RegularExpression").
			put(BsonTimestamp.class.getCanonicalName(), "Timestamp").
			build();
	private Types typeUtils;

	public BaseVisitor(ProcessingEnvironment processingEnv, String execPrefix, boolean canReturnVoid, int argQty) {
		this.execPrefix = execPrefix;
		this.canReturnVoid = canReturnVoid;
		this.argQty = argQty;
		Map<BsonType, Class<?>> empty = Collections.emptyMap();
		BsonTypeClassMap bsonTypeClassMap = new BsonTypeClassMap(empty);
		Elements elementUtils = processingEnv.getElementUtils();
		typeUtils = processingEnv.getTypeUtils();
		for (BsonType type : BsonType.values()) {
			Class<?> class1 = bsonTypeClassMap.get(type);
			if (class1 != null) {
				String cName = class1.getCanonicalName();
				TypeElement typeElement = elementUtils.getTypeElement(cName);
				mappings.put(typeElement.asType(), type);
			}
		}
	}

	public boolean visited(String fieldName) {
		return !visits.add(fieldName);
	}

	public Void visitVariable(VariableElement e, Boolean boxPrimitives) {
		if (e.getKind() == ElementKind.RESOURCE_VARIABLE) {
            return visitUnknown(e, boxPrimitives);
		}
		if (e.getModifiers().contains(Modifier.FINAL)
				|| e.getModifiers().contains(Modifier.PRIVATE)) {
			return DEFAULT_VALUE;
		}
		String varName = e.getSimpleName().toString();
		if (!visited(varName)) {
			TypeMirror tm = e.asType();
			if (tm.getKind().isPrimitive() && boxPrimitives) {
				tm = typeUtils.boxedClass((PrimitiveType) tm).asType();
			}
			visitedVars.put(tm, new VarInfo(varName, varName, tm));
		}
		return DEFAULT_VALUE;
	}

	public Void visitExecutable(ExecutableElement e, Boolean boxPrimitives) {
		if (!e.getKind().equals(ElementKind.METHOD)
				|| (!canReturnVoid && e.getReturnType().getKind().equals(TypeKind.VOID))
				|| !e.getSimpleName().toString().startsWith(execPrefix)
				|| e.getParameters().size() != argQty
				|| e.getModifiers().contains(Modifier.PRIVATE)) {
			return DEFAULT_VALUE;
		}
		TypeMirror tm = argQty == 0 ? e.getReturnType() : e.getParameters().get(0).asType();
		String methodName = e.getSimpleName().toString();
		String varName = methodName.substring(3);
		varName = varName.substring(0, 1).toLowerCase() + varName.substring(1);
		if (!visited(varName)) {
			if (tm.getKind().isPrimitive() && boxPrimitives) {
				tm = typeUtils.boxedClass((PrimitiveType) tm).asType();
			}
			visitedVars.put(tm, new VarInfo(varName, methodName, tm));
		}
        return DEFAULT_VALUE;
    }
}
