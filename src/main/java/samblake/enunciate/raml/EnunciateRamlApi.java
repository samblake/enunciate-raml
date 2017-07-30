package samblake.enunciate.raml;

import org.raml.api.RamlApi;
import org.raml.api.RamlMediaType;
import org.raml.api.RamlResource;
import org.raml.api.RamlSupportedAnnotation;

import java.util.Collections;
import java.util.List;

import static org.raml.api.RamlMediaType.UNKNOWN_TYPE;

public class EnunciateRamlApi implements RamlApi {

    private final RamlConfiguration configuration;
    private final RamlApplication application;

    public EnunciateRamlApi(RamlConfiguration configuration, RamlApplication application) {
        this.configuration = configuration;
        this.application = application;
    }

    @Override
    public String getTitle() {
        return configuration.getTitle();
    }

    @Override
    public String getVersion() {
        return configuration.getVersion();
    }

    @Override
    public String getBaseUri() {
        return configuration.getBaseUri();
    }

    @Override
    public List<RamlResource> getResources() {
        return application.buildResources();
    }

    @Override
    public List<RamlSupportedAnnotation> getSupportedAnnotation() {
        return Collections.emptyList();
    }

    @Override
    public RamlMediaType getDefaultMediaType() {
        return UNKNOWN_TYPE;
    }
}
