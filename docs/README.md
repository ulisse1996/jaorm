# JAORM
Just Another Object-Relational Mapping

![Build Status](https://github.com/ulisse1996/JAORM/workflows/build/badge.svg)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=ulisse1996_JAORM&metric=alert_status)](https://sonarcloud.io/dashboard?id=ulisse1996_JAORM)
[![SonarCloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=ulisse1996_JAORM&metric=coverage)](https://sonarcloud.io/component_measures/metric/coverage/list?id=ulisse1996_JAORM)
[![SonarCloud Bugs](https://sonarcloud.io/api/project_badges/measure?project=ulisse1996_JAORM&metric=bugs)](https://sonarcloud.io/component_measures/metric/reliability_rating/list?id=ulisse1996_JAORM)
[![SonarCloud Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=ulisse1996_JAORM&metric=vulnerabilities)](https://sonarcloud.io/component_measures/metric/security_rating/list?id=ulisse1996_JAORM)


JAORM is a lightweight modular compile-time based Java ORM.

JAORM use **Java Annotation Processor [JSR 269](https://jcp.org/en/jsr/detail?id=269)** for Entity Mapping Generation instead of
**Runtime Reflection API-based** mappers which have high performance cost.

JAORM is divided in modules that are used from main module using **Java SPI**

## Modules

- [Core (Entity Mapper and Query)](#10-core)
- [Cache](#20-cache)
- [DSL (Domain-Specific Language)](#30-dsl-domain-specific-language)

### **1.0** Core

Core Module contains [Entity Mapper](#11-entity-mapper) and [Query](#12-query) annotated services

User must define a custom implementation of **DataSourceProvider** that return an instance of **javax.sql.DataSource**
for execute Sql Statements

#### **1.1** Entity Mapper

An Entity is a POJO representing data that can be persisted to the database

```java
@Table(name = "TABLE_NAME")
public class Entity {
    
    @Id
    @Column(name = "COLUMN_1")
    private String column1;
    
    @Column(name = "COLUMN_2")
    private int column2;
}
```

Each POJO annotated with **@Table** annotation , is considered an Entity.

Each entity could contain :

- 1..1 **@Table** annotation that represent database table name
- 1..1 **@Cacheable** annotation that define an Entity as cacheable
- 1..N **@Column** annotated fields that represent one-to-one column in database table
- 0..N **@Id** annotated fields that represent a key column in database table
- 0..N **@Converter** annotated fields that represent a logical conversion between a sql type, 
  and a custom/not supported type
- 0..N **@Relationship** annotated fields that represent a logical relationship between two database tables

For each **@Column** annotated fields , user must also define a getter, and a setter for that field

A standard Constructor with no args must also be provided

##### **1.1.1** Id

In contrast to JPA , Jaorm does not use compound id class

User can declare one or more **@Id** annotation in a single entity that are used to define how Entity must be selected 
from database

##### **1.1.2** Converter

User can define a custom Converter between a custom defined type
(or a simple different type from the one defined on table column) and table column referenced by **@Column** annotation
using an implementation of **ValueConverter<T,R>** where **T** is the table column type and **R** is the custom defined type

Jaorm contains two custom implementation for Boolean type :

- BooleanStringConverter, that convert a matching "Y" string to true , otherwise to false
- BooleanIntConverter, that convert 1 to true, otherwise to false

Each of them contains standard implementation for ValueConverter with a public static instance (aka Singleton) named INSTANCE.

User can follow standard implementation (defining a INSTANCE field) or defining a public constructor

##### **1.1.3** Relationship

User can define one of One To One, One To Many, Many To Many relationships between two entities using **@Relationship**
annotation on a field.

Each **@Relationship** must contain 1..N **@RelationshipColumn**

Each **@RelationshipColumn** could contain :

- a default value , for relationship with a constant value
- a converter from ones defined in **ParameterConverter** that convert default value in a different sql type
- a source column , that match a table column defined in current entity
- a target column , that match a table column defined in relationship entity

The following table define how user must use **@RelationshipColumn**

| |**Default Value** | **Converter** | **Source Column** | **Target Column** |
| ----------- | ----------- | ----------- | ----------- | ----------- |
| **Default Value**| | <span style="color:LimeGreen">Optional | <span style="color:blue">Not Required | <span style="color:Maroon">Required |
| **Converter**  | <span style="color:Maroon">Required | | <span style="color:blue">Not Required | <span style="color:Maroon">Required |
| **Source Column** | <span style="color:blue">Not Required | <span style="color:blue">Not Required | | <span style="color:Maroon">Required |

Supported type for **@Relationship** annotated fields are :

- **java.util.Optional** , for an optional One to One relationship
- **java.util.List** , for One To Many or Many To Many relationship
- a User defined Entity, for One to One relationship

##### **1.1.4** Equals

For Equals check between two Entities , **EntityComparator** must be used if **equals** implementation use **getClass()**
checks because of **Delegation Pattern** used for Entity mapping. 

```java
public class Test {

  public static void main(String[] args) { 
    User user1 = new User();
    User user2 = new User();
    EntityComprator.getInstance(User.class)
            .equals(user1, user2);
    EntityComprator.getInstance(User.class)
            .equals(Collections.singletonList(user1), Collections.singletonList(user2));
  }
}
```

For support with **distinct()** in Java 8 **Stream API** , user can also use
**EntityComparator.distinct(Function\<? super T,?> function)**

```java
public class Test {
    
    void method() {
        List<User> users = QueriesService.getQuery(UserDao.class)
                .readAll();
        List<User> usersDistinctId = 
                users.stream()
                      .filter(EntityComparator.distinct(User::getUserId))
                      .collect(Collectors.toList);
    }
}
```

#### **1.2** Query

For each interface that contains a method with **@Query** annotation or is annotated with **@Dao** annotation, an implementation is generated.

Implemented Method execute sql value in the annotation and return an object for a non-void method. If returned Object is an Entity , 
Core module create the mapped entity else the first column is returned.

Query supports different arguments matchers likes :

- Standard Wildcards
- A named parameter (es :name)
- Ordered Wildcards (es: ?1,?2)
- At Names (es: @name, @name2)

Query uses parameter name or annotated parameter with Param for retrive the current value

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
### **2.0** Cache

Cache module implements a key based cache for **@Cacheable** annotated entities.

Each time a request for a select query is done with **DSL** or **@Query** implementation,
an entity is stored with selected keys.
In the followings requests , cached entities are returned if previous request was successful.

Default cache implementation is [Caffeine](https://github.com/ben-manes/caffeine) but user can override it
implementing custom **JaormCache** and **JaormAllCache** using an **AbstractCacheConfiguration**.
If custom implementation is used , user must create an SPI file for **CacheService** and cache module
must be omitted from dependencies.

Default cache implementation require a startup configuration for each entity that are **@Cacheable**
annotated.

```java
@Cacheable
@Table(name = "TABLE_NAME")
public class Entity {
    
    @Id
    @Column(name = "COLUMN_1")
    private String column1;
    
    @Column(name = "COLUMN_2")
    private int column2;
}
```

```java
public class Startup {
    
    public void doStartup() {
        CacheService cacheService = CacheService.getInstance();
        MyCacheConfiguration configuration = MyCacheConfiguration.INSTANCE; 
        // or use default StandardConfiguration in cache module
        
        cacheService.setConfiguration(User.class, configuration);
    }
}
```

### **3.0** DSL (Domain-Specific Language)

DSL is an abstraction over simple SQL Queries using a DSL.
It can be combined with Query annotated implementation or used in a stand-alone class.

Processor check if DSL is present in the classpath during processing annotation phase and create
custom {EntityName}Columns class that contains constant defined columns used for type-safety building

```java
public class Test {

    public static void main(String[] args) {
        Jaorm.select(Entity.class)
                .where(EntityColumns.COL1).eq("TEST")
                .where(EntityColumns.COL2).ne(2)
                .read();
    }
    
    public static class Entity {
        
        @Id
        @Column(name = "COL1")
        private String col1;
        
        @Column(name = "COL2")
        private int col2;
    }
}
```

Supported operations :

| Operation | EL Name | Standard Name |
| --------- | ------  | ------------- |
| = | eq | equalsTo |
| != (<>) | ne | notEqualsTo |
| \> | gt | greaterThan |
| < | lt | lessThan |
| \>= | ge | greaterOrEqualsTo |
| <= | le | lessOrEqualsTo |

DSL also supports :

- In, with an **Iterable** 
- Not in , with an **Iterable**
- IsNull, null check
- isNotNull, not null check
- like , with support for Start, End or Full check (See **LikeType**)
- notLike , with support for Start, End or Full check (See **LikeType**)

DSL produce :

- An **java.util.Optional**, for an optional result, using **readOpt** method
- A **java.util.List**, for a list of result, using **readAll** method
- An Entity instance , for a single result, using **read** method