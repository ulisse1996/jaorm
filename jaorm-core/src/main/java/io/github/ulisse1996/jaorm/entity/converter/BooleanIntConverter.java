package io.github.ulisse1996.jaorm.entity.converter;

public class BooleanIntConverter implements ValueConverter<Integer, Boolean> {

    public static final BooleanIntConverter INSTANCE = new BooleanIntConverter();

    private BooleanIntConverter() {}

    @Override
    public Boolean fromSql(Integer val) {
        return val != null && val == 1;
    }

    @Override
    public Integer toSql(Boolean val) {
        return val != null && val ? 1 : 0;
    }
}
