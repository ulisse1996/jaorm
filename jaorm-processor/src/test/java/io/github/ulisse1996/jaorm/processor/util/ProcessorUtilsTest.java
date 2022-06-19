package io.github.ulisse1996.jaorm.processor.util;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.github.ulisse1996.jaorm.annotation.*;
import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;
import io.github.ulisse1996.jaorm.external.LombokMock;
import io.github.ulisse1996.jaorm.external.LombokSupport;
import io.github.ulisse1996.jaorm.processor.CustomName;
import io.github.ulisse1996.jaorm.processor.config.ConfigHolder;
import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class ProcessorUtilsTest {

    @Mock private ProcessingEnvironment environment;
    @Mock private TypeElement element;
    @Mock private Elements elements;
    @Mock private Types types;
    @Mock private Filer filer;
    @Mock private TypeMirror tMirror;
    @Mock private VariableElement variableElement;

    @BeforeEach
    void initConfig() {
        ConfigHolder.init(Collections.emptyMap());
    }

    @AfterEach
    void destroyConfig() {
        ConfigHolder.destroy();
    }

    @Test
    void should_return_constructors_list() {
        ExecutableElement constructor = Mockito.mock(ExecutableElement.class);
        Element notConstructor = Mockito.mock(Element.class);
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getAllMembers(element))
                .then(invocationOnMock -> Arrays.asList(constructor, notConstructor));
        Mockito.when(constructor.getKind())
                .thenReturn(ElementKind.CONSTRUCTOR);
        Mockito.when(notConstructor.getKind())
                .thenReturn(ElementKind.METHOD);
        Assertions.assertEquals(Collections.singletonList(constructor),
                ProcessorUtils.getConstructors(environment, element));
    }

    @Test
    void should_return_annotated() {
        Table table = Mockito.mock(Table.class);
        Element annotated = Mockito.mock(Element.class);
        Element notAnnotated = Mockito.mock(Element.class);
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(environment.getTypeUtils())
                .thenReturn(types);
        Mockito.when(types.directSupertypes(Mockito.any()))
                .thenReturn(Collections.emptyList());
        Mockito.when(elements.getAllMembers(element))
                .then(invocationOnMock -> Arrays.asList(annotated, notAnnotated));
        Mockito.when(annotated.getSimpleName())
                .thenReturn(nameOf("annotated"));
        Mockito.when(notAnnotated.getSimpleName())
                .thenReturn(nameOf("notAnnotated"));
        Mockito.when(annotated.getAnnotation(Table.class))
                .thenReturn(table);
        Assertions.assertEquals(Collections.singletonList(annotated),
                ProcessorUtils.getAnnotated(environment, element, Table.class));
    }

    @Test
    void should_return_opt_getter() {
        Name name = Mockito.mock(Name.class);
        ExecutableElement getter = mockFindGetter(name, false);
        Optional<ExecutableElement> getterOpt = ProcessorUtils.findGetterOpt(environment, element, name);
        Assertions.assertTrue(getterOpt.isPresent());
        Assertions.assertEquals(getter, getterOpt.get());
    }

    @Test
    void should_return_opt_getter_for_boolean() {
        Name name = Mockito.mock(Name.class);
        ExecutableElement getter = mockFindGetter(name, true);
        Optional<ExecutableElement> getterOpt = ProcessorUtils.findGetterOpt(environment, element, name);
        Assertions.assertTrue(getterOpt.isPresent());
        Assertions.assertEquals(getter, getterOpt.get());
    }

    @Test
    void should_return_getter() {
        Name name = Mockito.mock(Name.class);
        ExecutableElement getter = mockFindGetter(name, false);
        ExecutableElement found = ProcessorUtils.findGetter(environment, element, name);
        Assertions.assertEquals(getter, found);
    }

    @Test
    void should_not_found_getter() {
        Name name = Mockito.mock(Name.class);
        Mockito.when(name.toString())
                .thenReturn("name");
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(environment.getTypeUtils())
                .thenReturn(types);
        Mockito.when(types.directSupertypes(Mockito.any()))
                .thenReturn(Collections.emptyList());
        Mockito.when(elements.getAllMembers(element))
                .then(invocation -> Collections.emptyList());
        Assertions.assertThrows(ProcessorException.class,
                () -> ProcessorUtils.findGetter(environment, element, name));
    }

    @Test
    void should_return_setter_opt() {
        Name name = Mockito.mock(Name.class);
        ExecutableElement setter = mockFindSetter(name);
        Optional<ExecutableElement> found = ProcessorUtils.findSetterOpt(environment, element, name);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(setter, found.get());
    }

    @Test
    void should_return_setter() {
        Name name = Mockito.mock(Name.class);
        ExecutableElement setter = mockFindSetter(name);
        ExecutableElement found = ProcessorUtils.findSetter(environment, element, name);
        Assertions.assertEquals(setter, found);
    }

    @Test
    void should_not_find_setter() {
        Name name = Mockito.mock(Name.class);
        Mockito.when(name.toString())
                .thenReturn("name");
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(environment.getTypeUtils())
                .thenReturn(types);
        Mockito.when(types.directSupertypes(Mockito.any()))
                .thenReturn(Collections.emptyList());
        Mockito.when(elements.getAllMembers(element))
                .then(invocation -> Collections.emptyList());
        Assertions.assertThrows(ProcessorException.class, () ->
                ProcessorUtils.findSetter(environment, element, name));
    }

    @Test
    void should_return_field_type() {
        TypeMirror mirror = Mockito.mock(TypeMirror.class);
        TypeElement element = Mockito.mock(TypeElement.class);
        Mockito.when(variableElement.asType())
                .thenReturn(mirror);
        Mockito.when(mirror.toString())
                .thenReturn("MyEntity");
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getTypeElement("MyEntity"))
                .thenReturn(element);
        Assertions.assertEquals(element, ProcessorUtils.getFieldType(environment, variableElement));
    }

    @Test
    void should_return_field_type_with_generic() {
        TypeMirror mirror = Mockito.mock(TypeMirror.class);
        TypeElement element = Mockito.mock(TypeElement.class);
        Mockito.when(variableElement.asType())
                .thenReturn(mirror);
        Mockito.when(mirror.toString())
                .thenReturn("java.util.List<MyEntity>");
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getTypeElement("MyEntity"))
                .thenReturn(element);
        Assertions.assertEquals(element, ProcessorUtils.getFieldType(environment, variableElement));
    }

    @Test
    void should_return_null_for_a_class_that_is_not_a_wrapper() {
        Mockito.when(element.getQualifiedName())
                .thenReturn(nameOf("io.test.MyEntity"));
        Assertions.assertNull(ProcessorUtils.getUnboxed(environment, element));
    }

    @Test
    void should_return_unboxed_type() {
        TypeMirror mirror = Mockito.mock(TypeMirror.class);
        PrimitiveType primitiveType = Mockito.mock(PrimitiveType.class);
        Mockito.when(element.getQualifiedName())
                .thenReturn(nameOf("java.lang.Boolean"));
        Mockito.when(element.asType())
                .thenReturn(mirror);
        Mockito.when(environment.getTypeUtils())
                .thenReturn(types);
        Mockito.when(types.unboxedType(mirror))
                .thenReturn(primitiveType);
        Assertions.assertEquals(primitiveType, ProcessorUtils.getUnboxed(environment, element));
    }

    @Test
    void should_return_generic_types_of_value_converter() {
        TypeMirror mirror = Mockito.mock(TypeMirror.class);
        TypeMirror converterMirror = Mockito.mock(TypeMirror.class);
        TypeElement elementType1 = Mockito.mock(TypeElement.class);
        TypeElement elementType2 = Mockito.mock(TypeElement.class);
        String valueConverterName = "io.github.ulisse1996.jaorm.entity.converter.ValueConverter<Type1, Type2>";
        String type1 = "Type1";
        String type2 = "Type2";
        Mockito.when(environment.getTypeUtils())
                .thenReturn(types);
        Mockito.when(types.asElement(mirror))
                .thenReturn(element);
        Mockito.when(element.getInterfaces())
                .then(invocation -> Collections.singletonList(converterMirror));
        Mockito.when(converterMirror.toString())
                .thenReturn(valueConverterName);
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getTypeElement(type1))
                .thenReturn(elementType1);
        Mockito.when(elements.getTypeElement(type2))
                .thenReturn(elementType2);
        Assertions.assertEquals(Arrays.asList(elementType1, elementType2),
                ProcessorUtils.getGenericTypes(environment, mirror, "io.github.ulisse1996.jaorm.entity.converter.ValueConverter"));
    }

    @Test
    void should_generate_file() throws IOException {
        JavaFile.Builder builder = Mockito.mock(JavaFile.Builder.class);
        JavaFile javaFile = Mockito.mock(JavaFile.class);
        GeneratedFile file = new GeneratedFile("io.test", Mockito.mock(TypeSpec.class), "Entity");
        try (MockedStatic<JavaFile> mk = Mockito.mockStatic(JavaFile.class)) {
            mk.when(() -> JavaFile.builder("io.test", file.getSpec()))
                    .thenReturn(builder);
            Mockito.when(builder.skipJavaLangImports(true))
                    .thenReturn(builder);
            Mockito.when(builder.indent(Mockito.anyString()))
                    .thenReturn(builder);
            Mockito.when(builder.addFileComment(Mockito.anyString()))
                    .thenReturn(builder);
            Mockito.when(builder.build())
                    .thenReturn(javaFile);
            Mockito.when(environment.getFiler())
                    .thenReturn(filer);
            ProcessorUtils.generate(environment, file);
            Mockito.verify(javaFile, Mockito.times(1)).writeTo(filer);
        }
    }

    @Test
    void should_throw_exception_during_generate_file() throws IOException {
        JavaFile.Builder builder = Mockito.mock(JavaFile.Builder.class);
        JavaFile javaFile = Mockito.mock(JavaFile.class);
        GeneratedFile file = new GeneratedFile("io.test", Mockito.mock(TypeSpec.class), "Entity");
        try (MockedStatic<JavaFile> mk = Mockito.mockStatic(JavaFile.class)) {
            mk.when(() -> JavaFile.builder("io.test", file.getSpec()))
                    .thenReturn(builder);
            Mockito.when(builder.skipJavaLangImports(true))
                    .thenReturn(builder);
            Mockito.when(builder.indent(Mockito.anyString()))
                    .thenReturn(builder);
            Mockito.when(builder.addFileComment(Mockito.anyString()))
                    .thenReturn(builder);
            Mockito.when(builder.build())
                    .thenReturn(javaFile);
            Mockito.when(environment.getFiler())
                    .thenReturn(filer);
            Mockito.doThrow(IOException.class)
                    .when(javaFile).writeTo(filer);
            Assertions.assertThrows(ProcessorException.class, () -> ProcessorUtils.generate(environment, file));
        }
    }

    @Test
    void should_find_opt_field_with_column_name() {
        Column annotation = Mockito.mock(Column.class);
        VariableElement column = Mockito.mock(VariableElement.class);
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getAllMembers(element))
                .then(invocation -> Collections.singletonList(column));
        Mockito.when(column.getAnnotation(Column.class))
                .thenReturn(annotation);
        Mockito.when(annotation.name())
                .thenReturn("NAME");
        Optional<VariableElement> found = ProcessorUtils.getFieldWithColumnNameOpt(environment, element, "NAME");
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(column, found.get());
    }

    @Test
    void should_not_find_field_with_column_name() {
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getAllMembers(element))
                .then(invocation -> Collections.emptyList());
        Optional<VariableElement> found = ProcessorUtils.getFieldWithColumnNameOpt(environment, element, "NAME");
        Assertions.assertFalse(found.isPresent());
    }

    @Test
    void should_throw_exception_for_missing_column() {
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getAllMembers(element))
                .then(invocation -> Collections.emptyList());
        Assertions.assertThrows(ProcessorException.class,
                () -> ProcessorUtils.getFieldWithColumnName(environment, element, "NAME"));
    }

    @Test
    void should_find_field_with_column_name() {
        Column annotation = Mockito.mock(Column.class);
        VariableElement column = Mockito.mock(VariableElement.class);
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getAllMembers(element))
                .then(invocation -> Collections.singletonList(column));
        Mockito.when(column.getAnnotation(Column.class))
                .thenReturn(annotation);
        Mockito.when(annotation.name())
                .thenReturn("NAME");
        VariableElement found = ProcessorUtils.getFieldWithColumnName(environment, element, "NAME");
        Assertions.assertEquals(column, found);
    }

    @Test
    void should_get_singleton_converter() {
        TypeMirror mirror = Mockito.mock(TypeMirror.class);
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);

        // Instance field Mock
        TypeMirror instanceMirror = Mockito.mock(TypeMirror.class);
        VariableElement instanceField = Mockito.mock(VariableElement.class);
        Mockito.when(instanceField.getKind())
                .thenReturn(ElementKind.FIELD);
        Mockito.when(instanceField.asType())
                .thenReturn(instanceMirror);
        Mockito.when(instanceMirror.toString())
                .thenReturn("io.test.MyConverter");
        Mockito.when(instanceField.getSimpleName())
                .thenReturn(nameOf("INSTANCE"));

        // Converter Mock
        mockConverterClass(mirror, true);
        Mockito.when(environment.getTypeUtils())
                .thenReturn(types);
        Mockito.when(types.asElement(mirror))
                .thenReturn(element);
        Mockito.when(elements.getAllMembers(element))
                .then(invocation -> Collections.singletonList(instanceField));
        Mockito.when(mirror.toString())
                .thenReturn("io.test.MyConverter");
        String expected = "io.test.MyConverter.INSTANCE";
        String found = ProcessorUtils.getConverterCaller(environment, variableElement);
        Assertions.assertEquals(expected, found);
    }

    @Test
    void should_get_new_converter_instance() {
        TypeMirror mirror = Mockito.mock(TypeMirror.class);
        mockConverterClass(mirror, false);
        Mockito.when(environment.getTypeUtils())
                .thenReturn(types);
        Mockito.when(types.asElement(mirror))
                .thenReturn(element);
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getAllMembers(element))
                .then(invocation -> Collections.emptyList());
        Mockito.when(mirror.toString())
                .thenReturn(MyConverter.class.getName());
        String expected = "new "+MyConverter.class.getName()+"()";
        String found = ProcessorUtils.getConverterCaller(environment, variableElement);
        Assertions.assertEquals(expected, found);
    }

    @Test
    void should_return_method() {
        ExecutableElement executableElement = Mockito.mock(ExecutableElement.class);
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getTypeElement(Mockito.anyString()))
                .thenReturn(element);
        Mockito.when(elements.getAllMembers(element))
                .then(invocation -> Collections.singletonList(executableElement));
        Mockito.when(executableElement.getSimpleName())
                .thenReturn(nameOf("method"));
        ExecutableElement result = ProcessorUtils.getMethod(environment, "method", Object.class);
        Assertions.assertEquals(executableElement, result);
    }

    @Test
    void should_throw_exception_for_missing_method() {
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getTypeElement(Mockito.anyString()))
                .thenReturn(element);
        Mockito.when(elements.getAllMembers(element))
                .then(invocation -> Collections.emptyList());
        Assertions.assertThrows(ProcessorException.class,
                () -> ProcessorUtils.getMethod(environment, "method", Object.class));
    }

    @Test
    void should_return_all_dao() {
        ExecutableElement daoQuery = Mockito.mock(ExecutableElement.class);
        TypeElement dao = Mockito.mock(TypeElement.class);
        RoundEnvironment roundEnvironment = Mockito.mock(RoundEnvironment.class);
        Mockito.when(daoQuery.getEnclosingElement())
                .thenReturn(dao);
        Mockito.when(roundEnvironment.getElementsAnnotatedWith(Dao.class))
                .then(invocation -> Collections.singleton(dao));
        Mockito.when(roundEnvironment.getElementsAnnotatedWith(Query.class))
                .then(invocation -> Collections.singleton(daoQuery));
        List<TypeElement> result = ProcessorUtils.getAllDao(roundEnvironment);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(dao, result.get(0));
    }

    @Test
    void should_return_false_for_base_dao() {
        TypeMirror mirror = Mockito.mock(TypeMirror.class);
        Mockito.when(element.getInterfaces())
                .then(invocation -> Collections.singletonList(mirror))
                .thenReturn(Collections.emptyList());
        Mockito.when(mirror.toString())
                .thenReturn("notADao");
        Mockito.when(environment.getTypeUtils()).thenReturn(types);
        Mockito.when(types.asElement(Mockito.any()))
                .thenReturn(element);
        Assertions.assertFalse(ProcessorUtils.isBaseDao(environment, element));
    }

    @Test
    void should_return_true_for_base_dao() {
        TypeMirror mirror = Mockito.mock(TypeMirror.class);
        Mockito.when(element.getInterfaces())
                .then(invocation -> Collections.singletonList(mirror));
        Mockito.when(mirror.toString())
                .thenReturn("io.github.ulisse1996.jaorm.BaseDao<Entity>");
        Assertions.assertTrue(ProcessorUtils.isBaseDao(environment, element));
    }

    @Test
    void should_return_entity_from_dao() {
        TypeMirror mirror = Mockito.mock(TypeMirror.class);
        TypeMirror emptyMirror = Mockito.mock(TypeMirror.class);
        Mockito.when(emptyMirror.toString())
                .thenReturn("empty");
        Mockito.when(element.asType()).thenReturn(tMirror);
        Mockito.when(environment.getTypeUtils()).thenReturn(types);
        Mockito.when(types.directSupertypes(Mockito.any()))
                .then(invocation -> Arrays.asList(emptyMirror, mirror))
                .thenReturn(Collections.emptyList());
        Mockito.when(mirror.toString())
                .thenReturn("io.github.ulisse1996.jaorm.BaseDao<Entity>");
        String name = "Entity";
        Assertions.assertEquals(name, ProcessorUtils.getBaseDaoGeneric(environment, element));
    }

    @Test
    void should_return_all_valid_elements() {
        List<Element> objectElements = getObjectElements();
        List<Element> allElements = new ArrayList<>(objectElements);
        TypeElement objectElement = Mockito.mock(TypeElement.class);
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(environment.getTypeUtils())
                .thenReturn(types);
        Mockito.when(types.directSupertypes(Mockito.any()))
                .thenReturn(Collections.emptyList());
        Mockito.when(elements.getAllMembers(element))
                .then(invocation -> allElements);
        Mockito.when(elements.getTypeElement("java.lang.Object"))
                .then(invocation -> objectElement);
        Mockito.when(elements.getAllMembers(objectElement))
                .then(invocation -> objectElements);
        Assertions.assertEquals(Collections.singletonList(objectElements.get(3)), ProcessorUtils.getAllValidElements(environment, element));
    }

    @Test
    void should_return_list_of_generics_types() {
        TypeMirror mirror = Mockito.mock(TypeMirror.class);
        TypeMirror converter = Mockito.mock(TypeMirror.class);
        TypeElement stringElement = Mockito.mock(TypeElement.class);
        TypeElement integerElement = Mockito.mock(TypeElement.class);
        mockConverterClass(mirror, false);
        Mockito.when(environment.getTypeUtils())
                .thenReturn(types);
        Mockito.when(types.asElement(mirror))
                .thenReturn(element);
        Mockito.when(element.getInterfaces())
                .then(invocation -> Collections.singletonList(converter));
        Mockito.when(converter.toString())
                .thenReturn("io.github.ulisse1996.jaorm.entity.converter.ValueConverter<String,Integer>");
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getTypeElement("String"))
                .thenReturn(stringElement);
        Mockito.when(elements.getTypeElement("Integer"))
                .thenReturn(integerElement);
        List<TypeElement> expected = Arrays.asList(stringElement, integerElement);
        Assertions.assertEquals(expected, ProcessorUtils.getConverterTypes(environment, variableElement));
    }

    private List<Element> getObjectElements() {
        Element finalElement = Mockito.mock(Element.class);
        Element nativeElement = Mockito.mock(Element.class);
        Element protectedElement = Mockito.mock(Element.class);
        Element privateElement = Mockito.mock(Element.class);
        Mockito.when(finalElement.getModifiers())
                .thenReturn(Collections.singleton(Modifier.FINAL));
        Mockito.when(finalElement.getSimpleName())
                .thenReturn(mockName("finalName"));
        Mockito.when(nativeElement.getModifiers())
                .thenReturn(Collections.singleton(Modifier.NATIVE));
        Mockito.when(nativeElement.getSimpleName())
                .thenReturn(mockName("nativeName"));
        Mockito.when(protectedElement.getModifiers())
                .thenReturn(Collections.singleton(Modifier.PROTECTED));
        Mockito.when(protectedElement.getSimpleName())
                .thenReturn(mockName("protectedName"));
        Mockito.when(privateElement.getModifiers())
                .thenReturn(Collections.singleton(Modifier.PRIVATE));
        Mockito.when(privateElement.getSimpleName())
                .thenReturn(mockName("privateName"));
        Mockito.when(privateElement.getKind())
                .thenReturn(ElementKind.FIELD);
        return Arrays.asList(finalElement, nativeElement, protectedElement, privateElement);
    }

    private Name mockName(String finalName) {
        return new CustomName(finalName);
    }

    @Test
    void should_throw_exception_for_missing_dao_generic() {
        Mockito.when(element.asType()).thenReturn(tMirror);
        Mockito.when(environment.getTypeUtils()).thenReturn(types);
        Mockito.when(types.directSupertypes(Mockito.any()))
                .then(invocation -> Collections.emptyList());
        Assertions.assertThrows(ProcessorException.class, () -> ProcessorUtils.getBaseDaoGeneric(environment, element));
    }

    @Test
    void should_return_empty_optional_for_missing_external_getter() {
        try (MockedStatic<LombokSupport> mk = Mockito.mockStatic(LombokSupport.class)) {
            Mockito.when(environment.getElementUtils())
                    .thenReturn(elements);
            Mockito.when(environment.getTypeUtils())
                    .thenReturn(types);
            Mockito.when(types.directSupertypes(Mockito.any()))
                    .thenReturn(Collections.emptyList());
            Mockito.when(elements.getAllMembers(Mockito.any()))
                    .thenReturn(Collections.emptyList());
            LombokSupport instance = Mockito.spy(new LombokSupport() {
                @Override
                public boolean isSupported() {
                    return false;
                }

                @Override
                public boolean isLombokGenerated(Element element) {
                    return false;
                }

                @Override
                public boolean hasLombokNoArgs(TypeElement entity) {
                    return false;
                }

                @Override
                public Element generateFakeElement(Element element, GenerationType generationType) {
                    return null;
                }
            });
            mk.when(LombokSupport::getInstance)
                    .thenReturn(instance);
            Assertions.assertFalse(ProcessorUtils.findGetterOpt(environment, element, nameOf("test")).isPresent());
            Mockito.verify(instance).isSupported();
        }
    }

    @Test
    void should_return_external_getter() {
        try (MockedStatic<LombokSupport> mk = Mockito.mockStatic(LombokSupport.class)) {
            Mockito.when(environment.getElementUtils())
                    .thenReturn(elements);
            Mockito.when(environment.getTypeUtils())
                    .thenReturn(types);
            Mockito.when(types.directSupertypes(Mockito.any()))
                    .thenReturn(Collections.emptyList());
            Mockito.when(elements.getAllMembers(Mockito.any()))
                    .then(invocation -> Collections.singletonList(variableElement));
            Mockito.when(variableElement.getSimpleName())
                    .thenReturn(nameOf("test"));
            LombokSupport instance = Mockito.spy(new LombokSupport() {
                @Override
                public boolean isSupported() {
                    return true;
                }

                @Override
                public boolean isLombokGenerated(Element element) {
                    return true;
                }

                @Override
                public boolean hasLombokNoArgs(TypeElement entity) {
                    return false;
                }

                @Override
                public Element generateFakeElement(Element element, GenerationType generationType) {
                    return Mockito.mock(ExecutableElement.class);
                }
            });
            mk.when(LombokSupport::getInstance)
                    .thenReturn(instance);
            Optional<ExecutableElement> opt = ProcessorUtils.findGetterOpt(environment, element, nameOf("test"));
            Assertions.assertTrue(opt.isPresent());
            Assertions.assertTrue(Mockito.mockingDetails(opt.get()).isMock());
            Mockito.verify(instance).isSupported();
            Mockito.verify(instance).isLombokGenerated(Mockito.any());
        }
    }

    @Test
    void should_return_empty_optional_for_not_lombok_generated_field() {
        try (MockedStatic<LombokSupport> mk = Mockito.mockStatic(LombokSupport.class)) {
            Mockito.when(environment.getElementUtils())
                    .thenReturn(elements);
            Mockito.when(environment.getTypeUtils())
                    .thenReturn(types);
            Mockito.when(types.directSupertypes(Mockito.any()))
                    .thenReturn(Collections.emptyList());
            Mockito.when(elements.getAllMembers(Mockito.any()))
                    .then(invocation -> Collections.singletonList(variableElement));
            Mockito.when(variableElement.getSimpleName())
                    .thenReturn(nameOf("test"));
            LombokSupport instance = Mockito.spy(new LombokSupport() {
                @Override
                public boolean isSupported() {
                    return true;
                }

                @Override
                public boolean isLombokGenerated(Element element) {
                    return false;
                }

                @Override
                public boolean hasLombokNoArgs(TypeElement entity) {
                    return false;
                }

                @Override
                public Element generateFakeElement(Element element, GenerationType generationType) {
                    return null;
                }
            });
            mk.when(LombokSupport::getInstance)
                    .thenReturn(instance);
            Assertions.assertFalse(ProcessorUtils.findGetterOpt(environment, element, nameOf("test")).isPresent());
            Mockito.verify(instance).isSupported();
            Mockito.verify(instance).isLombokGenerated(variableElement);
        }
    }

    @Test
    void should_return_empty_list_for_not_supported_lombok() {
        LombokSupport instance = Mockito.mock(LombokSupport.class);
        try (MockedStatic<LombokSupport> mk = Mockito.mockStatic(LombokSupport.class)) {
            mk.when(LombokSupport::getInstance)
                    .thenReturn(instance);
            Mockito.when(instance.isSupported())
                    .thenReturn(false);
            Assertions.assertEquals(Collections.emptyList(),
                    ProcessorUtils.appendExternalGeneratedMethods(environment, element, Collections.emptyList()));
        }
    }

    @Test
    void should_return_list_with_getter_for_lombok() {
        LombokSupport instance = Mockito.mock(LombokSupport.class);
        ExecutableElement getter = fakeAccessor();
        List<Element> fieldList = Collections.singletonList(variableElement);
        Mockito.when(variableElement.getKind())
                .thenReturn(ElementKind.FIELD);
        try (MockedStatic<LombokSupport> mk = Mockito.mockStatic(LombokSupport.class);
             MockedStatic<ProcessorUtils> utilsMk = Mockito.mockStatic(ProcessorUtils.class)) {
            utilsMk.when(() -> ProcessorUtils.appendExternalGeneratedMethods(environment, element, fieldList))
                    .thenCallRealMethod();
            utilsMk.when(() -> ProcessorUtils.findGetterOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Optional.of(getter));
            utilsMk.when(() -> ProcessorUtils.isLombokMock(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(true);
            mk.when(LombokSupport::getInstance)
                    .thenReturn(instance);
            Mockito.when(instance.isSupported())
                    .thenReturn(true);
            Mockito.when(instance.generateFakeElement(variableElement, LombokSupport.GenerationType.GETTER))
                    .thenReturn(getter);
            Assertions.assertEquals(Collections.singletonList(getter),
                    ProcessorUtils.appendExternalGeneratedMethods(environment, element, fieldList));
        }
    }

    @Test
    void should_return_list_with_setter_for_lombok() {
        LombokSupport instance = Mockito.mock(LombokSupport.class);
        ExecutableElement setter = fakeAccessor();
        List<Element> fieldList = Collections.singletonList(variableElement);
        Mockito.when(variableElement.getKind())
                .thenReturn(ElementKind.FIELD);
        try (MockedStatic<LombokSupport> mk = Mockito.mockStatic(LombokSupport.class);
             MockedStatic<ProcessorUtils> utilsMk = Mockito.mockStatic(ProcessorUtils.class)) {
            utilsMk.when(() -> ProcessorUtils.appendExternalGeneratedMethods(environment, element, fieldList))
                    .thenCallRealMethod();
            utilsMk.when(() -> ProcessorUtils.findSetterOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Optional.of(setter));
            utilsMk.when(() -> ProcessorUtils.isLombokMock(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(true);
            mk.when(LombokSupport::getInstance)
                    .thenReturn(instance);
            Mockito.when(instance.isSupported())
                    .thenReturn(true);
            Mockito.when(instance.generateFakeElement(variableElement, LombokSupport.GenerationType.SETTER))
                    .thenReturn(setter);
            Assertions.assertEquals(Collections.singletonList(setter),
                    ProcessorUtils.appendExternalGeneratedMethods(environment, element, fieldList));
        }
    }

    @Test
    void should_return_true_for_lombok_mock_for_getter() {
        Element mock = Mockito.mock(Element.class);
        ExecutableElement getterMock = Mockito.spy(fakeAccessor());
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mk.when(() -> ProcessorUtils.isLombokMock(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenCallRealMethod();
            mk.when(() -> ProcessorUtils.findGetterOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Optional.of(getterMock));
            Assertions.assertTrue(ProcessorUtils.isLombokMock(environment, element, mock));
        }
    }

    @Test
    void should_return_true_for_lombok_mock_for_setter() {
        Element mock = Mockito.mock(Element.class);
        ExecutableElement setterMock = Mockito.spy(fakeAccessor());
        try (MockedStatic<ProcessorUtils> mk = Mockito.mockStatic(ProcessorUtils.class)) {
            mk.when(() -> ProcessorUtils.isLombokMock(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenCallRealMethod();
            mk.when(() -> ProcessorUtils.findSetterOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Optional.of(setterMock));
            Assertions.assertTrue(ProcessorUtils.isLombokMock(environment, element, mock));
        }
    }

    @Test
    void should_return_list_with_getter_and_setter_for_lombok() {
        LombokSupport instance = Mockito.mock(LombokSupport.class);
        ExecutableElement getter = fakeAccessor();
        ExecutableElement setter = fakeAccessor();
        List<Element> fieldList = Collections.singletonList(variableElement);
        Mockito.when(variableElement.getKind())
                .thenReturn(ElementKind.FIELD);
        try (MockedStatic<LombokSupport> mk = Mockito.mockStatic(LombokSupport.class);
            MockedStatic<ProcessorUtils> utilsMk = Mockito.mockStatic(ProcessorUtils.class)) {
            utilsMk.when(() -> ProcessorUtils.appendExternalGeneratedMethods(environment, element, fieldList))
                    .thenCallRealMethod();
            utilsMk.when(() -> ProcessorUtils.findGetterOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Optional.of(getter));
            utilsMk.when(() -> ProcessorUtils.findSetterOpt(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(Optional.of(setter));
            utilsMk.when(() -> ProcessorUtils.isLombokMock(Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn(true);
            mk.when(LombokSupport::getInstance)
                    .thenReturn(instance);
            Mockito.when(instance.isSupported())
                    .thenReturn(true);
            Mockito.when(instance.generateFakeElement(variableElement, LombokSupport.GenerationType.GETTER))
                    .thenReturn(getter);
            Mockito.when(instance.generateFakeElement(variableElement, LombokSupport.GenerationType.SETTER))
                    .thenReturn(setter);
            Assertions.assertEquals(Arrays.asList(getter, setter),
                    ProcessorUtils.appendExternalGeneratedMethods(environment, element, fieldList));
        }
    }

    @Test
    void should_return_annotated_field_in_super_class() {
        TypeMirror objectMirror = Mockito.mock(TypeMirror.class);
        TypeMirror interfaceMirror = Mockito.mock(TypeMirror.class);
        TypeMirror superMirror = Mockito.mock(TypeMirror.class);
        TypeElement objectType = Mockito.mock(TypeElement.class);
        TypeElement interfaceType = Mockito.mock(TypeElement.class);
        TypeElement superType = Mockito.mock(TypeElement.class);
        VariableElement variableElement = Mockito.mock(VariableElement.class);
        Mockito.when(objectMirror.toString())
                .thenReturn("java.lang.Object");
        Mockito.when(interfaceMirror.toString())
                .thenReturn("MyInterface<Test>");
        Mockito.when(superMirror.toString())
                .thenReturn("Super");
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getAllMembers(element))
                .thenReturn(Collections.emptyList());
        Mockito.when(environment.getTypeUtils())
                .thenReturn(types);
        Mockito.when(types.directSupertypes(Mockito.any()))
                .then(invocation -> Arrays.asList(objectMirror, interfaceMirror, superMirror))
                .thenReturn(Collections.emptyList());
        Mockito.when(elements.getTypeElement("java.lang.Object"))
                .thenReturn(objectType);
        Mockito.when(elements.getTypeElement("MyInterface"))
                .thenReturn(interfaceType);
        Mockito.when(elements.getTypeElement("Super"))
                .thenReturn(superType);
        Mockito.when(interfaceType.getKind())
                .thenReturn(ElementKind.INTERFACE);
        Mockito.when(elements.getAllMembers(superType))
                .then(invocation -> Collections.singletonList(variableElement));
        Mockito.when(variableElement.getSimpleName())
                .thenReturn(nameOf("variable"));
        Mockito.when(variableElement.getAnnotation(Mockito.any()))
                .then(invocation -> Mockito.mock(invocation.getArgument(0)));
        List<Element> found = ProcessorUtils.getAnnotated(environment, element, Column.class);
        Assertions.assertEquals(Collections.singletonList(variableElement), found);
    }

    @Test
    void should_return_true_for_external_generated_constructor() {
        LombokSupport instance = Mockito.mock(LombokSupport.class);
        Mockito.when(instance.hasLombokNoArgs(Mockito.any()))
                .thenReturn(true);
        try (MockedStatic<LombokSupport> mk = Mockito.mockStatic(LombokSupport.class)) {
            mk.when(LombokSupport::getInstance)
                    .thenReturn(instance);
            Assertions.assertTrue(ProcessorUtils.hasExternalConstructor(Mockito.mock(TypeElement.class)));
        }
    }

    @Test
    void should_return_same_sql_for_standard_query() {
        String mySql = "SELECT ";
        Assertions.assertEquals(mySql, ProcessorUtils.getSqlOrSqlFromFile(mySql, this.environment));
        Mockito.verifyNoInteractions(this.environment);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/test.sql", "test.sql"})
    void should_return_sql_from_file(String sql) throws IOException, URISyntaxException {
        FileObject obj = Mockito.mock(FileObject.class);
        URI uri = Objects.requireNonNull(ProcessorUtilsTest.class.getResource("/fakeSql.sql")).toURI();
        Mockito.when(environment.getFiler())
                .thenReturn(filer);
        Mockito.when(filer.getResource(Mockito.any(), Mockito.anyString(), Mockito.any()))
                .thenReturn(obj);
        Mockito.when(obj.toUri())
                .thenReturn(uri);
        Assertions.assertEquals(
                String.join("", Files.readAllLines(Paths.get(uri))),
                ProcessorUtils.getSqlOrSqlFromFile(sql, environment)
        );
    }

    @Test
    void should_throw_exception_for_sql_read() throws IOException {
        Mockito.when(environment.getFiler())
                .thenReturn(filer);
        Mockito.when(filer.getResource(Mockito.any(), Mockito.anyString(), Mockito.any()))
                .thenThrow(IOException.class);
        Assertions.assertThrows(ProcessorException.class, () -> ProcessorUtils.getSqlOrSqlFromFile(".sql", environment));
    }

    @Test
    void should_throw_exception_for_spi_creation() throws IOException {
        Mockito.when(environment.getFiler())
                .thenReturn(filer);
        Mockito.when(filer.createResource(Mockito.any(), Mockito.anyString(), Mockito.any()))
                .thenThrow(IOException.class);
        Assertions.assertThrows(ProcessorException.class, () -> ProcessorUtils.generateSpi(environment, Collections.singletonList(new GeneratedFile("", null, "")), //NOSONAR
                Object.class));
    }

    private ExecutableElement fakeAccessor() {
        return new FakeAccessor();
    }

    private void mockConverterClass(TypeMirror mirror, boolean withException) {
        Converter converter = Mockito.mock(Converter.class);
        Class<?> klassMock = MyConverter.class;
        Mockito.when(variableElement.getAnnotation(Converter.class))
                .thenReturn(converter);
        if (withException) {
            Mockito.doThrow(new MirroredTypeException(mirror))
                    .when(converter).value();
        } else {
            Mockito.when(converter.value())
                    .then(invocation -> klassMock);
            Mockito.when(elements.getTypeElement(MyConverter.class.getName()))
                    .thenReturn(element);
            Mockito.when(element.asType())
                    .thenReturn(mirror);
        }
    }

    private ExecutableElement mockFindSetter(Name name) {
        ExecutableElement setter = Mockito.mock(ExecutableElement.class);
        ExecutableElement notMySetter = Mockito.mock(ExecutableElement.class);
        Mockito.when(name.toString())
                .thenReturn("name");
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(environment.getTypeUtils())
                .thenReturn(types);
        Mockito.when(types.directSupertypes(Mockito.any()))
                .thenReturn(Collections.emptyList());
        Mockito.when(elements.getAllMembers(element))
                .then(invocation -> Arrays.asList(setter, notMySetter));
        Mockito.when(setter.getSimpleName())
                .thenReturn(nameOf("setName"));
        Mockito.when(setter.getKind())
                .thenReturn(ElementKind.METHOD);
        Mockito.when(notMySetter.getSimpleName())
                .thenReturn(nameOf("notMySetter"));
        return setter;
    }

    private ExecutableElement mockFindGetter(Name name, boolean booleanGetter) {
        ExecutableElement getter = Mockito.mock(ExecutableElement.class);
        ExecutableElement notMyGetter = Mockito.mock(ExecutableElement.class);
        Mockito.when(name.toString())
                .thenReturn("name");
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(environment.getTypeUtils())
                .thenReturn(types);
        Mockito.when(types.directSupertypes(Mockito.any()))
                .thenReturn(Collections.emptyList());
        Mockito.when(elements.getAllMembers(element))
                .then(invocation -> Arrays.asList(getter, notMyGetter));
        Mockito.when(getter.getSimpleName())
                .thenReturn(nameOf(booleanGetter ? "isName" : "getName"));
        Mockito.when(getter.getKind())
                .thenReturn(ElementKind.METHOD);
        Mockito.when(notMyGetter.getSimpleName())
                .thenReturn(nameOf("notMyGetter"));
        return getter;
    }

    @Test
    void should_throw_exception_during_spi_creation_with_nio() throws IOException {
        GeneratedFile file = new GeneratedFile(
                "package",
                TypeSpec.classBuilder("TEST").build(),
                null
        );
        Path servicePath = Mockito.mock(Path.class);
        Path path = Mockito.mock(Path.class);
        FileSystem fileSystem = Mockito.mock(FileSystem.class);
        FileSystemProvider provider = Mockito.mock(FileSystemProvider.class);
        ConfigHolder.init(new HashMap<>());
        ConfigHolder.setServices(path);

        Mockito.when(path.getParent()).thenReturn(path);
        Mockito.when(path.getFileSystem())
                .thenReturn(fileSystem);
        Mockito.when(fileSystem.provider())
                .thenReturn(provider);
        Mockito.when(path.resolve(Mockito.anyString()))
                .thenReturn(servicePath);
        Mockito.when(servicePath.getFileSystem()).thenReturn(fileSystem);
        Mockito.when(provider.newByteChannel(Mockito.any(), Mockito.any()))
                .thenThrow(IOException.class);
        Mockito.doThrow(IOException.class)
                .when(provider).checkAccess(Mockito.any(), Mockito.any());

        Assertions.assertThrows(ProcessorException.class, //NOSONAR
                () -> ProcessorUtils.generateSpi(environment, Collections.singletonList(file), Object.class)); //NOSONAR
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void should_generate_spi_using_nio() throws IOException {
        GeneratedFile file = new GeneratedFile(
                "package",
                TypeSpec.classBuilder("TEST").build(),
                null
        );
        Path testDir = Files.createTempDirectory("testDir");
        Path testPath = testDir.resolve("META-INF").resolve("services");
        try {
            ConfigHolder.init(new HashMap<>());
            ConfigHolder.setServices(testPath);
            ProcessorUtils.generateSpi(environment, Collections.singletonList(file), Object.class);

            Assertions.assertTrue(testDir.resolve("META-INF").toFile().exists());
            Assertions.assertTrue(testDir.resolve("META-INF").resolve("services").toFile().exists());
            Assertions.assertTrue(testDir.resolve("META-INF").resolve("services").resolve("java.lang.Object").toFile().exists());
        } finally {
            ConfigHolder.destroy();
            Files.walk(testDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            Files.deleteIfExists(testDir);
        }
    }

    private Name nameOf(String name) {
        return new Name() {

            @Override
            public boolean contentEquals(CharSequence cs) {
                return name.contentEquals(cs);
            }

            @Override
            public int length() {
                return name.length();
            }

            @Override
            public char charAt(int index) {
                return name.charAt(index);
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return name.subSequence(start, end);
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    private static final class MyConverter implements ValueConverter<String,String> {

        @Override
        public String fromSql(String val) {
            return val;
        }

        @Override
        public String toSql(String val) {
            return val;
        }
    }

    private static class FakeAccessor extends LombokMock implements ExecutableElement {

        FakeAccessor() {
            super(null);
        }

        @Override
        public List<? extends TypeParameterElement> getTypeParameters() {
            return null;
        }

        @Override
        public TypeMirror getReturnType() {
            return null;
        }

        @Override
        public List<? extends VariableElement> getParameters() {
            return null;
        }

        @Override
        public TypeMirror getReceiverType() {
            return null;
        }

        @Override
        public boolean isVarArgs() {
            return false;
        }

        @Override
        public boolean isDefault() {
            return false;
        }

        @Override
        public List<? extends TypeMirror> getThrownTypes() {
            return null;
        }

        @Override
        public AnnotationValue getDefaultValue() {
            return null;
        }

        @Override
        public TypeMirror asType() {
            return null;
        }

        @Override
        public ElementKind getKind() {
            return null;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return null;
        }

        @Override
        public Name getSimpleName() {
            return null;
        }

        @Override
        public Element getEnclosingElement() {
            return null;
        }

        @Override
        public List<? extends Element> getEnclosedElements() {
            return null;
        }

        @Override
        public List<? extends AnnotationMirror> getAnnotationMirrors() {
            return null;
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            return null;
        }

        @Override
        public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
            return null;
        }

        @Override
        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            return null;
        }
    }
}
