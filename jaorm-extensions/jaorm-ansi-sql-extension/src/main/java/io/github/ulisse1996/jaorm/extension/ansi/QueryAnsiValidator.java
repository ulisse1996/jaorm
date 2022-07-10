package io.github.ulisse1996.jaorm.extension.ansi;

import io.github.ulisse1996.jaorm.annotation.Query;
import io.github.ulisse1996.jaorm.extension.api.AbstractValidatorExtension;
import io.github.ulisse1996.jaorm.extension.api.exception.ProcessorValidationException;
import org.teiid.api.exception.query.QueryParserException;
import org.teiid.query.parser.QueryParser;
import org.teiid.query.sql.lang.Command;

import javax.annotation.processing.ProcessingEnvironment;
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
            QueryParser queryParser = QueryParser.getQueryParser();
            Command command = queryParser.parseCommand(sql.toUpperCase());
            command.acceptVisitor(new QueryAnsiVisitor());
        } catch (QueryParserException e) {
            throw new ProcessorValidationException(e);
        }
    }
}
