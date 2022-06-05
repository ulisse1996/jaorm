package io.github.ulisse1996.jaorm.vendor;

import io.github.ulisse1996.jaorm.entity.SqlColumn;

public class AnsiFunctions {

    private AnsiFunctions() {
        throw new UnsupportedOperationException("No instance for utility class !");
    }

    public static <T> VendorFunction<String> upper(SqlColumn<T, String> column) {
        return new UpperFunction(column);
    }

    public static <T> VendorFunction<String> lower(SqlColumn<T, String> column) {
        return new LowerFunction(column);
    }

    private static class UpperFunction implements VendorFunction<String> {

        private final SqlColumn<?, String> column;

        private UpperFunction(SqlColumn<?, String> column) {
            this.column = column;
        }

        @Override
        public String apply(String alias) {
            return String.format("UPPER(%s.%s)", alias, column.getName());
        }

        @Override
        public boolean isString() {
            return true;
        }
    }

    private static class LowerFunction implements VendorFunction<String> {

        private final SqlColumn<?, String> column;

        private LowerFunction(SqlColumn<?, String> column) {
            this.column = column;
        }

        @Override
        public String apply(String alias) {
            return String.format("LOWER(%s.%s)", alias, column.getName());
        }

        @Override
        public boolean isString() {
            return true;
        }
    }
}
