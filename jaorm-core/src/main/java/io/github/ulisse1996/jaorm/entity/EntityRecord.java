package io.github.ulisse1996.jaorm.entity;

import java.util.Optional;

public interface EntityRecord<R> {

    @SuppressWarnings("unchecked")
    default R read() {
        EntityRecordDao<R> instance = (EntityRecordDao<R>) EntityRecordDao.getInstance(getClass());
        return instance.read((R) this);
    }

    @SuppressWarnings("unchecked")
    default Optional<R> readOpt() {
        EntityRecordDao<R> instance = (EntityRecordDao<R>) EntityRecordDao.getInstance(getClass());
        return instance.readOpt((R) this);
    }

    @SuppressWarnings("unchecked")
    default R update() {
        EntityRecordDao<R> instance = (EntityRecordDao<R>) EntityRecordDao.getInstance(getClass());
        return instance.update((R) this);
    }

    @SuppressWarnings("unchecked")
    default R insert() {
        EntityRecordDao<R> instance = (EntityRecordDao<R>) EntityRecordDao.getInstance(getClass());
        return instance.insert((R) this);
    }

    @SuppressWarnings("unchecked")
    default void delete() {
        EntityRecordDao<R> instance = (EntityRecordDao<R>) EntityRecordDao.getInstance(getClass());
        instance.delete((R) this);
    }
}
