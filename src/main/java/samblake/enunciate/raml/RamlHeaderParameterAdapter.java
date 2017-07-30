package samblake.enunciate.raml;

import com.webcohesion.enunciate.api.resources.Parameter;
import org.raml.api.RamlHeaderParameter;

public class RamlHeaderParameterAdapter extends RamlParameterAdapter implements RamlHeaderParameter {

    public RamlHeaderParameterAdapter(Parameter parameter) {
        super(parameter);
    }
}
