package samblake.enunciate.raml;

import com.google.common.base.Optional;
import com.webcohesion.enunciate.api.HasAnnotations;

import java.lang.annotation.Annotation;

import static com.google.common.base.Optional.fromNullable;

public class Annotable<T extends HasAnnotations> implements org.raml.api.Annotable {

    private T object;

    public Annotable(T object) {
        this.object = object;
    }

    @Override
    public <A extends Annotation> Optional<A> getAnnotation(Class<A> annotationType) {
        return fromNullable(object.getAnnotation(annotationType));
    }
}
