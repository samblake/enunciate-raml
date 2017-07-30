package samblake.enunciate.raml;

import com.google.common.base.Optional;
import com.webcohesion.enunciate.api.resources.Parameter;
import org.raml.api.RamlParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static com.google.common.base.Optional.fromNullable;

public class RamlParameterAdapter extends Annotable<Parameter> implements RamlParameter {
    private final Parameter parameter;

    public RamlParameterAdapter(Parameter parameter) {
        super(parameter);
        this.parameter = parameter;
    }

    @Override
    public String getName() {
        return parameter.getName();
    }

    @Override
    public Optional<String> getDefaultValue() {
        return fromNullable(parameter.getDefaultValue());
    }

    @Override
    public Type getType() {
        // TODO
         return String.class;
    }

    @Override
    public <T extends Annotation> Optional<T> getAnnotation(Class<T> annotationType) {
        return fromNullable(parameter.getAnnotation(annotationType));
    }
}