/*
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or at <a href="http://www.apache.org/licenses/LICENSE-2">apache.org</a>.
 */

package io.leangen.geantyref;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TypeVariableImpl<D extends GenericDeclaration> implements TypeVariable<D> {

    private final Map<Class<? extends Annotation>, Annotation> annotations;
    private final D genericDeclaration;
    private final String name;
    private final AnnotatedType[] bounds;

    TypeVariableImpl(TypeVariable<D> variable, AnnotatedType[] bounds) {
        this(variable, variable.getAnnotations(), bounds);
    }

    TypeVariableImpl(TypeVariable<D> variable, Annotation[] annotations, AnnotatedType[] bounds) {
        Objects.requireNonNull(variable);
        this.genericDeclaration = variable.getGenericDeclaration();
        this.name = variable.getName();
        this.annotations = new HashMap<>();
        for (Annotation annotation : annotations) {
            this.annotations.put(annotation.annotationType(), annotation);
        }
        if (bounds == null || bounds.length == 0) {
            throw new IllegalArgumentException("There must be at least one bound. For an unbound variable, the bound must be Object");
        }
        this.bounds = bounds;
    }

    private static AnnotatedType[] annotateBounds(Type[] bounds) {
        if (bounds == null || bounds.length == 0) {
            throw new IllegalArgumentException("There must be at least one bound. For an unbound variable, the bound must be Object");
        }
        return Arrays.stream(bounds).map(GenericTypeReflector::annotate).toArray(AnnotatedType[]::new);
    }

    @Override
    public Type[] getBounds() {
        return Arrays.stream(this.bounds).map(AnnotatedType::getType).toArray(Type[]::new);
    }

    @Override
    public D getGenericDeclaration() {
        return this.genericDeclaration;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public AnnotatedType[] getAnnotatedBounds() {
        return this.bounds;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return (T) this.annotations.get(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotations.values().toArray(new Annotation[annotations.size()]);
    }

    //should this maybe return only annotations directly on the variable?
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return getAnnotations();
    }

    @Override
    public int hashCode() {
        return 127 * (this.getName().hashCode() + Arrays.hashCode(this.getBounds()))
                ^ (this.getGenericDeclaration().hashCode() + Arrays.hashCode(this.getAnnotations()));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TypeVariableImpl)) {
            return false;
        }
        TypeVariableImpl that = (TypeVariableImpl) other;
        return this.getName().equals(that.getName()) && Arrays.equals(this.getAnnotations(), that.getAnnotations())
                && Arrays.equals(this.getBounds(), that.getBounds()) && this.getGenericDeclaration().equals(that.getGenericDeclaration());
    }

    @Override
    public String toString() {
        return annotationsString() + this.getName() + " extends " + typesString(bounds);
    }

    private String annotationsString() {
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
