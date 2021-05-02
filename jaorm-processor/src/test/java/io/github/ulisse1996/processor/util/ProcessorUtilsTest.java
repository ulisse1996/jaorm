package io.github.ulisse1996.processor.util;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.github.ulisse1996.annotation.*;
import io.github.ulisse1996.processor.exception.ProcessorException;
import io.github.ulisse1996.entity.converter.ValueConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.io.IOException;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class ProcessorUtilsTest {

    @Mock private ProcessingEnvironment environment;
    @Mock private TypeElement element;
    @Mock private Elements elements;
    @Mock private Types types;
    @Mock private Filer filer;
    @Mock private VariableElement variableElement;

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
        Mockito.when(elements.getAllMembers(element))
                .then(invocationOnMock -> Arrays.asList(annotated, notAnnotated));
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
        String valueConverterName = "io.github.ulisse1996.entity.converter.ValueConverter<Type1, Type2>";
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
                ProcessorUtils.getGenericTypes(environment, mirror));
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
                .then(invocation -> Collections.singletonList(mirror));
        Mockito.when(mirror.toString())
                .thenReturn("notADao");
        Assertions.assertFalse(ProcessorUtils.isBaseDao(element));
    }

    @Test
    void should_return_true_for_base_dao() {
        TypeMirror mirror = Mockito.mock(TypeMirror.class);
        Mockito.when(element.getInterfaces())
                .then(invocation -> Collections.singletonList(mirror));
        Mockito.when(mirror.toString())
                .thenReturn("io.github.ulisse1996.BaseDao<Entity>");
        Assertions.assertTrue(ProcessorUtils.isBaseDao(element));
    }

    @Test
    void should_return_entity_from_dao() {
        TypeMirror mirror = Mockito.mock(TypeMirror.class);
        TypeMirror emptyMirror = Mockito.mock(TypeMirror.class);
        Mockito.when(emptyMirror.toString())
                .thenReturn("empty");
        Mockito.when(element.getInterfaces())
                .then(invocation -> Arrays.asList(emptyMirror, mirror));
        Mockito.when(mirror.toString())
                .thenReturn("io.github.ulisse1996.BaseDao<Entity>");
        String name = "Entity";
        Assertions.assertEquals(name, ProcessorUtils.getBaseDaoGeneric(element));
    }

    @Test
    void should_return_all_valid_elements() {
        List<Element> objectElements = getObjectElements();
        List<Element> allElements = new ArrayList<>(objectElements);
        TypeElement objectElement = Mockito.mock(TypeElement.class);
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
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
                .thenReturn("io.github.ulisse1996.entity.converter.ValueConverter<String,Integer>");
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
        Mockito.when(nativeElement.getModifiers())
                .thenReturn(Collections.singleton(Modifier.NATIVE));
        Mockito.when(protectedElement.getModifiers())
                .thenReturn(Collections.singleton(Modifier.PROTECTED));
        Mockito.when(privateElement.getModifiers())
                .thenReturn(Collections.singleton(Modifier.PRIVATE));
        Mockito.when(privateElement.getKind())
                .thenReturn(ElementKind.FIELD);
        return Arrays.asList(finalElement, nativeElement, protectedElement, privateElement);
    }

    @Test
    void should_throw_exception_for_missing_dao_generic() {
        Mockito.when(element.getInterfaces())
                .then(invocation -> Collections.emptyList());
        Assertions.assertThrows(ProcessorException.class, () -> ProcessorUtils.getBaseDaoGeneric(element));
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
        Mockito.when(elements.getAllMembers(element))
                .then(invocation -> Arrays.asList(setter, notMySetter));
        Mockito.when(setter.getSimpleName())
                .thenReturn(nameOf("setName"));
        Mockito.when(setter.getKind())
                .thenReturn(ElementKind.METHOD);
        return setter;
    }

    private ExecutableElement mockFindGetter(Name name, boolean booleanGetter) {
        ExecutableElement getter = Mockito.mock(ExecutableElement.class);
        ExecutableElement notMyGetter = Mockito.mock(ExecutableElement.class);
        Mockito.when(name.toString())
                .thenReturn("name");
        Mockito.when(environment.getElementUtils())
                .thenReturn(elements);
        Mockito.when(elements.getAllMembers(element))
                .then(invocation -> Arrays.asList(getter, notMyGetter));
        Mockito.when(getter.getSimpleName())
                .thenReturn(nameOf(booleanGetter ? "isName" : "getName"));
        Mockito.when(getter.getKind())
                .thenReturn(ElementKind.METHOD);
        return getter;
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

            @SuppressWarnings("NullableProblems")
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
}
