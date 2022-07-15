package io.github.ulisse1996.jaorm.extension.ansi;

import io.github.ulisse1996.jaorm.extension.api.exception.ProcessorValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teiid.query.sql.LanguageVisitor;
import org.teiid.query.sql.lang.*;
import org.teiid.query.sql.symbol.*;

import java.util.Arrays;
import java.util.List;

public class QueryAnsiVisitor extends LanguageVisitor {

    private static final Logger logger = LoggerFactory.getLogger(QueryAnsiVisitor.class);

    private static final List<String> SUPPORTED = Arrays.asList(
            "AVG", "CORR", "COUNT", "COVAR_POP",
            "COVAR_SAMP", "CUME_DIST", "DENSE_RANK",
            "MIN", "MAX", "PERCENT_RANK", "PERCENTILE_CONT",
            "PERCENTILE_DISC", "RANK", "REGR_AVGX", "REGR_AVGY",
            "REGR_COUNT", "REGR_INTERCEPT", "REGR_R2",
            "REGR_SLOPE", "REGR_SXX", "REGR_SXY", "REGR_SYY",
            "STDDEV_POP", "STDDEV_SAMP", "SUM", "VAR_POP", "VAR_SAMP"
    );

    @Override
    public void visit(Query obj) {
        visit(obj.getSelect());

        if (obj.getCriteria() != null) {
            visitCriteria(obj.getCriteria());
        }
    }

    @Override
    public void visit(Delete obj) {
        visitCriteria(obj.getCriteria());
    }

    @Override
    public void visit(Update obj) {
        obj.getChangeList().getClauses()
                .stream()
                .map(SetClause::getValue)
                .filter(Function.class::isInstance)
                .map(Function.class::cast)
                .forEach(this::visit);

        if (obj.getCriteria() != null) {
            visitCriteria(obj.getCriteria());
        }
    }

    private void visitCriteria(Criteria criteria) {
        if (criteria == null) {
            return;
        }

        if (criteria instanceof CompareCriteria) {
            visit(((CompareCriteria) criteria));
        } else if (criteria instanceof CompoundCriteria) {
            CompoundCriteria compoundCriteria = (CompoundCriteria) criteria;
            for (Criteria c : compoundCriteria.getCriteria()) {
                visitCriteria(c);
            }
        } else if (criteria instanceof SetCriteria) {
            SetCriteria setCriteria = (SetCriteria) criteria;
            for (Object o : setCriteria.getValues()) {
                if (o instanceof Function) {
                    visit((Function) o);
                }
            }
        } else if (criteria instanceof SubquerySetCriteria) {
            SubquerySetCriteria setCriteria = (SubquerySetCriteria) criteria;
            setCriteria.getCommand().acceptVisitor(this);
        } else {
            logger.debug("Can't validate criteria {}", criteria.getType());
        }
    }

    @Override
    public void visit(Select obj) {
        obj.getSymbols().stream()
                .filter(ExpressionSymbol.class::isInstance)
                .map(ExpressionSymbol.class::cast)
                .forEach(this::visit);
    }

    @Override
    public void visit(ExpressionSymbol obj) {
        if (obj.getExpression() instanceof Function) {
            visit(((Function) obj.getExpression()));
        } else if (obj.getExpression() instanceof SearchedCaseExpression) {
            visit((SearchedCaseExpression) obj.getExpression());
        }
    }

    @Override
    public void visit(SearchedCaseExpression obj) {
        for (Object when : obj.getWhen()) {
            checkObject(when);
        }
        for (Object then : obj.getThen()) {
            checkObject(then);
        }
        checkFunction(obj.getElseExpression());
    }

    private void checkObject(Object o) {
        if (o instanceof Criteria) {
            visitCriteria((Criteria) o);
        } else if (o instanceof Expression) {
            checkFunction((Expression) o);
        }
    }

    @Override
    public void visit(CompareCriteria obj) {
        checkFunction(obj.getRightExpression());
        checkFunction(obj.getLeftExpression());
    }

    private void checkFunction(Expression expression) {
        if (expression instanceof Function) {
            visit((Function) expression);
        }
    }

    @Override
    public void visit(Function obj) {
        if (!SUPPORTED.contains(obj.getName())) {
            throw new ProcessorValidationException(
                    String.format("Function %s is not a valid ANSI SQL function !", obj.getName())
            );
        }
    }
}
