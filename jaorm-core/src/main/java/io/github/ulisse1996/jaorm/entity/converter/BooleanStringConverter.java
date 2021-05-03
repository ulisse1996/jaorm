package io.github.ulisse1996.jaorm.entity.converter;

public class BooleanStringConverter implements ValueConverter<String, Boolean> {

    public static final BooleanStringConverter INSTANCE = new BooleanStringConverter();

    private BooleanStringConverter() {}

    @Override
    public Boolean fromSql(String val) {
        if (val != null && !val.isEmpty()) {
            return val.equals("Y");
        }
        return Boolean.FALSE;
    }

    @Override
    public String toSql(Boolean val) {
        return val != null && val ? "Y" : "N";
    }
}
