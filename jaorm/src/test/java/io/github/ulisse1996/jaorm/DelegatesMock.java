package io.github.ulisse1996.jaorm;

import io.github.ulisse1996.jaorm.entity.SqlColumn;
import io.github.ulisse1996.jaorm.schema.TableInfo;
import io.github.ulisse1996.jaorm.spi.DelegatesService;
import io.github.ulisse1996.jaorm.entity.EntityDelegate;
import io.github.ulisse1996.jaorm.entity.EntityMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public class DelegatesMock extends DelegatesService {

    @Override
    public Map<Class<?>, Supplier<? extends EntityDelegate<?>>> getDelegates() {
        return Collections.singletonMap(MyEntity.class, MyEntityDelegate::new);
    }

    public static class MyEntity {

        private String field1;
        private BigDecimal field2;

        public String getField1() {
            return field1;
        }

        public void setField1(String field1) {
            this.field1 = field1;
        }

        public BigDecimal getField2() {
            return field2;
        }

        public void setField2(BigDecimal field2) {
            this.field2 = field2;
        }
    }

    public static class MyEntityDelegate extends MyEntity implements EntityDelegate<MyEntity> {

        private MyEntity entity;

        @Override
        public String getField1() {
            return entity.getField1();
        }

        @Override
        public BigDecimal getField2() {
            return entity.getField2();
        }

        @Override
        public EntityDelegate<MyEntity> generateDelegate() {
            return new MyEntityDelegate();
        }

        @Override
        public Supplier<MyEntity> getEntityInstance() {
            return MyEntity::new;
        }

        @Override
        public EntityMapper<MyEntity> getEntityMapper() {
            EntityMapper.Builder<MyEntity> builder = new EntityMapper.Builder<>();
            builder.add("FIELD1", String.class, (entity, value) -> entity.setField1((String) value), MyEntity::getField1, false, false);
            builder.add("FIELD2", BigDecimal.class, (entity, value) -> entity.setField2((BigDecimal) value), MyEntity::getField2, false, false);
            return builder.build();
        }

        @Override
        public void setEntity(ResultSet rs) throws SQLException {
            this.entity = toEntity(rs);
        }

        @Override
        public void setFullEntity(MyEntity entity) {
            this.entity = entity;
        }

        @Override
        public void setFullEntityFullColumns(Map<SqlColumn<MyEntity, ?>, ?> columns) {

        }

        @Override
        public MyEntity getEntity() {
            return this.entity;
        }

        @Override
        public String getBaseSql() {
            return "SELECT FIELD1, FIELD2 FROM MYENTITY";
        }

        @Override
        public String getKeysWhere() {
            return "";
        }

        @Override
        public String getKeysWhere(String alias) {
            return null;
        }

        @Override
        public String getInsertSql() {
            return "INSERT INTO MYENTITY (FIELD1, FIELD2) VALUES (?,?)";
        }

        @Override
        public String[] getSelectables() {
            return new String[] {"FIELD1", "FIELD2"};
        }

        @Override
        public String getTable() {
            return "MYENTITY";
        }

        @Override
        public String getUpdateSql() {
            return "UPDATE MYENTITY SET FIELD1 = ? , FIELD2 = ?";
        }

        @Override
        public String getDeleteSql() {
            return "DELETE MYENTITY WHERE FIELD1 = ?";
        }

        @Override
        public boolean isModified() {
            return false;
        }

        @Override
        public boolean isDefaultGeneration() {
            return false;
        }

        @Override
        public MyEntity initDefault(MyEntity entity) {
            return null;
        }

        @Override
        public TableInfo toTableInfo() {
            return null;
        }
    }
}
