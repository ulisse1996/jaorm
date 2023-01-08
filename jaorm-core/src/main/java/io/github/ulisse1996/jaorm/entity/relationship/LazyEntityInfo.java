package io.github.ulisse1996.jaorm.entity.relationship;

import io.github.ulisse1996.jaorm.entity.sql.SqlParameter;

import java.util.List;

public class LazyEntityInfo {

    private final List<SqlParameter> parameters;
    private final String sql;
    private final boolean fromMany;

    public LazyEntityInfo(List<SqlParameter> parameters, String sql, boolean fromMany) {
        this.parameters = parameters;
        this.sql = sql;
        this.fromMany = fromMany;
    }

    public boolean isFromMany() {
        return fromMany;
    }

    public List<SqlParameter> getParameters() {
        return parameters;
    }

    public String getSql() {
        return sql;
    }
}
