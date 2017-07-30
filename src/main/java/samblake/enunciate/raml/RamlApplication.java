package samblake.enunciate.raml;

import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.ResourceApi;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.api.services.ServiceApi;
import org.raml.api.RamlResource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RamlApplication {

    private final List<ResourceApi> resourceApis;
    private final List<ServiceApi> serviceApis;
    private final Set<Syntax> syntaxes;

    public RamlApplication(List<ResourceApi> resourceApis, List<ServiceApi> serviceApis, Set<Syntax> syntaxes) {
        this.resourceApis = resourceApis;
        this.serviceApis = serviceApis;
        this.syntaxes = syntaxes;
    }

    public List<RamlResource> buildResources() {

        List<RamlResource> resources = resourceApis.stream()
                .flatMap(resourceApi -> createResources(resourceApi))
                .collect(Collectors.toList());

        /*
        for (ServiceApi serviceApi : serviceApis) {
            for (ServiceGroup serviceGroup : serviceApi.getServiceGroups()) {
                for (Service service : serviceGroup.getServices()) {
                    service.getPath()
                }
            }
        }
        */

        return resources;
    }

    private Stream<RamlResource> createResources(ResourceApi resourceApi) {
        return resourceApi.getResourceGroups().stream().flatMap(this::createResources);
    }

    private Stream<RamlResource> createResources(ResourceGroup resourceGroup) {
        return resourceGroup.getResources().stream().map(RamlResourceAdapter::new);
    }
}
