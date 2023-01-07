package io.github.ulisse1996.jaorm.vendor.db2;

import io.github.ulisse1996.jaorm.util.ClassChecker;
import io.github.ulisse1996.jaorm.vendor.specific.GeneratedKeysSpecific;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Db2GeneratedKeysSpecific implements GeneratedKeysSpecific {

    private static final Logger logger = LoggerFactory.getLogger(Db2GeneratedKeysSpecific.class);

    @Override
    public String getReturningKeys(Set<String> keys) {
        return "";
    }

    @Override
    public boolean isCustomReturnKey() {
        return false;
    }

    @Override
    public boolean isCustomGetResultSet() {
        return true;
    }

    @Override
    public List<ResultSet> getResultSets(PreparedStatement pr) {
        Class<?> klass = ClassChecker.findClass("com.ibm.db2.jcc.DB2PreparedStatement", Thread.currentThread().getContextClassLoader());
        try {
            if (klass != null) {
                Object real = pr.unwrap(klass);
                Method method = real.getClass().getMethod("getDBGeneratedKeys");
                ResultSet[] arr = (ResultSet[]) method.invoke(real);
                return List.of(arr);
            }
        } catch (Exception ex) {
            logger.error("Can't retrieve generated keys", ex);
        }
        return Collections.emptyList();
    }

    @Override
    public <T> T getReturningKey(ResultSet rs, Map.Entry<String, Class<?>> entry) {
        throw new UnsupportedOperationException();
    }
}
