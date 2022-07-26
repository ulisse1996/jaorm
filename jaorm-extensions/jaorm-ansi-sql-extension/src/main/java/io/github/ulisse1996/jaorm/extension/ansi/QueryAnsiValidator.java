package io.github.ulisse1996.jaorm.extension.ansi;

import io.github.ulisse1996.jaorm.annotation.Query;
import io.github.ulisse1996.jaorm.extension.api.AbstractValidatorExtension;
import io.github.ulisse1996.jaorm.extension.api.exception.ProcessorValidationException;
import org.teiid.api.exception.query.QueryParserException;
import org.teiid.query.parser.QueryParser;
import org.teiid.query.sql.lang.Command;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

public class QueryAnsiValidator extends AbstractValidatorExtension {

    @Override
    public Set<Class<? extends Annotation>> getSupported() {
        return Collections.singleton(Query.class);
    }

    @Override
    public void validateSql(String sql, ProcessingEnvironment processingEnvironment) {
        try {
            processingEnvironment.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    String.format("ANSI SQL Validator for %s", sql)
            );
            QueryParser queryParser = QueryParser.getQueryParser();
            Command command = queryParser.parseCommand(sql.toUpperCase());
            command.acceptVisitor(new QueryAnsiVisitor());
        } catch (QueryParserException e) {
            throw new ProcessorValidationException(e);
        }
    }
}
