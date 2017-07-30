package samblake.enunciate.raml;

import com.google.common.base.Optional;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Parameter;
import org.raml.api.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.of;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class RamlResourceMethodAdapter extends Annotable<Method> implements RamlResourceMethod {
    private static final String PARAM_HEADER = "header";
    private static final String PARAM_FORM = "form";
    private static final String PARAM_PATH = "path";
    private static final String PARAM_QUERY = "query";

    private final Method method;

    public RamlResourceMethodAdapter(Method method) {
        super(method);
        this.method = method;
    }

    @Override
    public String getHttpMethod() {
        return method.getHttpMethod();
    }

    @Override
    public List<RamlMediaType> getConsumedMediaTypes() {
        return method.getRequestEntity() == null ? emptyList()
                : method.getRequestEntity().getMediaTypes().stream()
                    .map(RamlMediaTypeAdapter::new)
                    .collect(toList());
    }

    @Override
    public List<RamlMediaType> getProducedMediaTypes() {
        return method.getResponseEntity() == null ? emptyList()
                : method.getResponseEntity().getMediaTypes().stream()
                    .map(RamlMediaTypeAdapter::new)
                    .collect(toList());
    }

    @Override
    public List<RamlQueryParameter> getQueryParameters() {
        return method.getParameters().stream()
                .filter(param -> param.getTypeLabel().equals(PARAM_PATH) || param.getTypeLabel().equals(PARAM_QUERY))
                .map(RamlQueryParameterAdapter::new)
                .collect(toList());
    }

    @Override
    public List<RamlHeaderParameter> getHeaderParameters() {
        return method.getParameters().stream()
                .filter(param -> param.getTypeLabel().equals(PARAM_HEADER))
                .map(RamlHeaderParameterAdapter::new)
                .collect(toList());
    }

    @Override
    public List<RamlFormParameter> getFormParameters() {
        return method.getParameters().stream()
                .filter(param -> param.getTypeLabel().equals(PARAM_FORM))
                .map(RamlFormParameterAdapter::new)
                .collect(toList());
    }

    @Override
    public List<RamlMultiFormDataParameter> getMultiFormDataParameter() {
        return emptyList();
    }

    @Override
    public Optional<String> getDescription() {
        return fromNullable(method.getDescription());
    }

    @Override
    public Optional<RamlEntity> getConsumedType() {
        return method.getRequestEntity() == null ? Optional.absent()
                : of(new RamlEntityAdapter(method.getRequestEntity()));
    }

    @Override
    public Optional<RamlEntity> getProducedType() {
        return method.getResponseEntity() == null ? Optional.absent()
                : of(new RamlEntityAdapter(method.getResponseEntity()));
    }
}
