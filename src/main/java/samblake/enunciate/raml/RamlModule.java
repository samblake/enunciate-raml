package samblake.enunciate.raml;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.api.DefaultRegistrationContext;
import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.ResourceApi;
import com.webcohesion.enunciate.api.services.ServiceApi;
import com.webcohesion.enunciate.artifacts.ArtifactType;
import com.webcohesion.enunciate.artifacts.ClientLibraryArtifact;
import com.webcohesion.enunciate.artifacts.FileArtifact;
import com.webcohesion.enunciate.module.*;
import org.raml.api.RamlApi;
import org.raml.emitter.FileEmitter;
import org.raml.emitter.RamlEmissionException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class RamlModule extends BasicGeneratingModule implements ApiRegistryAwareModule {

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
            File srcDir = getSourceDir();
            if (!isUpToDateWithSources(srcDir)) {
                ApiRegistrationContext registrationContext = new DefaultRegistrationContext();

                List<ResourceApi> resourceApis = this.apiRegistry.getResourceApis(registrationContext);
                Set<Syntax> syntaxes = this.apiRegistry.getSyntaxes(registrationContext);
                List<ServiceApi> serviceApis = this.apiRegistry.getServiceApis(registrationContext);

                if (syntaxes.isEmpty() && serviceApis.isEmpty() && resourceApis.isEmpty()) {
                    warn("No RAML generated: there are no data types, services, or resources to document.");
                    return;
                }

                RamlApi raml = createModel(context, resourceApis, syntaxes, serviceApis);

                srcDir.mkdirs();// make sure the docs dir exists

                File file = new File(srcDir, getSlug() + "." + getName());
                FileEmitter.forFile(file.toPath()).emit(raml);
            }
            else {
                info("Skipping RAML generation as everything appears up-to-date...");
            }

            createArtifact(srcDir);
        }
        catch (RamlEmissionException e) {
            throw new EnunciateException(e);
        }
    }

    private void createArtifact(File srcDir) {
        File packageDir = getPackageDir();
        packageDir.mkdirs();

        File bundle = new File(packageDir, getBundleFileName());
        boolean anyFiles = bundle.exists();
        if (!isUpToDateWithSources(packageDir)) {
            try {
                anyFiles = enunciate.zip(bundle, srcDir);
            }
            catch (IOException e) {
                throw new EnunciateException(e);
            }
        }

        if (anyFiles) {
            ClientLibraryArtifact artifactBundle = new ClientLibraryArtifact(getName(), "raml.library", "RAML Document");
            artifactBundle.setPlatform("RAML");
            FileArtifact sourceScript = new FileArtifact(getName(), "raml.document", bundle);
            sourceScript.setArtifactType(ArtifactType.sources);
            sourceScript.setPublic(false);
            String description = "RAML Document";
            artifactBundle.setDescription(description);
            artifactBundle.addArtifact(sourceScript);
            this.enunciate.addArtifact(artifactBundle);
        }
    }

    protected File getSourceDir() {
        return new File(new File(this.enunciate.getBuildDir(), getName()), "src");
    }

    protected File getPackageDir() {
        return new File(new File(this.enunciate.getBuildDir(), getName()), "build");
    }

    /**
     * The name of the bundle file.
     *
     * @return The name of the bundle file.
     */
    protected String getBundleFileName() {
        return getSlug() + ".zip";
    }

    /**
     * The label for the RAML document.
     *
     * @return The label for the RAML document.
     */
    public String getSlug() {
        return this.config.getString("[@slug]", this.enunciate.getConfiguration().getSlug());
    }

    private RamlApi createModel(EnunciateContext context, List<ResourceApi> resourceApis,
                                Set<Syntax> syntaxes, List<ServiceApi> serviceApis) {

        RamlApplication application = new RamlApplication(resourceApis, serviceApis, syntaxes);
        RamlConfiguration configuration = new RamlConfiguration(context.getConfiguration());
        return new EnunciateRamlApi(configuration, application);
    }
}
