# JAORM
Just Another Object-Relational Mapping

![Build Status](https://github.com/ulisse1996/JAORM/workflows/build/badge.svg)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=ulisse1996_JAORM&metric=alert_status)](https://sonarcloud.io/dashboard?id=ulisse1996_JAORM)
[![SonarCloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=ulisse1996_JAORM&metric=coverage)](https://sonarcloud.io/component_measures/metric/coverage/list?id=ulisse1996_JAORM)
[![SonarCloud Bugs](https://sonarcloud.io/api/project_badges/measure?project=ulisse1996_JAORM&metric=bugs)](https://sonarcloud.io/component_measures/metric/reliability_rating/list?id=ulisse1996_JAORM)
[![SonarCloud Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=ulisse1996_JAORM&metric=vulnerabilities)](https://sonarcloud.io/component_measures/metric/security_rating/list?id=ulisse1996_JAORM)


JAORM is a lightweight modular compile-time based Java ORM.

JAORM use **javax.annotation.processing.Processor** for Entity Mapping Generation instead of
**Runtime Reflection API-based** mappers which have high performance cost.

JAORM is divided in modules that are used from main module using **Java SPI**

## Modules

- [Entity Mapper](#Entity)
- [Query Mapper](#Query)
- [Cache](#Cache) (Optional)
- [DSL](#Query-DSL) (Optional)

### Entity

Entity mapping is performed using **Annotation** that are discarded by the compiler after compilation and 
each Entity is mapped using [Delegation Pattern](https://en.wikipedia.org/wiki/Delegation_pattern)

```java
@Table(name = "TABLE")
public class User {

    @Id
    @Column(name = "USERID")
    private int userId;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "DEPARTMENT_ID")
    private int departmentId;

    @Column(name = "RESULT")
    @Converter(BooleanStringConverter.class)
    private boolean result;
}
```

### Query

For each interface that contains a method with **Query** annotation, an implementation is generated.

Implemented Method execute sql value in the annotation and return an object for a non-void method.
If returned Object is an Entity , Entity module create the mapped entity else the first column is returned.

Query supports different arguments matchers likes :

- Standard Wildcards
- A named parameter (es :name)
- Ordered Wildcards (es: ?1,?2)
- At Names (es: @name, @name2)

Query uses parameter name or annotated parameter with **Param** for retrive the
current value

```java
public interface UserDao extends BaseDao<User> {

    @Query(sql = "SELECT * FROM USER WHERE USER_ID = ? AND USERNAME = ?")
    User getUser(String userId, String username);

    @Query(sql = "SELECT * FROM USER WHERE USER_ID = ? AND USERNAME = ?")
    Optional<User> getUserOpt(String userId, String username);

    @Query(sql = "SELECT * FROM USER WHERE USER_ID = ? AND USERNAME = ?")
    List<User> getAllUsers(String userId, String username);

    @Query(sql = "SELECT * FROM USER WHERE USER_ID = :userId AND USERNAME = :username AND NAME = :username")
    User getUserNamed(String userId, String username);

    @Query(sql = "SELECT * FROM USER WHERE USER_ID = :userId AND USERNAME = :username AND NAME = :username")
    User getUserNamed2(String userId, @Param(name = "USERNAME") String name);

    @Query(sql = "SELECT * FROM USER WHERE USER_ID = ?1 AND USERNAME = ?2 AND NAME = ?1")
    User getUserOrdered(String userId, String username);

    @Query(sql = "SELECT * FROM USER WHERE USER_ID = @userId AND USERNAME = @username AND NAME = @username")
    User getUserAtNamed(String userId, String username);

    @Query(sql = "UPDATE USER SET USER_ID = :userId where USERNAME = :username")
    void updateUser(String userId, String username);
}
```
### Cache

### Query DSL