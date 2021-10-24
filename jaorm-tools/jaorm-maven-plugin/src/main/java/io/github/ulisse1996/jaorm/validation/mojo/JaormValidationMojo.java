package io.github.ulisse1996.jaorm.validation.mojo;

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
import io.github.ulisse1996.jaorm.validation.model.EntityMetadata;
import io.github.ulisse1996.jaorm.validation.model.TableMetadata;
import io.github.ulisse1996.jaorm.validation.util.ValidationUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Mojo(name = "jaorm-validation", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE)
public class JaormValidationMojo extends AbstractMojo {

    @Parameter(required = true, readonly = true)
    private String jdbcDriver;

    @Parameter(required = true, readonly = true)
    private String jdbcUsername;

    @Parameter(required = true, readonly = true)
    private String jdbcPassword;

    @Parameter(required = true, readonly = true)
    private String jdbcUrl;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    private Driver driver;
    private TypeSolver standardConfig;

    @Override
    public void execute() throws MojoExecutionException {
        List<String> compileSourceRoots = project.getCompileSourceRoots();
        getLog().info(String.format("Processing Sources in Roots [%s]", compileSourceRoots));

        try {
            this.standardConfig = buildConfig(compileSourceRoots);
            for (String sources : compileSourceRoots) {
                File dir = Paths.get(sources).toFile();
                doValidation(dir);
            }
        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }
    }

    private void doValidation(File file) throws IOException, SQLException, EntityValidationException {
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

    private boolean isJavaFile(File file) {
        return file.getName().endsWith(".java");
    }

    private void validate(File file) throws IOException, SQLException, EntityValidationException {
        ParserConfiguration config = new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(this.standardConfig));
        JavaParser parser = new JavaParser(config);
        CompilationUnit compilationUnit = parser.parse(file).getResult()
                .orElseThrow(() -> new EntityValidationException("Can't parse class"));
        List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class)
                .stream()
                .filter(t -> t.isAnnotationPresent(Table.class.getSimpleName()))
                .filter(t -> !t.isInterface())
                .collect(Collectors.toList());
        for (ClassOrInterfaceDeclaration klass : classes) {
            String klassName = klass.getNameAsString();
            String table = Optional.ofNullable(ValidationUtils.getExpression(klass, Table.class, "name"))
                    .map(Expression::asStringLiteralExpr)
                    .map(StringLiteralExpr::asString)
                    .orElse(null);
            Objects.requireNonNull(table, "Table can't be null !");
            getLog().info("Checking Table " + table);
            try (Connection connection = getConnection();
                 PreparedStatement pr = connection.prepareStatement(String.format("SELECT * FROM %s WHERE 1 = 0", table));
                 ResultSet rs = pr.executeQuery()) {
                TableMetadata metadata = new TableMetadata(rs.getMetaData());
                EntityMetadata entityMetadata = new EntityMetadata(klass);
                for (EntityMetadata.FieldMetadata fieldMetadata : entityMetadata.getFields()) {
                    getLog().info(String.format("Checking Field %s of Entity %s", fieldMetadata.getName(), klassName));
                    Optional<TableMetadata.ColumnMetadata> columnOpt = metadata.findColumn(fieldMetadata.getColumnName());
                    if (!columnOpt.isPresent()) {
                        throw new EntityValidationException(
                                String.format("Column %s not found in Entity %s", fieldMetadata.getColumnName(), klassName)
                        );
                    }
                    TableMetadata.ColumnMetadata column = columnOpt.get();
                    if (!column.matchType(
                            fieldMetadata.getConverterType() != null ? fieldMetadata.getConverterType() : fieldMetadata.getType()
                    )) {
                        throw new EntityValidationException(
                                String.format("Field %s in Entity %s mismatch type! Found [%s], required one of %s",
                                        fieldMetadata.getName(), klassName, fieldMetadata.getType(), column.getSupportedTypesName())
                        );
                    }
                }
            }
        }
    }

    private TypeSolver buildConfig(List<String> compileSourceRoots) throws IOException {
        List<TypeSolver> solvers = new ArrayList<>(Collections.singletonList(new ReflectionTypeSolver()));
        Set<Artifact> artifacts = this.project.getArtifacts();
        for (Artifact artifact : artifacts) {
            solvers.add(new JarTypeSolver(artifact.getFile()));
        }
        for (String source : compileSourceRoots) {
            solvers.add(new JavaParserTypeSolver(source));
        }
        return new CombinedTypeSolver(solvers);
    }

    private synchronized Connection getConnection() throws SQLException {
        if (this.driver == null) {
            try {
                this.driver = (Driver) Class.forName(this.jdbcDriver).newInstance();
                DriverManager.registerDriver(this.driver);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
                throw new SQLException(e);
            }
        }

        return DriverManager.getConnection(this.jdbcUrl, this.jdbcUsername, this.jdbcPassword);
    }
}
