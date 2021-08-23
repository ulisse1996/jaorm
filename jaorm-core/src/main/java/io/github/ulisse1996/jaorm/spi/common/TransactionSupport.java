package io.github.ulisse1996.jaorm.spi.common;

import io.github.ulisse1996.jaorm.Transaction;
import io.github.ulisse1996.jaorm.entity.sql.DataSourceProvider;

public interface TransactionSupport {

    Transaction getCurrentTransaction();
    void createTransaction();
    DataSourceProvider createDelegate(DataSourceProvider provider);
}
