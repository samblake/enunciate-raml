package samblake.enunciate.raml;

import com.webcohesion.enunciate.api.resources.Parameter;
import org.raml.api.RamlQueryParameter;

public class RamlQueryParameterAdapter extends RamlParameterAdapter implements RamlQueryParameter {

    public RamlQueryParameterAdapter(Parameter parameter) {
        super(parameter);
    }
}
