package com.cramja.rest.core.config;


public abstract class Field<T> {

    public enum Type {
        STRING,
        LONG,
        BOOLEAN
    }

    protected String name;
    protected String description;
    protected boolean optional;
    protected T defaultVal;

    public Field(String name, String description, boolean optional, T defaultVal) {
        this.name = name;
        this.description = description;
        this.optional = optional;
        this.defaultVal = defaultVal;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOptional() {
        return optional;
    }

    public abstract Type getType();

    public static class StringField extends Field<String> {

        protected StringField(String name, String description, boolean optional, String defaultVal) {
            super(name, description, optional, defaultVal);
        }

        public static StringField of(String name, String description) {
            return new StringField(name, description, false, null);
        }

        public static StringField of(String name, String description, String defaultValue) {
            return new StringField(name, description, true, defaultValue);
        }

        @Override
        public Type getType() {
            return Type.STRING;
        }

    }

    public static class LongField extends Field<Long> {

        protected LongField(String name, String description, boolean optional, Long defaultVal) {
            super(name, description, optional, defaultVal);
        }

        public static LongField of(String name, String description) {
            return new LongField(name, description, false, null);
        }

        public static LongField of(String name, String description, long defaultValue) {
            return new LongField(name, description, true, defaultValue);
        }

        @Override
        public Type getType() {
            return Type.LONG;
        }
    }

    public static class BooleanField extends Field<Boolean> {

        protected BooleanField(String name, String description, boolean optional, Boolean defaultVal) {
            super(name, description, optional, defaultVal);
        }

        public static BooleanField of(String name, String description) {
            return new BooleanField(name, description, false, null);
        }

        public static BooleanField of(String name, String description, boolean defaultValue) {
            return new BooleanField(name, description, true, defaultValue);
        }

        @Override
        public Type getType() {
            return Type.BOOLEAN;
        }
    }
}
