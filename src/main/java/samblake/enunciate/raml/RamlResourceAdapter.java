package samblake.enunciate.raml;

import com.webcohesion.enunciate.api.resources.Resource;
import org.raml.api.RamlResource;
import org.raml.api.RamlResourceMethod;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RamlResourceAdapter implements RamlResource {
    private final Resource resource;

    public RamlResourceAdapter(Resource resource) {
        this.resource = resource;
    }

    @Override
    public String getPath() {
        return resource.getPath();
    }

    @Override
    public List<RamlResource> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public List<RamlResourceMethod> getMethods() {
        return resource.getMethods().stream()
                .map(RamlResourceMethodAdapter::new)
                .collect(Collectors.toList());
    }
}
