![Jaorm](logo.png)

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

- Core (Entity Mapper and Query)
- Cache
- DSL (Domain-Specific Language)
- Transaction
- Lombok Support
- SQL Specifics
  - Oracle
  - MySql
  - MS SQL Server
  - PostgreSQL
- Validation
- Extensions
  - ANSI SQL Build Extensions
  - Dependency Injection Extensions
    - Micronaut Extension
    - Jakarta CDI Extension
    - Javax CDI Extension
    - Spring DI Extension

## Features

- Easy and Customizable Entity Mapping with Java Annotations without the use of Java Reflection API
- Powerful abstract DAO with Custom Query
- Type-Safe Query Builder with DSL
- Supports for Spring and JTA Transactions
- Supports for Basic JDBC Transaction with Transaction Module
- Easy and Customizable Entity Cache
- Supports for most of famous RDBMS
- Supports for **@Getter**, **@Setter** and **@Data** Annotations of [Lombok](https://projectlombok.org/) on Entity
- Supports for [JSR 380](https://beanvalidation.org/2.0-jsr380/) Validation on Entity during Persist/Update
- Supports for DI with JavaEE, JakartaEE, Micronaut and Spring

## Use

For use Jaorm , just include desired modules in **dependencies** and provide the following
compile configuration for maven

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.8.1</version>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>io.github.ulisse1996</groupId>
                <artifactId>jaorm-processor</artifactId>
                <version>${jaorm.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
    <executions>
        <execution>
            <id>test-compile</id>
            <goals>
                <goal>testCompile</goal>
            </goals>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>io.github.ulisse1996</groupId>
                        <artifactId>jaorm-processor</artifactId>
                        <version>${jaorm.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </execution>
    </executions>
</plugin>
```

For more details and examples visit : [Jaorm Documentation](https://ulisse1996.github.io/jaorm-docs/)
