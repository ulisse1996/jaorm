package io.github.ulisse1996.jaorm.integration.test.cdi;

import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.integration.test.cdi.inject.MyIdentifier;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@EnableWeld
class CDIIT {

    @WeldSetup
    private final WeldInitiator weld = WeldInitiator.performDefaultDiscovery();

    @Inject private CdiApplicationScopedDAO applicationScopedDao;
    @Inject private CdiRequestScopedDAO requestScopedDAO;
    @Inject private CdiSessionScopedDAO sessionScopedDAO;
    @Inject private CdiDefaultScopedDAO defaultScopedDAO;
    @Inject @MyIdentifier(CDIEntity.class) private BaseDao<CDIEntity> producedDao;

    @Test
    void should_find_request_scope_dao_bean() {
        Assertions.assertNotNull(applicationScopedDao);
        Assertions.assertNotNull(requestScopedDAO);
        Assertions.assertNotNull(sessionScopedDAO);
        Assertions.assertNotNull(defaultScopedDAO);
        Assertions.assertNotNull(producedDao);
    }
}
