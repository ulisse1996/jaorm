# CHANGELOG

## 0.21.0
- Feature: Case insensitive for like checks

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
