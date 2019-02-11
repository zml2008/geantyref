package io.leangen.geantyref;

import static io.leangen.geantyref.GenericTypeReflector.transform;
import static java.util.Arrays.stream;

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public abstract class TypeVisitor {

	private final Map<TypeVariable, AnnotatedTypeVariable> varCache = new IdentityHashMap<>();

	protected AnnotatedType visitParameterizedType(AnnotatedParameterizedType type) {
		AnnotatedType[] params = Arrays.stream(type.getAnnotatedActualTypeArguments())
				.map(param -> transform(param, this))
				.toArray(AnnotatedType[]::new);

		return GenericTypeReflector.replaceParameters(type, params);
	}

	protected AnnotatedType visitWildcardType(AnnotatedWildcardType type) {
		AnnotatedType[] lowerBounds = Arrays.stream(type.getAnnotatedLowerBounds())
				.map(bound -> transform(bound, this))
				.toArray(AnnotatedType[]::new);
		AnnotatedType[] upperBounds = Arrays.stream(type.getAnnotatedUpperBounds())
				.map(bound -> transform(bound, this))
				.toArray(AnnotatedType[]::new);
		WildcardType inner = new WildcardTypeImpl(
				Arrays.stream(upperBounds).map(AnnotatedType::getType).toArray(Type[]::new),
				Arrays.stream(lowerBounds).map(AnnotatedType::getType).toArray(Type[]::new));
		return new AnnotatedWildcardTypeImpl(inner, type.getAnnotations(),
				lowerBounds, upperBounds);
	}

	protected AnnotatedType visitVariable(AnnotatedTypeVariable type) {
		TypeVariable var = (TypeVariable) type.getType();
		if (varCache.containsKey(var)) {
			return varCache.get(var);
		}
		AnnotatedTypeVariableImpl variable = new AnnotatedTypeVariableImpl((TypeVariable<?>) var, type.getAnnotations());
		varCache.put(var, variable);
		AnnotatedType[] bounds = Arrays.stream(type.getAnnotatedBounds())
				.map(bound -> transform(bound, this))
				.toArray(AnnotatedType[]::new);
		variable.init(bounds);
		return variable;
	}

	protected AnnotatedType visitArray(AnnotatedArrayType type) {
		AnnotatedType componentType = transform(type.getAnnotatedGenericComponentType(), this);
		return new AnnotatedArrayTypeImpl(GenericArrayTypeImpl.createArrayType(componentType.getType()), type.getAnnotations(), componentType);
	}

	protected AnnotatedType visitCaptureType(AnnotatedCaptureType type) {
		return type;
	}

	protected AnnotatedType visitClass(AnnotatedType type) {
		return type;
	}

	protected AnnotatedType visitUnmatched(AnnotatedType type) {
		return type;
	}
}
