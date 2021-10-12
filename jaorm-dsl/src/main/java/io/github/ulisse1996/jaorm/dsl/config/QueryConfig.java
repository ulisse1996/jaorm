package io.github.ulisse1996.jaorm.dsl.config;

public class QueryConfig {

    private final boolean caseInsensitive;
    private final WhereChecker checker;

    private QueryConfig(QueryConfig.Builder builder) {
        this.caseInsensitive = !builder.caseSensitive;
        this.checker = builder.checker;
    }

    public WhereChecker getChecker() {
        return checker;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private boolean caseSensitive;
        private WhereChecker checker;

        private Builder() {
            this.caseSensitive = true;
            this.checker = new DefaultWhereChecker();
        }

        public Builder caseInsensitive() {
            this.caseSensitive = false;
            return this;
        }

        public Builder caseSensitive() {
            this.caseSensitive = true;
            return this;
        }

        public Builder withWhereChecker(WhereChecker checker) {
            this.checker = checker;
            return this;
        }

        public QueryConfig build() {
            return new QueryConfig(this);
        }
    }
}
