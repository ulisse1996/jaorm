package io.github.ulisse1996.jaorm.tools.service.impl;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import io.github.ulisse1996.jaorm.annotation.Query;
import io.github.ulisse1996.jaorm.annotation.Table;
import io.github.ulisse1996.jaorm.tools.exception.EntityValidationException;
import io.github.ulisse1996.jaorm.tools.exception.QueryValidationException;
import io.github.ulisse1996.jaorm.tools.model.ConnectionInfo;
import io.github.ulisse1996.jaorm.tools.model.EntityMetadata;
import io.github.ulisse1996.jaorm.tools.service.AbstractValidator;
import io.github.ulisse1996.jaorm.tools.util.ValidationUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SourceValidator extends AbstractValidator {

    private final List<String> sources;
    private final TypeSolver standardConfig;
    private final List<File> jars;
    private final boolean skipCache;

    public SourceValidator(List<String> sources,
                           String projectRoot,
                           List<File> jars,
                           ConnectionInfo connectionInfo,
                           boolean skipCache) throws IOException {
        super(projectRoot, connectionInfo);
        this.sources = sources;
        this.jars = jars;
        this.skipCache = skipCache;
        this.standardConfig = buildConfig();
    }

    private TypeSolver buildConfig() throws IOException {
        List<TypeSolver> solvers = new ArrayList<>(Collections.singletonList(new ReflectionTypeSolver()));
        for (File jar : this.jars) {
            solvers.add(new JarTypeSolver(jar));
        }
        for (String source : this.sources) {
            solvers.add(new JavaParserTypeSolver(source));
        }
        return new CombinedTypeSolver(solvers);
    }

    @Override
    public void validateEntities() throws EntityValidationException, IOException, SQLException, NoSuchAlgorithmException {
        getLog().info(() -> String.format("Processing Sources in Roots %s", sources));
        for (String source : this.sources) {
            File dir = Paths.get(source).toFile();
            doEntityValidation(dir);
        }
    }

    @Override
    public void validateQueries() throws QueryValidationException, IOException, NoSuchAlgorithmException {
        for (String source : this.sources) {
            File dir = Paths.get(source).toFile();
            doQueriesValidation(dir);
        }
    }

    private void doQueriesValidation(File file) throws QueryValidationException, IOException, NoSuchAlgorithmException {
        if (isJavaFile(file)) {
            validateQuery(file);
        } else if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                for (File sub : subFiles) {
                    doQueriesValidation(sub);
                }
            }
        }
    }

    private void validateQuery(File file) throws QueryValidationException, IOException, NoSuchAlgorithmException {
        ParserConfiguration config = new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(this.standardConfig));
        JavaParser parser = new JavaParser(config);
        CompilationUnit compilationUnit = parser.parse(file).getResult()
                .orElseThrow(() -> new QueryValidationException("Can't parse class"));
        List<ClassOrInterfaceDeclaration> classes = new ArrayList<>(compilationUnit.findAll(ClassOrInterfaceDeclaration.class));
        String calculated = null;
        for (ClassOrInterfaceDeclaration klass : classes) {
            String current = cache.getCurrentHash(file.getAbsolutePath());
            calculated = cache.calculateHash(file.getAbsolutePath());
            if (current.equalsIgnoreCase(calculated) && !this.skipCache) {
                getLog().info(() -> String.format("Skipping calculation for File %s", file.getAbsolutePath()));
                return;
            }
            List<MethodDeclaration> methods = klass.getMethods()
                    .stream()
                    .filter(m -> m.isAnnotationPresent(Query.class))
                    .collect(Collectors.toList());
            for (MethodDeclaration method : methods) {
                String sql = Optional.ofNullable(ValidationUtils.getExpression(method, Query.class, "sql"))
                        .map(Expression::asStringLiteralExpr)
                        .map(StringLiteralExpr::asString)
                        .orElse(null);
                boolean noArgs = Optional.ofNullable(ValidationUtils.getExpression(method, Query.class, "noArgs"))
                        .map(Expression::asBooleanLiteralExpr)
                        .map(BooleanLiteralExpr::getValue)
                        .orElse(false);
                Objects.requireNonNull(sql, "Sql can't be null !");
                validateQuery(sql, noArgs);
            }
        }
        if (!classes.isEmpty()) {
            cache.updateHash(file.getAbsolutePath(), calculated);
        }
    }

    private void doEntityValidation(File file) throws EntityValidationException, IOException, SQLException, NoSuchAlgorithmException {
        if (isJavaFile(file)) {
            validate(file);
        } else if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                for (File sub : subFiles) {
                    doEntityValidation(sub);
                }
            }
        }
    }

    private void validate(File file) throws IOException, EntityValidationException, NoSuchAlgorithmException, SQLException {
        ParserConfiguration config = new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(this.standardConfig));
        JavaParser parser = new JavaParser(config);
        CompilationUnit compilationUnit = parser.parse(file).getResult()
                .orElseThrow(() -> new EntityValidationException("Can't parse class"));
        List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class)
                .stream()
                .filter(t -> t.isAnnotationPresent(Table.class.getSimpleName()))
                .filter(t -> !t.isInterface())
                .collect(Collectors.toList());
        String calculated = null;
        if (!classes.isEmpty()) {
            String current = cache.getCurrentHash(file.getAbsolutePath());
            calculated = cache.calculateHash(file.getAbsolutePath());
            if (current.equalsIgnoreCase(calculated) && !this.skipCache) {
                getLog().info(() -> String.format("Skipping calculation for File %s", file.getAbsolutePath()));
                return;
            }
        }
        for (ClassOrInterfaceDeclaration klass : classes) {
            validateClass(klass);
        }
        if (!classes.isEmpty()) {
            cache.updateHash(file.getAbsolutePath(), calculated);
        }
    }

    private void validateClass(ClassOrInterfaceDeclaration klass) throws EntityValidationException, SQLException {
        String klassName = klass.getNameAsString();
        String table = Optional.ofNullable(ValidationUtils.getExpression(klass, Table.class, "name"))
                .map(Expression::asStringLiteralExpr)
                .map(StringLiteralExpr::asString)
                .orElse(null);
        Objects.requireNonNull(table, "Table can't be null !");
        getLog().info(() -> "Checking Table " + table);
        EntityMetadata entityMetadata = new EntityMetadata(klass);
        validate(klassName, entityMetadata, table);
    }

    private boolean isJavaFile(File file) {
        return file.getName().endsWith(".java");
    }
}
