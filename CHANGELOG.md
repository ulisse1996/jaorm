# CHANGELOG

## 1.8.0
- Feature: Cursor for read large relationships

## 1.7.0
- Feature: SL4J support for standard Logging with custom fallback on SPI JaormLoggerHandler
- Feature: List\<TableRow> support for @Query
- Feature: Merge DSL
- Feature: Extension API for Jaorm Processor validation
- Feature: Default ANSI Sql Query Validator
- Feature: Merge, Count and GeneratedKeys vendor specifics
- Rework: IT Rework using Test Containers
- Bugfix: Missing node name on Graph read
- Bugfix: Missing upsert on single Entity update
- Bugfix: Missing QueryConfig on SubQuery creation

## 1.6.0
- Refactor: Move generation bundle SPI to SPI Provider for Single Feature
- Bugfix: Fix quarkus dev reload for class checks

## 1.5.0
- Feature: Single/Double/Triple Key DAO
- Bugfix: Fix recursive search for BaseDAO

## 1.4.1
- Bugfix: Fix recursive read of valid elements from elements

## 1.4.0
- Feature: DSL Supports for Standard ANSI SQL Functions
- Feature: DSL Case
- Feature: DSL Update with VendorFunction
- Feature: Entity Active Record using EntityRecord
- Feature: Projection Columns

## 1.3.1
- Bugfix: Fix wrong path provider for Windows with IntelliJ Wrapped API

## 1.3.0
- Feature: Improving for Incremental Build
- Removed: Removed Tables, use Jaorm DSL as alternative read

## 1.2.0
- Feature: Page supports for Base Dao and Query Builder

## 1.1.1
- Bugfix: Remove query load for cascade impl
- Bugfix: Fix conversion on id columns

## 1.1.0
- Feature: Automatic set of Relationship columns
- Bugfix: Fix NPE on Listener Service for empty events
- Bugfix: Fix wrong sql server specific name
- Bugfix: Fix Stream support for Entities and Projection

## 1.0.1
- Bugfix: Fix Hibernate Validation Dependency version 

## 1.0.0
- Feature: Schema Support for Entities
- Feature: JSR 380 Validation Support for Entities

## 0.35.0
- Feature: Default value for String, Temporal (Date, LocalDate, Instant ecc.) and Numeric

## 0.34.0
- Feature: Query SQL Reference in files

## 0.33.1
- Bugfix: Fix NPE on Where Clause Checks

## 0.33.0
- Feature: Entity Graph Selection for Entity

## 0.32.0
- Feature: Move Tools Core to specific Project
- Feature: Move IT run to optional profile
- Feature: SQL Validation for Query
- Feature: Maven Plugin skip cache
- Bugfix: Move hash to SHA-512

## 0.31.0
- Feature: Batch Insert/Update for Entity
- Feature: Dsl Support for Insert/Update
- Feature: Lombok Support for NoArgsConstructor
- Feature: Log Sql for Batch Insert/Update

## 0.30.0
- Feature: Projections Support

## 0.29.0
- Feature: Support for Multiple Jaorm SPI Configurations
- Feature: Hash Cache for Jaorm Validation
- Feature: Tables with Suffix
- Feature: Jaorm Validation for Classpath Entities

## 0.28.0
- Feature: Jaorm Tools for Maven
- Bugfix: Fix wrong type on SqlParameter for Relationship

## 0.27.1
- Bugfix: Fix Wrong Where SQL building for Custom Checkers

## 0.27.0
- Feature: SubQuery Support for DSL
- Feature: QueryConfig for Case Sensitive and Where Checker Conditions for DSL

## 0.26.0
- Feature: Custom Functions for DSL

## 0.25.1
- Bugfix: Add missing Aliases for Vendor projects

## 0.25.0
- Feature: Aliases Vendor specific for DSL

## 0.24.0
- Feature: New DSL Builder: QueryBuilder

## 0.23.0
- Feature: Order By Join Column with Alias
- Feature: Where Join with Alias
- Feature: Join With Alias
- Bugfix: Add missing log for SimpleQueryRunner

## 0.22.0
- Feature: Count select for DSL
- Feature: Join Column Where for DSL Joins
- Feature: Processor Log with Messager
- Bugfix: Fix Wrong Upper in where conditions

## 0.21.0
- Feature: Case insensitive for like checks
- Bugfix: Fix README logo

## 0.20.1
- Feature: Move Documento to Jaorm Docs

## 0.20.0
- Feature: Move SQL Specific Syntax to Jaorm Sql Specific Parent Project

## 0.19.1
- Bugfix: Missing Class Entity on Custom Generator

## 0.19.0
- Feature: Move processor to compile plugin annotation processor path (see [README.md](README.md) for more details)

## 0.18.0
- Feature: Add **Tables** , a utility class that can retrieve entity using 
processed keys without using a new Entity instance

## 0.17.0
- Feature: Add custom generation with **@TableGenerated** and **@CustomGenerated**

## 0.16.1
- Bugfix: Fix Service loading exception catch

## 0.16.0
- Feature: Add Column Auto Generated

## 0.15.0
- Feature: Global Listener for Persist, Update and Remove events
- Feature: Feature Configuration for Insert after failed Update

## 0.14.2
- Bugfix: Moved Updated Row from EntityDelete
- Feature: Add **orElse(any)** to Result for Optional compliance

## 0.14.1

- Bugfix: Fix Conversion for Custom Sql Parameter Types
- Bugfix: Fix PreUpdate and PreRemove called after Update/Remove event

## 0.14.0

- Feature: On Relationship **Update** if updated rows equals 0 , 
  Jaorm will try **Insert** if **persist** event is also available for Relationship Node
