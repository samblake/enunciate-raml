package samblake.enunciate.raml;

import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import org.raml.api.RamlMediaType;

public class RamlMediaTypeAdapter implements RamlMediaType {

    private final MediaTypeDescriptor mediaType;

    public RamlMediaTypeAdapter(MediaTypeDescriptor mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public String toStringRepresentation() {
        return mediaType.getMediaType();
    }
}
