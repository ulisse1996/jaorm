import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Query;
import io.github.ulisse1996.jaorm.mapping.Cursor;
import io.github.ulisse1996.jaorm.mapping.TableRow;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface QueryWithList extends BaseDao<io.test.SimpleEntity> {

    @Query(sql = "DELETE FROM MY_TABLE WHERE ONE IN (:list)")
    void deleteAll(List<String> list);

    @Query(sql = "SELECT * FROM MY_TABLE WHERE ONE IN (:list)")
    Optional<TableRow> doReadOpt(List<String> list);

    @Query(sql = "SELECT * FROM MY_TABLE WHERE ONE IN (:list)")
    Stream<TableRow> doReadStream(List<String> list);

    @Query(sql = "SELECT * FROM MY_TABLE WHERE ONE IN (:list)")
    Cursor<io.test.SimpleEntity> readCursor(List<String> list);
}