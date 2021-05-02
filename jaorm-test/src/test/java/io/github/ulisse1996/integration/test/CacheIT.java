package io.github.ulisse1996.integration.test;

import io.github.ulisse1996.integration.test.entity.Role;
import io.github.ulisse1996.integration.test.query.RoleDAO;
import io.github.ulisse1996.spi.QueriesService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

class CacheIT extends AbstractIT {

    @ParameterizedTest
    @MethodSource("getSqlTests")
    void should_return_all_saved_roles(HSQLDBProvider.DatabaseType databaseType, String initSql) {
        setDataSource(databaseType, initSql);

        Role role = new Role();
        role.setRoleId(1);
        role.setRoleName("NAME1");
        Role role2 = new Role();
        role2.setRoleId(2);
        role2.setRoleName("NAME2");

        RoleDAO dao = QueriesService.getInstance().getQuery(RoleDAO.class);
        dao.insert(Arrays.asList(role, role2));

        List<Role> roles = dao.readAll(); // Cache first load
        List<Role> rolesAfter = dao.readAll();

        Assertions.assertEquals(roles, rolesAfter);
        Assertions.assertSame(roles, rolesAfter);
    }
}
