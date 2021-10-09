# CHANGELOG

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
