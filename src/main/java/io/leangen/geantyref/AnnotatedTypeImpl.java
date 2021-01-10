/*
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or at <a href="http://www.apache.org/licenses/LICENSE-2">apache.org</a>.
 */

package io.leangen.geantyref;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

class AnnotatedTypeImpl implements AnnotatedType {

    protected Type type;
    protected Map<Class<? extends Annotation>, Annotation> annotations;

    AnnotatedTypeImpl(Type type) {
        this(type, new Annotation[0]);
    }

    AnnotatedTypeImpl(Type type, Annotation[] annotations) {
        this.type = Objects.requireNonNull(type);
        this.annotations = new LinkedHashMap<>();
        for (Annotation annotation : annotations) {
            this.annotations.put(annotation.annotationType(), annotation);
        }
    }

    // @Override // added in Java 9
    public AnnotatedType getAnnotatedOwnerType() {
        if (!(type instanceof Class<?>)) {
            throw new IllegalArgumentException("Should be handled by a subtype");
        }
        final Class<?> ownerType = ((Class<?>) type).getEnclosingClass();
        if (ownerType == null) {
            return null;
        }

        return GenericTypeReflector.annotate(ownerType, this.annotations.values().toArray(new Annotation[0]));
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return (T) annotations.get(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotations.values().toArray(new Annotation[0]);
    }

    //should this maybe return only annotations directly on type?
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return getAnnotations();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AnnotatedType)) {
            return false;
        }
        AnnotatedType that = (AnnotatedType) other;
        return this.getType().equals(that.getType()) && Arrays.equals(this.getAnnotations(), that.getAnnotations());
    }

    @Override
    public int hashCode() {
        return 127 * this.getType().hashCode() ^ Arrays.hashCode(this.getAnnotations());
    }

    @Override
    public String toString() {
        return annotationsString() + GenericTypeReflector.getTypeName(type);
    }

    String annotationsString() {
        return annotations.isEmpty() ? "" : annotations.values().stream()
                .map(Annotation::toString)
                .collect(Collectors.joining(", ")) + " ";
    }

    String typesString(AnnotatedType[] types) {
        return Arrays.stream(types)
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }
}
