package io.github.ulisse1996.jaorm.entity.converter;

public interface ValueConverter<T,R> {

    ValueConverter<?, ?> NONE_CONVERTER = new ValueConverter<Object, Object>() {
        @Override
        public Object fromSql(Object val) {
            return val;
        }

        @Override
        public Object toSql(Object val) {
            return val;
        }
    };

    @SuppressWarnings("unchecked")
    static <T,R> ValueConverter<T, R> none() {
        return (ValueConverter<T, R>) NONE_CONVERTER;
    }

    R fromSql(T val);
    T toSql(R val);
}
