package com.cramja.rest.core.config;

public class FieldValue<T> {

    private Field<T> field;
    private T value;

    public FieldValue(Field<T> field, T value) {
        this.field = field;
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public static <T> FieldValue<T> of(Field<T> field, Object o) {
        switch (field.getType()) {
            case LONG: {
                Long val;
                if (o instanceof Long) {
                    val = (Long) o;
                } else {
                    try {
                        val = Long.parseLong(String.valueOf(o));
                    } catch (NumberFormatException e) {
                        val = null;
                    }
                }
                return new FieldValue<>(field, (T) val);
            }
            case STRING: {
                return new FieldValue<>(field, (T) String.valueOf(o));
            }
            case BOOLEAN: {
                Boolean val;
                if (o instanceof Boolean) {
                    val = (Boolean) o;
                } else {
                    val = Boolean.parseBoolean(String.valueOf(o));
                }
                return new FieldValue<>(field, (T) val);
            }

            default:
                throw new AssertionError("unhandled config type " + field.getType());
        }
    }

    public Field<T> getField() {
        return field;
    }

    public T getValue() {
        return value;
    }

}
