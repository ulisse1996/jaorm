package io.github.ulisse1996.jaorm.processor.util;

import com.squareup.javapoet.JavaFile;
import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Converter;
import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.annotation.Query;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;
import io.github.ulisse1996.jaorm.external.LombokMock;
import io.github.ulisse1996.jaorm.external.LombokSupport;
import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProcessorUtils {

    private ProcessorUtils() {}

    private static final Map<Class<?>, Class<?>> WRAPPER_TYPE_MAP;
    private static final List<String> SUPPORTED_RELATIONSHIP_TYPES = Arrays.asList(
            java.util.List.class.getName(),
            java.util.Optional.class.getName(),
            Result.class.getName()
    );
    private static final String STARTING_GENERIC = "<";
    private static final String ENDING_GENERING = ">";

    static {
        Map<Class<?>,Class<?>> map = new HashMap<>(16);
        map.put(Integer.class, int.class);
        map.put(Byte.class, byte.class);
        map.put(Character.class, char.class);
        map.put(Boolean.class, boolean.class);
        map.put(Double.class, double.class);
        map.put(Float.class, float.class);
        map.put(Long.class, long.class);
        map.put(Short.class, short.class);
        map.put(Void.class, void.class);
        WRAPPER_TYPE_MAP = Collections.unmodifiableMap(map);
    }

    public static List<ExecutableElement> getConstructors(ProcessingEnvironment processingEnvironment,
                                                          TypeElement typeElement) {
        return processingEnvironment.getElementUtils().getAllMembers(typeElement)
                .stream()
                .filter(ele -> ElementKind.CONSTRUCTOR.equals(ele.getKind()))
                .map(ExecutableElement.class::cast)
                .collect(Collectors.toList());
    }

    @SafeVarargs
    public static List<Element> getAnnotated(ProcessingEnvironment processingEnvironment,
                                             TypeElement typeElement, Class<? extends Annotation>... annotations) {
        return processingEnvironment.getElementUtils().getAllMembers(typeElement)
                .stream()
                .filter(ele -> Stream.of(annotations).anyMatch(an -> ele.getAnnotation(an) != null))
                .collect(Collectors.toList());
    }

    public static Optional<ExecutableElement> findGetterOpt(ProcessingEnvironment processingEnvironment,
                                                            TypeElement entity, Name simpleName) {
        String fieldName = simpleName.toString();
        fieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        String getter = "get" + fieldName;
        String booleanGetter = "is" + fieldName;
        Optional<ExecutableElement> getterOpt = processingEnvironment.getElementUtils().getAllMembers(entity)
                .stream()
                .filter(e -> e.getSimpleName().contentEquals(getter) || e.getSimpleName().contentEquals(booleanGetter))
                .filter(e -> ElementKind.METHOD.equals(e.getKind()))
                .map(ExecutableElement.class::cast)
                .findFirst();
        if (getterOpt.isPresent()) {
            return getterOpt;
        } else {
            return checkExternalSupport(entity, simpleName.toString(), LombokSupport.GenerationType.GETTER);
        }
    }

    private static Optional<ExecutableElement> checkExternalSupport(TypeElement entity, String fieldName,
                                                                    LombokSupport.GenerationType generationType) {
        LombokSupport instance = LombokSupport.getInstance();
        if (!instance.isSupported()) {
            return Optional.empty();
        }

        Element field = entity.getEnclosedElements()
                .stream()
                .filter(e -> e.getSimpleName().toString().equalsIgnoreCase(fieldName))
                .findFirst()
                .orElseThrow(() -> new ProcessorException("Can't find field " + fieldName));

        if (!instance.isLombokGenerated(field)) {
            return Optional.empty();
        }

        return Optional.of(instance.generateFakeElement(field, generationType))
                .map(ExecutableElement.class::cast);
    }

    public static ExecutableElement findGetter(ProcessingEnvironment processingEnvironment,
                                               TypeElement entity, Name simpleName) {
        return findGetterOpt(processingEnvironment, entity, simpleName)
                .orElseThrow(() -> new ProcessorException("Getter is not available but it was validated"));
    }

    public static ExecutableElement findSetter(ProcessingEnvironment processingEnvironment,
                                               TypeElement entity, Name simpleName) {
        return findSetterOpt(processingEnvironment, entity, simpleName)
                .orElseThrow(() -> new ProcessorException("Setter is not available but it was validated"));
    }

    public static Optional<ExecutableElement> findSetterOpt(ProcessingEnvironment processingEnvironment,
                                                            TypeElement entity, Name simpleName) {
        String fieldName = simpleName.toString();
        fieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        String setter = "set" + fieldName;
        Optional<ExecutableElement> setterOpt = processingEnvironment.getElementUtils().getAllMembers(entity)
                .stream()
                .filter(e -> e.getSimpleName().contentEquals(setter))
                .filter(e -> ElementKind.METHOD.equals(e.getKind()))
                .map(ExecutableElement.class::cast)
                .findFirst();
        if (setterOpt.isPresent()) {
            return setterOpt;
        } else {
            return checkExternalSupport(entity, simpleName.toString(), LombokSupport.GenerationType.SETTER);
        }
    }

    public static TypeElement getFieldType(ProcessingEnvironment processingEnvironment, VariableElement variableElement) {
        TypeMirror typeMirror = variableElement.asType();
        String realType = extractRealType(typeMirror);
        return processingEnvironment.getElementUtils().getTypeElement(realType);
    }

    private static String extractRealType(TypeMirror typeMirror) {
        for (String regex : SUPPORTED_RELATIONSHIP_TYPES) {
            if (typeMirror.toString().startsWith(regex)) {
                return typeMirror.toString().replace(regex, "")
                        .replace(STARTING_GENERIC, "").replace(ENDING_GENERING, "");
            }
        }

        return typeMirror.toString();
    }

    public static PrimitiveType getUnboxed(ProcessingEnvironment processingEnvironment, TypeElement element) {
        if (WRAPPER_TYPE_MAP.keySet().stream().anyMatch(c -> c.getName().equalsIgnoreCase(element.getQualifiedName().toString()))) {
            return processingEnvironment.getTypeUtils().unboxedType(element.asType());
        }

        return null;
    }

    public static List<TypeElement> getGenericTypes(ProcessingEnvironment processingEnvironment, TypeMirror mirror) {
        TypeElement typeElement = (TypeElement) processingEnvironment.getTypeUtils().asElement(mirror);
        TypeMirror converterType = typeElement.getInterfaces().get(0);
        return getParameters(processingEnvironment, converterType);
    }

    private static List<TypeElement> getParameters(ProcessingEnvironment processingEnvironment, TypeMirror mirror) {
        String[] param = mirror.toString().replace(ValueConverter.class.getName(), "")
                .replace(STARTING_GENERIC, "").replace(ENDING_GENERING, "")
                .split(",");
        return Stream.of(param)
                .map(String::trim)
                .map(p -> processingEnvironment.getElementUtils().getTypeElement(p))
                .collect(Collectors.toList());
    }

    public static VariableElement getFieldWithColumnName(ProcessingEnvironment processingEnvironment,
                                                         TypeElement type, String column) {
        return getFieldWithColumnNameOpt(processingEnvironment, type, column)
                .orElseThrow(() -> new ProcessorException("Column is not available but it was validated"));
    }

    public static Optional<VariableElement> getFieldWithColumnNameOpt(ProcessingEnvironment processingEnvironment,
                                                                      TypeElement type, String targetColumn) {
        return processingEnvironment.getElementUtils().getAllMembers(type)
                .stream()
                .filter(ele -> ele.getAnnotation(Column.class) != null)
                .filter(ele -> ele.getAnnotation(Column.class).name().equalsIgnoreCase(targetColumn))
                .map(VariableElement.class::cast)
                .findFirst();
    }

    public static void generate(ProcessingEnvironment processingEnvironment, GeneratedFile generatedFile) {
        try {
            JavaFile file = JavaFile.builder(generatedFile.getPackageName(), generatedFile.getSpec())
                    .skipJavaLangImports(true)
                    .indent("    ")
                    .addFileComment("Jaorm Processed at " +  LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
            file.writeTo(processingEnvironment.getFiler());
        } catch (IOException ex) {
            throw new ProcessorException("Error during Class creation : " + ex.getMessage());
        }
    }

    public static String getConverterCaller(ProcessingEnvironment processingEnvironment,
                                            VariableElement variableElement) {
        TypeMirror converterClass = getConverterClass(processingEnvironment, variableElement);
        return isConverterSingleton(processingEnvironment, converterClass)
                ? converterClass.toString() +  "." + getConvertSingleton(processingEnvironment, converterClass)
                : "new "  + converterClass.toString() + "()";
    }

    private static String getConvertSingleton(ProcessingEnvironment processingEnvironment,
                                              TypeMirror converterClass) {
        TypeElement type = (TypeElement) processingEnvironment.getTypeUtils().asElement(converterClass);
        return processingEnvironment.getElementUtils().getAllMembers(type)
                .stream()
                .filter(ele -> ele.getKind().isField())
                .map(VariableElement.class::cast)
                .filter(ele -> ele.asType().toString().equals(converterClass.toString()))
                .findFirst()
                .map(v -> v.getSimpleName().toString())
                .orElseThrow(() -> new ProcessorException("Can't find Singleton field for Converter " + converterClass));
    }

    private static boolean isConverterSingleton(ProcessingEnvironment processingEnvironment,
                                                TypeMirror converterClass) {
        TypeElement type = (TypeElement) processingEnvironment.getTypeUtils().asElement(converterClass);
        return processingEnvironment.getElementUtils().getAllMembers(type)
                .stream()
                .filter(ele -> ele.getKind().isField())
                .map(VariableElement.class::cast)
                .anyMatch(ele -> ele.asType().toString().equals(converterClass.toString()));
    }

    public static List<TypeElement> getConverterTypes(ProcessingEnvironment processingEnvironment,
                                                      VariableElement variableElement) {
        TypeMirror converterClass = getConverterClass(processingEnvironment, variableElement);
        return getGenericTypes(processingEnvironment, converterClass);
    }

    private static TypeMirror getConverterClass(ProcessingEnvironment environment, VariableElement variableElement) {
        TypeMirror converterClass;
        Converter converter = variableElement.getAnnotation(Converter.class);
        try {
            // Only way to get class
            Class<?> klass = converter.value();
            converterClass = environment.getElementUtils().getTypeElement(klass.getName()).asType();
        } catch (MirroredTypeException ex) {
            converterClass = ex.getTypeMirror();
        }
        return Objects.requireNonNull(converterClass, "Can't get converter type !");
    }

    public static ExecutableElement getMethod(ProcessingEnvironment processingEnvironment,
                                              String name, Class<?> klass) {
        TypeElement element = processingEnvironment.getElementUtils().getTypeElement(klass.getName());
        return processingEnvironment.getElementUtils().getAllMembers(element)
                .stream()
                .filter(f -> f.getSimpleName().toString().equalsIgnoreCase(name))
                .findFirst()
                .map(ExecutableElement.class::cast)
                .orElseThrow(() -> new ProcessorException("Can't find method " + name));
    }

    public static List<TypeElement> getAllDao(RoundEnvironment roundEnvironment) {
        return Stream.concat(
                roundEnvironment.getElementsAnnotatedWith(Dao.class).stream(),
                roundEnvironment.getElementsAnnotatedWith(Query.class).stream()
        ).map(ele -> {
            if (ele instanceof ExecutableElement) {
                return ele.getEnclosingElement();
            } else {
                return ele;
            }
        }).map(TypeElement.class::cast)
                .distinct()
                .collect(Collectors.toList());
    }

    public static boolean isBaseDao(TypeElement element) {
        if (!element.getInterfaces().isEmpty()) {
            for (TypeMirror typeMirror : element.getInterfaces()) {
                if (typeMirror.toString().contains(BaseDao.class.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    public static String getBaseDaoGeneric(TypeElement element) {
        for (TypeMirror typeMirror : element.getInterfaces()) {
            if (typeMirror.toString().contains(BaseDao.class.getName())) {
                return typeMirror.toString().replace(BaseDao.class.getName(), "")
                        .replace(STARTING_GENERIC, "")
                        .replace(ENDING_GENERING, "");
            }
        }

        throw new ProcessorException("Can't find generic type of BaseDao");
    }

    public static List<Element> getAllValidElements(ProcessingEnvironment processingEnvironment,
                                                    TypeElement entity) {
        List<Element> elements = new ArrayList<>(processingEnvironment.getElementUtils().getAllMembers(entity));
        TypeElement objectElements = processingEnvironment.getElementUtils().getTypeElement("java.lang.Object");
        List<Element> notValidElements = processingEnvironment.getElementUtils().getAllMembers(objectElements)
                .stream()
                .filter(ele -> {
                    Set<Modifier> modifiers = ele.getModifiers();
                    return modifiers.contains(Modifier.FINAL)
                            || modifiers.contains(Modifier.NATIVE)
                            || modifiers.contains(Modifier.PROTECTED);
                }).collect(Collectors.toList());
        elements.removeAll(notValidElements);
        elements.removeIf(ele -> {
            Set<Modifier> modifiers = ele.getModifiers();
            return modifiers.contains(Modifier.PROTECTED)
                    || (modifiers.contains(Modifier.PRIVATE) && !ele.getKind().isField());
        });
        return elements;
    }

    public static List<ExecutableElement> appendExternalGeneratedMethods(ProcessingEnvironment processingEnvironment,
                                                                         TypeElement entity, List<? extends Element> elements) {
        LombokSupport lombokSupport = LombokSupport.getInstance();
        if (!lombokSupport.isSupported()) {
            return Collections.emptyList();
        }
        List<Element> toBeCreated = elements.stream()
                .filter(e -> e.getKind().isField())
                .filter(e -> isLombokMock(processingEnvironment, entity, e)
                ).collect(Collectors.toList());
        return toBeCreated.stream()
                .flatMap(e -> {
                    List<Element> list = new ArrayList<>();
                    if (findGetterOpt(processingEnvironment, entity, e.getSimpleName()).map(LombokMock.class::isInstance)
                            .orElse(false)) {
                        list.add(lombokSupport.generateFakeElement(e, LombokSupport.GenerationType.GETTER));
                    }
                    if (findSetterOpt(processingEnvironment, entity, e.getSimpleName()).map(LombokMock.class::isInstance)
                            .orElse(false)) {
                        list.add(lombokSupport.generateFakeElement(e, LombokSupport.GenerationType.SETTER));
                    }
                    return list.stream();
                }).map(ExecutableElement.class::cast)
                .collect(Collectors.toList());
    }

    static boolean isLombokMock(ProcessingEnvironment processingEnvironment, TypeElement entity, Element e) {
        return findGetterOpt(processingEnvironment, entity, e.getSimpleName())
                .map(LombokMock.class::isInstance)
                .orElse(false)
                || findSetterOpt(processingEnvironment, entity, e.getSimpleName())
                .map(LombokMock.class::isInstance)
                .orElse(false);
    }
}
