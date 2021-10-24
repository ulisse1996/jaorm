package io.github.ulisse1996.jaorm.validation.mojo;

import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import io.github.ulisse1996.jaorm.validation.exception.EntityValidationException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.*;

@ExtendWith(MockitoExtension.class)
class JaormValidationMojoTest {

    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;
    private MavenProject mavenProject;
    private JaormValidationMojo mojo;

    @BeforeEach
    void init() {
        MockDriver.reInit();
        this.mojo = new JaormValidationMojo();
        this.mavenProject = new MavenProject();
        try {
            Field project = JaormValidationMojo.class.getDeclaredField("project");
            Field driver = JaormValidationMojo.class.getDeclaredField("jdbcDriver");
            Field url = JaormValidationMojo.class.getDeclaredField("jdbcUrl");
            project.setAccessible(true);
            driver.setAccessible(true);
            url.setAccessible(true);
            project.set(mojo, mavenProject);
            driver.set(mojo, MockDriver.class.getName());
            url.set(mojo, "url");
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    void should_throw_exception_for_driver_initialization() throws NoSuchFieldException, IllegalAccessException {
        Field field = JaormValidationMojo.class.getDeclaredField("jdbcDriver");
        field.setAccessible(true);
        field.set(mojo, "fake.driver");

        File dir = dirTo("/simple-entity");
        mavenProject.getCompileSourceRoots().add(dir.toPath().toString());

        try {
            mojo.execute();
        } catch (MojoExecutionException e) {
            Assertions.assertTrue(e.getCause() instanceof SQLException);
            return;
        }
        Assertions.fail("Should throw Exception");
    }

    @Test
    void should_validate_simple_entity() throws SQLException {
        File dir = dirTo("/simple-entity");
        mavenProject.getCompileSourceRoots().add(dir.toPath().toString());

        ResultSetMetaData metaData = getMetadataFrom(
                Metadata.of(JDBCType.VARCHAR, "COL1"),
                Metadata.of(JDBCType.VARCHAR, "COL2")
        );

        Mockito.when(MockDriver.getConnection().prepareStatement(Mockito.anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.getMetaData())
                .thenReturn(metaData);

        Assertions.assertDoesNotThrow(mojo::execute);
    }

    @Test
    void should_not_find_column() throws SQLException {
        File dir = dirTo("/simple-entity");
        mavenProject.getCompileSourceRoots().add(dir.toPath().toString());

        Mockito.when(MockDriver.getConnection().prepareStatement(Mockito.anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.getMetaData())
                .thenReturn(Mockito.mock(ResultSetMetaData.class));

        try {
            mojo.execute();
        } catch (MojoExecutionException e) {
            Assertions.assertTrue(e.getCause() instanceof EntityValidationException);
            EntityValidationException ex = (EntityValidationException) e.getCause();
            Assertions.assertEquals(
                    "Column COL1 not found in Entity SimpleEntity", ex.getMessage()
            );
        }
    }

    @Test
    void should_not_have_matched_column() throws SQLException {
        File dir = dirTo("/simple-entity");
        mavenProject.getCompileSourceRoots().add(dir.toPath().toString());

        ResultSetMetaData metaData = getMetadataFrom(Metadata.of(JDBCType.BIGINT, "COL1"));

        Mockito.when(MockDriver.getConnection().prepareStatement(Mockito.anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.getMetaData())
                .thenReturn(metaData);

        try {
            mojo.execute();
        } catch (MojoExecutionException e) {
            Assertions.assertTrue(e.getCause() instanceof EntityValidationException);
            EntityValidationException ex = (EntityValidationException) e.getCause();
            Assertions.assertEquals(
                    "Field col1 in Entity SimpleEntity mismatch type! Found [java.lang.String], required one of [java.lang.Long, long]", ex.getMessage()
            );
        }
    }

    @Test
    void should_validate_entity_with_artifacts() throws SQLException {
        Artifact artifact = Mockito.mock(Artifact.class);
        File file = Mockito.mock(File.class);

        Mockito.when(artifact.getFile())
                .thenReturn(file);

        File dir = dirTo("/complex-entity");
        mavenProject.getCompileSourceRoots().add(dir.toPath().toString());
        mavenProject.getArtifacts().add(artifact);

        ResultSetMetaData metaData = getMetadataFrom(
                Metadata.of(JDBCType.BOOLEAN, "COL1"),
                Metadata.of(JDBCType.VARCHAR, "COL2"),
                Metadata.of(JDBCType.SMALLINT, "COL3"),
                Metadata.of(JDBCType.INTEGER, "COL4"),
                Metadata.of(JDBCType.BIGINT, "COL5"),
                Metadata.of(JDBCType.FLOAT, "COL6"),
                Metadata.of(JDBCType.DOUBLE, "COL7"),
                Metadata.of(JDBCType.NUMERIC, "COL8"),
                Metadata.of(JDBCType.CHAR, "COL9"),
                Metadata.of(JDBCType.DATE, "COL10"),
                Metadata.of(JDBCType.TIME, "COL11"),
                Metadata.of(JDBCType.BLOB, "COL12"),
                Metadata.of(JDBCType.DECIMAL, "COL13"),
                Metadata.of(JDBCType.TIMESTAMP, "COL14")
        );

        Mockito.when(MockDriver.getConnection().prepareStatement(Mockito.anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.getMetaData())
                .thenReturn(metaData);

        Answer<?> answer = invocation -> {
            String name = invocation.getMethod().getName();
            switch (name) {
                case "setParent":
                    return null;
                case "tryToSolveType":
                    return SymbolReference.unsolved(ResolvedReferenceTypeDeclaration.class);
            }
            return null;
        };

        try (MockedConstruction<JarTypeSolver> ignored = Mockito.mockConstructionWithAnswer(JarTypeSolver.class, answer)) {
            Assertions.assertDoesNotThrow(mojo::execute);
        }
    }

    @Test
    void should_validate_entity_with_converter() throws SQLException {
        File dir = dirTo("/entity-with-converter");
        mavenProject.getCompileSourceRoots().add(dir.toPath().toString());

        ResultSetMetaData metaData = getMetadataFrom(
                Metadata.of(JDBCType.INTEGER, "COL1"),
                Metadata.of(JDBCType.VARCHAR, "COL2")
        );

        Mockito.when(MockDriver.getConnection().prepareStatement(Mockito.anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.getMetaData())
                .thenReturn(metaData);

        Assertions.assertDoesNotThrow(mojo::execute);
    }

    @Test
    void should_validate_complex_entity() throws SQLException {
        File dir = dirTo("/complex-entity");
        mavenProject.getCompileSourceRoots().add(dir.toPath().toString());

        ResultSetMetaData metaData = getMetadataFrom(
                Metadata.of(JDBCType.BOOLEAN, "COL1"),
                Metadata.of(JDBCType.VARCHAR, "COL2"),
                Metadata.of(JDBCType.SMALLINT, "COL3"),
                Metadata.of(JDBCType.INTEGER, "COL4"),
                Metadata.of(JDBCType.BIGINT, "COL5"),
                Metadata.of(JDBCType.FLOAT, "COL6"),
                Metadata.of(JDBCType.DOUBLE, "COL7"),
                Metadata.of(JDBCType.NUMERIC, "COL8"),
                Metadata.of(JDBCType.CHAR, "COL9"),
                Metadata.of(JDBCType.DATE, "COL10"),
                Metadata.of(JDBCType.TIME, "COL11"),
                Metadata.of(JDBCType.BLOB, "COL12"),
                Metadata.of(JDBCType.DECIMAL, "COL13"),
                Metadata.of(JDBCType.TIMESTAMP, "COL14")
        );

        Mockito.when(MockDriver.getConnection().prepareStatement(Mockito.anyString()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.getMetaData())
                .thenReturn(metaData);

        Assertions.assertDoesNotThrow(mojo::execute);
    }

    private ResultSetMetaData getMetadataFrom(Metadata... columns) throws SQLException {
        ResultSetMetaData metaData = Mockito.mock(ResultSetMetaData.class);
        Mockito.when(metaData.getColumnCount())
                .thenReturn(columns.length);
        Mockito.when(metaData.getColumnName(Mockito.anyInt()))
                .then(i -> {
                    int index = ((int)i.getArgument(0)) - 1;
                    return columns[index].name;
                });
        Mockito.when(metaData.getColumnType(Mockito.anyInt()))
                .then(i -> {
                    int index = ((int)i.getArgument(0)) - 1;
                    return columns[index].type.getVendorTypeNumber();
                });
        return metaData;
    }

    private File dirTo(String location) {
        URL url = JaormValidationMojoTest.class.getResource(location);
        if (url == null) {
            Assertions.fail(String.format("%s is not a valid location !", location));
        }
        try {
            return new File(url.toURI());
        } catch (Exception ex) {
            Assertions.fail(ex);
            throw new IllegalArgumentException();
        }
    }

    private static class Metadata {
        private final JDBCType type;
        private final String name;

        private Metadata(JDBCType type, String name) {
            this.type = type;
            this.name = name;
        }

        public static Metadata of(JDBCType type, String name) {
            return new Metadata(type, name);
        }
    }
}
