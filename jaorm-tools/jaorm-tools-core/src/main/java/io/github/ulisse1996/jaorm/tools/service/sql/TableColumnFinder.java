package io.github.ulisse1996.jaorm.tools.service.sql;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TableColumnFinder extends TablesNamesFinder implements OrderByVisitor {

    private final List<TableColumnPair> pairs;
    private final List<TableAliasPair> tables;
    private final List<TableColumnFinder> subQueries;

    public TableColumnFinder() {
        this.pairs = new ArrayList<>();
        this.tables = new ArrayList<>();
        this.subQueries = new ArrayList<>();
        this.init(true);
    }

    @Override
    public void visit(Update update) {
        super.visit(update);
        if (update.getUpdateSets() != null) {
            for (UpdateSet updateSet : update.getUpdateSets()) {
                if (updateSet.getColumns() != null) {
                    updateSet.getColumns().forEach(this::visit);
                }
                if (updateSet.getExpressions() != null) {
                    updateSet.getExpressions().forEach(e -> e.accept(this));
                }
            }
        }
    }

    @Override
    public void visit(Insert insert) {
        visit(insert.getTable());
        if (insert.getSelect() != null) {
            TableColumnFinder finder = new TableColumnFinder();
            this.subQueries.add(finder);
            insert.getSelect().accept(finder);
        } else if (insert.getColumns() != null) {
            insert.getColumns().forEach(this::visit);
        }
    }

    @Override
    public void visit(Column column) {
        pairs.add(new TableColumnPair(Optional.ofNullable(column.getTable()).map(Table::getName).orElse(""), column.getColumnName()));
    }

    @Override
    protected String extractTableName(Table table) {
        this.tables.add(new TableAliasPair(Optional.ofNullable(table.getAlias()).map(Alias::getName).orElse(""), table.getName()));
        return super.extractTableName(table);
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        super.visit(plainSelect);
        if (plainSelect.getOrderByElements() != null) {
            for (OrderByElement orderByElement : plainSelect.getOrderByElements()) {
                orderByElement.accept(this);
            }
        }
    }

    @Override
    public void visit(SubSelect subSelect) {
        TableColumnFinder finder = new TableColumnFinder();
        this.subQueries.add(finder);
        if (subSelect.getWithItemsList() != null) {
            for (WithItem withItem : subSelect.getWithItemsList()) {
                withItem.accept(finder);
            }
        }
        subSelect.getSelectBody().accept(finder);
    }

    @Override
    public void visit(OrderByElement orderBy) {
        orderBy.getExpression().accept(this);
    }

    public List<TableColumnPair> getPairs() {
        return pairs;
    }

    public List<TableAliasPair> getTables() {
        return tables;
    }

    public List<TableColumnFinder> getSubQueries() {
        return subQueries;
    }
}
