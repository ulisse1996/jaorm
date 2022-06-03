package io.github.ulisse1996.jaorm.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings({"rawtypes", "ResultOfMethodCallIgnored"})
@ExtendWith(MockitoExtension.class)
class EntityRecordTest {

    @Mock private EntityRecordDao<EntityRecord<?>> dao;
    private final EntityRecord<?> record = new EntityRecord<Object>() {};

    @Test
    void should_call_read_on_record_dao() {
        try (MockedStatic<EntityRecordDao> mk = Mockito.mockStatic(EntityRecordDao.class)) {
            mk.when(() -> EntityRecordDao.getInstance(Mockito.any()))
                    .thenReturn(dao);
            record.read();

            Mockito.verify(dao)
                    .read(Mockito.any());
        }
    }

    @Test
    void should_call_read_opt_on_record_dao() {
        try (MockedStatic<EntityRecordDao> mk = Mockito.mockStatic(EntityRecordDao.class)) {
            mk.when(() -> EntityRecordDao.getInstance(Mockito.any()))
                    .thenReturn(dao);
            record.readOpt();

            Mockito.verify(dao)
                    .readOpt(Mockito.any());
        }
    }

    @Test
    void should_call_update_on_record_dao() {
        try (MockedStatic<EntityRecordDao> mk = Mockito.mockStatic(EntityRecordDao.class)) {
            mk.when(() -> EntityRecordDao.getInstance(Mockito.any()))
                    .thenReturn(dao);
            record.update();

            Mockito.verify(dao)
                    .update(Mockito.any(EntityRecord.class));
        }
    }

    @Test
    void should_call_insert_on_record_dao() {
        try (MockedStatic<EntityRecordDao> mk = Mockito.mockStatic(EntityRecordDao.class)) {
            mk.when(() -> EntityRecordDao.getInstance(Mockito.any()))
                    .thenReturn(dao);
            record.insert();

            Mockito.verify(dao)
                    .insert(Mockito.any(EntityRecord.class));
        }
    }

    @Test
    void should_call_delete_on_record_dao() {
        try (MockedStatic<EntityRecordDao> mk = Mockito.mockStatic(EntityRecordDao.class)) {
            mk.when(() -> EntityRecordDao.getInstance(Mockito.any()))
                    .thenReturn(dao);
            record.delete();

            Mockito.verify(dao)
                    .delete(Mockito.any(EntityRecord.class));
        }
    }
}
