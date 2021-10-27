package io.github.ulisse1996.jaorm.validation.service.impl;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import io.github.ulisse1996.jaorm.annotation.Table;
import io.github.ulisse1996.jaorm.validation.exception.EntityValidationException;
import io.github.ulisse1996.jaorm.validation.model.ConnectionInfo;
import io.github.ulisse1996.jaorm.validation.model.EntityMetadata;
import io.github.ulisse1996.jaorm.validation.service.AbstractValidator;
import io.github.ulisse1996.jaorm.validation.util.ValidationUtils;

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

    public SourceValidator(List<String> sources,
                           String projectRoot,
                           List<File> jars,
                           ConnectionInfo connectionInfo) throws IOException {
        super(projectRoot, connectionInfo);
        this.sources = sources;
        this.jars = jars;
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
    public void validate() throws EntityValidationException, IOException, SQLException, NoSuchAlgorithmException {
        getLog().info(() -> String.format("Processing Sources in Roots %s", sources));
        for (String source : this.sources) {
            File dir = Paths.get(source).toFile();
            doValidation(dir);
        }
    }

    private void doValidation(File file) throws EntityValidationException, IOException, SQLException, NoSuchAlgorithmException {
        if (isJavaFile(file)) {
            validate(file);
        } else if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (subFiles != null) {
                for (File sub : subFiles) {
                    doValidation(sub);
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
            if (current.equalsIgnoreCase(calculated)) {
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
