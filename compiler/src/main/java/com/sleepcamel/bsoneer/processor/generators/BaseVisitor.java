package com.sleepcamel.bsoneer.processor.generators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
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
	private Elements elementUtils;
	private static Map<TypeMirror, TypeElement> collectionMappings = new HashMap<TypeMirror, TypeElement>();
	private AnnotationInfo ai;
	private boolean foundCustomId = false;

	public BaseVisitor(ProcessingEnvironment processingEnv, String execPrefix, boolean canReturnVoid, int argQty, AnnotationInfo ai) {
		this.execPrefix = execPrefix;
		this.canReturnVoid = canReturnVoid;
		this.argQty = argQty;
		this.ai = ai;
		Map<BsonType, Class<?>> empty = Collections.emptyMap();
		BsonTypeClassMap bsonTypeClassMap = new BsonTypeClassMap(empty);
		elementUtils = processingEnv.getElementUtils();
		typeUtils = processingEnv.getTypeUtils();
		
		for (BsonType type : BsonType.values()) {
			Class<?> class1 = bsonTypeClassMap.get(type);
			if (class1 != null) {
				String cName = class1.getCanonicalName();
				TypeElement typeElement = elementUtils.getTypeElement(cName);
				mappings.put(typeElement.asType(), type);
			}
		}
		addCollectionMapping(BlockingDeque.class, LinkedBlockingDeque.class);
		addCollectionMapping(BlockingQueue.class, LinkedBlockingDeque.class);
		addCollectionMapping(Deque.class, LinkedBlockingDeque.class);
		addCollectionMapping(Queue.class, LinkedBlockingDeque.class);
		addCollectionMapping(List.class, ArrayList.class);
		addCollectionMapping(Set.class, LinkedHashSet.class);
		addCollectionMapping(SortedSet.class, TreeSet.class);
		addCollectionMapping(NavigableSet.class, TreeSet.class);
		addCollectionMapping("java.util.concurrent.TransferQueue", "java.util.concurrent.LinkedTransferQueue");
	}
	
	private void addCollectionMapping(Class<?> a, Class<?> b){
		addCollectionMapping(a.getCanonicalName(), b.getCanonicalName());
	}
	
	private void addCollectionMapping(String aCanonicalName, String bCanonicalName){
		collectionMappings.put(typeUtils.erasure(elementUtils.getTypeElement(aCanonicalName).asType()), elementUtils.getTypeElement(bCanonicalName));
	}

	public boolean visited(String fieldName) {
		return !visits.add(fieldName);
	}

	public Void visitVariable(VariableElement e, Boolean boxPrimitives) {
		if (e.getKind() == ElementKind.RESOURCE_VARIABLE) {
            return visitUnknown(e, boxPrimitives);
		}
		if (e.getModifiers().contains(Modifier.FINAL)
				|| e.getModifiers().contains(Modifier.PRIVATE)
				|| e.getModifiers().contains(Modifier.TRANSIENT)) {
			return DEFAULT_VALUE;
		}
		String varName = e.getSimpleName().toString();
		addVarInfo(varName, varName, e.asType(), boxPrimitives);
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
		addVarInfo(varName, methodName, tm, boxPrimitives);
        return DEFAULT_VALUE;
    }
	
	public boolean customIdNotFound(){
		return ai.hasCustomId() && !foundCustomId;
	}
	
	private void addVarInfo(String varName, String methodName, TypeMirror tm, boolean boxPrimitives){
		boolean accessViaProperty = varName.equals(methodName);
		String bsonName = varName;
		boolean customId = ai.hasCustomId() && ai.getIdProperty().equals(varName);
		String[] otherBsonNames = null;
		if ( customId ){
			foundCustomId  = true;
			if ( !ai.isKeepNonIdProperty() ){
				bsonName = "_id";
			}else{
				otherBsonNames = new String[]{"_id"};
			}
		}
		if (!visited(varName)) {
			if (tm.getKind().isPrimitive() && boxPrimitives) {
				tm = typeUtils.boxedClass((PrimitiveType) tm).asType();
			}
			visitedVars.put(tm, new VarInfo(varName, methodName, tm, accessViaProperty, bsonName, otherBsonNames));
		}
	}
	
	protected boolean isJavaCollection(TypeMirror key) {
		if ( key.getKind() != TypeKind.DECLARED ){
			return false;
		}
		TypeMirror erasuredType = typeUtils.erasure(key);
		return elementUtils.getPackageElement("java.util")
				.equals(elementUtils.getPackageOf(typeUtils.asElement(erasuredType))) && 
				typeUtils.isAssignable(erasuredType,
				typeUtils.erasure(elementUtils.getTypeElement(Collection.class.getCanonicalName()).asType()));
	}
	
	protected TypeMirror boxedArray(TypeMirror tm){
		if ( tm.getKind() != TypeKind.ARRAY ){
			return null;
		}
		ArrayType at = (ArrayType) tm;
		TypeMirror componentType = at.getComponentType();
		TypeKind kind = componentType.getKind();

		TypeMirror boxed = componentType;
		if (kind == TypeKind.ARRAY) {
			boxed = boxedArray(boxed);
		} else if (kind.isPrimitive()) {
			boxed = typeUtils.boxedClass((PrimitiveType) boxed).asType();
		}
		return typeUtils.getArrayType(boxed);
	}
	
	protected TypeMirror typeArg(TypeMirror tm){
		return ((DeclaredType) tm).getTypeArguments().get(0);
	}
	
	protected TypeMirror getJavaCollectionImplementationClass(TypeMirror typeMirror) {
		if ( !isJavaCollection(typeMirror) ){
			throw new RuntimeException("Type "+typeMirror+" is not a java collection");
		}
		DeclaredType declared = (DeclaredType) typeMirror;
		if ( ElementKind.INTERFACE.equals(declared.asElement().getKind()) ){
			TypeMirror dt = declared.getTypeArguments().get(0);
			TypeElement typeElem = collectionMappings.get(typeUtils.erasure(typeMirror));
			return typeUtils.getDeclaredType(typeElem, dt);
		}
		return typeMirror;
	}
}
