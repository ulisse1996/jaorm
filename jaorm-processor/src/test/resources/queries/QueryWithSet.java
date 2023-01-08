import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Query;

import java.util.Set;

public interface QueryWithSet extends BaseDao<io.test.SimpleEntity> {

    @Query(sql = "DELETE FROM MY_TABLE WHERE ONE IN (:list)")
    void deleteAll(Set<String> list);
}