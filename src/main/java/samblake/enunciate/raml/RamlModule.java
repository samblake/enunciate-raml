package samblake.enunciate.raml;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.api.DefaultRegistrationContext;
import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.ResourceApi;
import com.webcohesion.enunciate.api.services.ServiceApi;
import com.webcohesion.enunciate.artifacts.FileArtifact;
import com.webcohesion.enunciate.module.*;
import org.raml.api.RamlApi;
import org.raml.emitter.FileEmitter;
import org.raml.emitter.RamlEmissionException;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class RamlModule extends BasicGeneratingModule implements ApiRegistryAwareModule, DocumentationProviderModule {

    private File defaultDocsDir;
    private String defaultDocsSubdir;
    private ApiRegistry apiRegistry;

    @Override
    public String getName() {
        return "raml";
    }

    @Override
    public void setApiRegistry(ApiRegistry registry) {
        this.apiRegistry = registry;
    }

    @Override
    public void setDefaultDocsDir(File file) {
        this.defaultDocsDir = file;
    }

    @Override
    public void setDefaultDocsSubdir(String s) {
        this.defaultDocsSubdir = s;
    }

    /**
     * The documentation "base".  The documentation base is the initial contents of the directory
     * where the documentation will be output.  Can be a zip file or a directory.
     *
     * @return The documentation "base".
     */
    public File getBase() {
        String base = this.config.getString("[@base]");
        return base == null ? null : resolveFile(base);
    }

    /**
     * The subdirectory in the web application where the documentation will be put.
     *
     * @return The subdirectory in the web application where the documentation will be put.
     */
    public File getDocsDir() {
        String docsDir = this.config.getString("[@docsDir]");
        return docsDir != null ? resolveFile(docsDir) : this.defaultDocsDir != null ? this.defaultDocsDir : new File(this.enunciate.getBuildDir(), getName());
    }

    public String getDocsSubdir() {
        return this.config.getString("[@docsSubdir]", this.defaultDocsSubdir);
    }


    @Override
    public List<DependencySpec> getDependencySpecifications() {
        // depends on any module that provides something to the api registry.
        return Arrays.asList((DependencySpec) new DependencySpec() {
            @Override
            public boolean accept(EnunciateModule module) {
                return module instanceof ApiFeatureProviderModule;
            }

            @Override
            public boolean isFulfilled() {
                return true;
            }

            @Override
            public String toString() {
                return "all api feature provider modules";
            }
        });
    }

    @Override
    public void call(EnunciateContext context) {
        try {
            File docsDir = getDocsDir();
            String subDir = getDocsSubdir();
            if (subDir != null) {
                docsDir = new File(docsDir, subDir);
            }

            String title = context.getConfiguration().getTitle();
            String name = title.toLowerCase().replace(" ", "-");
            File file = new File(docsDir, name + "." + getName());

            if (!isUpToDateWithSources(file)) {
                ApiRegistrationContext registrationContext = new DefaultRegistrationContext();

                List<ResourceApi> resourceApis = this.apiRegistry.getResourceApis(registrationContext);
                Set<Syntax> syntaxes = this.apiRegistry.getSyntaxes(registrationContext);
                List<ServiceApi> serviceApis = this.apiRegistry.getServiceApis(registrationContext);

                if (syntaxes.isEmpty() && serviceApis.isEmpty() && resourceApis.isEmpty()) {
                    warn("No RAML generated: there are no data types, services, or resources to document.");
                    return;
                }

                RamlApi raml = createModel(context, resourceApis, syntaxes, serviceApis);

                docsDir.mkdirs();// make sure the docs dir exists
                FileEmitter.forFile(file.toPath()).emit(raml);
            }
            else {
                info("Skipping RAML generation as everything appears up-to-date...");
            }

            this.enunciate.addArtifact(new FileArtifact(getName(), "docs", docsDir));
        }
        catch (RamlEmissionException e) {
            throw new EnunciateException(e);
        }

    }

    private RamlApi createModel(EnunciateContext context, List<ResourceApi> resourceApis,
                                Set<Syntax> syntaxes, List<ServiceApi> serviceApis) {

        RamlApplication application = new RamlApplication(resourceApis, serviceApis, syntaxes);
        RamlConfiguration configuration = new RamlConfiguration(context.getConfiguration());
        return new EnunciateRamlApi(configuration, application);
    }
}
