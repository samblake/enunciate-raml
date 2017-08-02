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
import com.webcohesion.enunciate.util.freemarker.FileDirective;
import freemarker.cache.URLTemplateLoader;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singletonList;

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
        return singletonList((DependencySpec) new DependencySpec() {
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

                srcDir.mkdirs(); // make sure the src dir exists
                Map<String, Object> model = createModel(context, resourceApis, syntaxes, serviceApis, srcDir);
                processTemplate(getRamlTemplateURL(), model);
            }
            else {
                info("Skipping RAML generation as everything appears up-to-date...");
            }

            createArtifact(srcDir);
        }
        catch (IOException | TemplateException e) {
            throw new EnunciateException("Could not generate RAML", e);
        }
    }

    /**
     * Processes the specified template with the given model.
     *
     * @param templateURL The template URL.
     * @param model       The root model.
     */
    private void processTemplate(URL templateURL, Object model) throws IOException, TemplateException {
        debug("Processing template %s.", templateURL);
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_22);

        configuration.setTemplateLoader(new URLTemplateLoader() {
            protected URL getURL(String name) {
                try {
                    return new URL(name);
                }
                catch (MalformedURLException e) {
                    return null;
                }
            }
        });

        configuration.setTemplateExceptionHandler(new TemplateExceptionHandler() {
            public void handleTemplateException(TemplateException templateException, Environment environment, Writer writer) throws TemplateException {
                throw templateException;
            }
        });

        configuration.setLocalizedLookup(false);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setURLEscapingCharset("UTF-8");
        Template template = configuration.getTemplate(templateURL.toString());
        StringWriter unhandledOutput = new StringWriter();
        template.process(model, unhandledOutput);
        debug("Freemarker processing output:\n%s", unhandledOutput);
    }

    /**
     * The url to the freemarker XML processing template that will be used to transforms the docs.xml to the site documentation. For more
     * information, see http://freemarker.sourceforge.net/docs/xgui.html
     *
     * @return The url to the freemarker XML processing template.
     */
    public File getFreemarkerTemplateFile() {
        String templatePath = this.config.getString("[@freemarkerTemplate]");
        return templatePath == null ? null : resolveFile(templatePath);
    }

    /**
     * The URL to the Freemarker template for processing the base documentation xml file.
     *
     * @return The URL to the Freemarker template for processing the base documentation xml file.
     */
    private URL getRamlTemplateURL() throws MalformedURLException {
        File templateFile = getFreemarkerTemplateFile();
        if (templateFile != null && !templateFile.exists()) {
            warn("Unable to use freemarker template at %s: file doesn't exist!", templateFile);
            templateFile = null;
        }

        if (templateFile != null) {
            return templateFile.toURI().toURL();
        }
        else {
            return RamlModule.class.getResource("raml.fmt");
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
    private String getSlug() {
        return this.config.getString("[@slug]", this.enunciate.getConfiguration().getSlug());
    }

    private Map<String, Object> createModel(EnunciateContext context, List<ResourceApi> resourceApis,
                                            Set<Syntax> syntaxes, List<ServiceApi> serviceApis, File srcDir) {

        Map<String, Object> model = new HashMap<String, Object>();

        String intro = this.enunciate.getConfiguration().readDescription(context, false);
        if (intro != null) {
            model.put("intro", intro);
        }

        String copyright = this.enunciate.getConfiguration().getCopyright();
        if (copyright != null) {
            model.put("copyright", copyright);
        }

        String version = this.enunciate.getConfiguration().getVersion();
        if (version != null) {
            model.put("version", version);
        }

        String applicationRoot = this.enunciate.getConfiguration().getApplicationRoot();
        if (applicationRoot != null) {
            model.put("applicationRoot", applicationRoot);
        }

        String title = this.enunciate.getConfiguration().getTitle();
        model.put("title", title == null ? "Web Service API" : title);

        model.put("file", new FileDirective(srcDir, this.enunciate.getLogger()));
        model.put("fileName", getSlug() + "." + getName());

        model.put("data", syntaxes);
        model.put("resourceApis", resourceApis);
        model.put("serviceApis", serviceApis);

        return model;
    }
}