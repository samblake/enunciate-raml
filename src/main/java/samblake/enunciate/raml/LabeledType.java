package samblake.enunciate.raml;

import com.webcohesion.enunciate.api.resources.Parameter;

import java.lang.reflect.Type;

public class LabeledType implements Type {
    private final String typeName;
    private final String typeLabel;

    public LabeledType(String typeName, String typeLabel) {
        this.typeName = typeName;
        this.typeLabel = typeLabel;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public static Type from(Parameter parameter) {
        return new LabeledType(parameter.getTypeName(), parameter.getTypeLabel());
    }
}