# CHANGELOG

## 0.14.1

- Bugfix: Fix Conversion for Custom Sql Parameter Types
- Bugfix: Fix PreUpdate and PreRemove called after Update/Remove event

## 0.14.0

- Feature: On Relationship **Update** if updated rows equals 0 , 
  Jaorm will try **Insert** if **persist** event is also available for Relationship Node
