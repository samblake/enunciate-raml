package samblake.enunciate.raml;

import com.webcohesion.enunciate.api.resources.Parameter;
import org.raml.api.RamlFormParameter;

public class RamlFormParameterAdapter extends RamlParameterAdapter implements RamlFormParameter {

    public RamlFormParameterAdapter(Parameter parameter) {
        super(parameter);
    }
}
