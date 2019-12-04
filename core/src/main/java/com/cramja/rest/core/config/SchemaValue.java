package com.cramja.rest.core.config;

import java.util.HashMap;
import java.util.Map;

public class SchemaValue {

    private Schema schema;
    private Map<String, FieldValue<?>> fieldValues;

    private SchemaValue(
            Schema schema,
            Map<String, FieldValue<?>> fieldValues) {
        this.schema = schema;
        this.fieldValues = fieldValues;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Field<T> field) {
        return (T) fieldValues.get(field.getName()).getValue();
    }

    public static SchemaValueBuilder newBuilder(Schema schema) {
        return new SchemaValueBuilder(schema);
    }

    public static final class SchemaValueBuilder {

        private Schema schema;
        private Map<String, FieldValue<?>> fieldValues;

        private SchemaValueBuilder(Schema schema) {
            this.schema = schema;
            this.fieldValues = new HashMap<>();
            for (Field<?> field : schema.getFields()) {
                if (field.defaultVal != null) {
                    fieldValues.put(field.getName(), FieldValue.of(field, field.defaultVal));
                }
            }
        }

        public SchemaValueBuilder addFieldValue(FieldValue fieldValue) {
            this.fieldValues.put(fieldValue.getField().getName(), fieldValue);
            return this;
        }

        public SchemaValue build() {
            return new SchemaValue(schema, fieldValues);
        }
    }
}
