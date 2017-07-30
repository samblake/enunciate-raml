package samblake.enunciate.raml;

import com.google.common.base.Optional;
import com.webcohesion.enunciate.api.resources.Entity;
import org.raml.api.RamlEntity;

import java.lang.reflect.Type;

import static com.google.common.base.Optional.fromNullable;

public class RamlEntityAdapter extends Annotable<Entity> implements RamlEntity {
    private final Entity entity;

    public RamlEntityAdapter(Entity entity) {
        super(entity);
        this.entity = entity;
    }

    @Override
    public Type getType() {
        // TODO
        return String.class;
    }

    @Override
    public Optional<String> getDescription() {
        return fromNullable(entity.getDescription());
    }

    @Override
    public RamlEntity createDependent(Type type) {
        // TODO
        return null;
    }
}
