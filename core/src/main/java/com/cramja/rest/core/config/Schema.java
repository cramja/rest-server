package com.cramja.rest.core.config;

import com.cramja.rest.core.config.SchemaValue.SchemaValueBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Schema {

    private String name;
    private String description;
    private String basePath;
    private List<Field<?>> fields;

    public Schema(
            String name,
            String description,
            String basePath,
            List<Field<?>> fields) {
        this.name = name;
        this.description = description;
        this.basePath = basePath;
        this.fields = fields;
    }

    public static SchemaBuilder newBuilder() {
        return new SchemaBuilder();
    }

    public SchemaValue parse(Map<String, Object> properties) {
        SchemaValueBuilder builder = SchemaValue.newBuilder(this);
        String base = basePath.equals("") ? "" : basePath + ".";
        for (String key : properties.keySet()) {
            if (key.startsWith(base)) {
                String fieldKey = key.substring(base.length());
                for (Field<?> field : fields) {
                    if (field.getName().equals(fieldKey)) {
                        builder.addFieldValue(FieldValue.of(field, properties.get(key)));
                        break;
                    }
                }
            }
        }
        return builder.build();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getBasePath() {
        return basePath;
    }

    public List<Field<?>> getFields() {
        return fields;
    }

    public static final class SchemaBuilder {
        private String name;
        private String description;
        private String basePath;
        private List<Field<?>> fields = new ArrayList<>();

        private SchemaBuilder() {
        }

        public SchemaBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public SchemaBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public SchemaBuilder setBasePath(String basePath) {
            this.basePath = basePath;
            return this;
        }

        public SchemaBuilder addField(Field<?> field) {
            this.fields.add(field);
            return this;
        }

        public Schema build() {
            return new Schema(name, description, basePath, fields);
        }
    }
}
