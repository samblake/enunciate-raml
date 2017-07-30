package samblake.enunciate.raml;

import com.webcohesion.enunciate.EnunciateConfiguration;

public class RamlConfiguration {

    private final EnunciateConfiguration configuration;

    public RamlConfiguration(EnunciateConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getTitle() {
        return configuration.getTitle();
    }

    public String getBaseUri() {
        return configuration.getApplicationRoot() == null ? "/"
                : configuration.getApplicationRoot();
    }

    public String getVersion() {
        return configuration.getVersion();
    }

}
