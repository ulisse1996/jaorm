package io.github.ulisse1996.jaorm.processor.util;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import io.github.ulisse1996.jaorm.BaseDao;
import io.github.ulisse1996.jaorm.annotation.Column;
import io.github.ulisse1996.jaorm.annotation.Converter;
import io.github.ulisse1996.jaorm.annotation.Dao;
import io.github.ulisse1996.jaorm.annotation.Query;
import io.github.ulisse1996.jaorm.entity.Result;
import io.github.ulisse1996.jaorm.entity.converter.ValueConverter;
import io.github.ulisse1996.jaorm.external.LombokMock;
import io.github.ulisse1996.jaorm.external.LombokSupport;
import io.github.ulisse1996.jaorm.mapping.Cursor;
import io.github.ulisse1996.jaorm.processor.config.ConfigHolder;
import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProcessorUtils {

    private static final String REQUIRE_NON_NULL = "$T.requireNonNull(this.entity)";

    private ProcessorUtils() {}

    private static final Map<Class<?>, Class<?>> WRAPPER_TYPE_MAP;
    private static final List<String> SUPPORTED_RELATIONSHIP_TYPES = Arrays.asList(
            java.util.List.class.getName(),
            java.util.Optional.class.getName(),
            Result.class.getName(),
            Cursor.class.getName()
    );
    private static final String STARTING_GENERIC = "<";
    private static final String ENDING_GENERIC = ">";

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
        return getAllElements(processingEnvironment, typeElement)
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
        Optional<ExecutableElement> getterOpt = getAllElements(processingEnvironment, entity)
                .stream()
                .filter(e -> e.getSimpleName().contentEquals(getter) || e.getSimpleName().contentEquals(booleanGetter))
                .filter(e -> ElementKind.METHOD.equals(e.getKind()))
                .map(ExecutableElement.class::cast)
                .findFirst();
        if (getterOpt.isPresent()) {
            return getterOpt;
        } else {
            return checkExternalSupport(processingEnvironment, entity, simpleName.toString(), LombokSupport.GenerationType.GETTER);
        }
    }

    private static Optional<ExecutableElement> checkExternalSupport(ProcessingEnvironment processingEnvironment,
                                                                    TypeElement entity, String fieldName,
                                                                    LombokSupport.GenerationType generationType) {
        LombokSupport instance = LombokSupport.getInstance();
        if (!instance.isSupported()) {
            return Optional.empty();
        }

        Element field = getAllElements(processingEnvironment, entity)
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
        Optional<ExecutableElement> setterOpt = getAllElements(processingEnvironment, entity)
                .stream()
                .filter(e -> e.getSimpleName().contentEquals(setter))
                .filter(e -> ElementKind.METHOD.equals(e.getKind()))
                .map(ExecutableElement.class::cast)
                .findFirst();
        if (setterOpt.isPresent()) {
            return setterOpt;
        } else {
            return checkExternalSupport(processingEnvironment, entity, simpleName.toString(), LombokSupport.GenerationType.SETTER);
        }
    }

    public static TypeElement getFieldType(ProcessingEnvironment processingEnvironment, VariableElement variableElement) {
        TypeMirror typeMirror = variableElement.asType();
        String realType = extractRealType(typeMirror);
        TypeElement element = processingEnvironment.getElementUtils().getTypeElement(realType);
        if (element == null) {
            return WRAPPER_TYPE_MAP.entrySet()
                    .stream()
                    .filter(e -> e.getValue().getName().equals(realType))
                    .findFirst()
                    .map(e -> processingEnvironment.getElementUtils().getTypeElement(e.getKey().getName()))
                    .orElse(null);
        }

        return element;
    }

    private static String extractRealType(TypeMirror typeMirror) {
        for (String regex : SUPPORTED_RELATIONSHIP_TYPES) {
            if (typeMirror.toString().startsWith(regex)) {
                return typeMirror.toString().replace(regex, "")
                        .replace(STARTING_GENERIC, "").replace(ENDING_GENERIC, "");
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

    public static List<TypeElement> getGenericTypes(ProcessingEnvironment processingEnvironment, TypeMirror mirror, String name) {
        TypeElement typeElement = (TypeElement) processingEnvironment.getTypeUtils().asElement(mirror);
        TypeMirror converterType = typeElement.getInterfaces().get(0);
        return getParameters(processingEnvironment, converterType, name);
    }

    private static List<TypeElement> getParameters(ProcessingEnvironment processingEnvironment, TypeMirror mirror, String name) {
        String[] param = mirror.toString().replace(name, "")
                .replace(STARTING_GENERIC, "").replace(ENDING_GENERIC, "")
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
        return getGenericTypes(processingEnvironment, converterClass, ValueConverter.class.getName());
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

    public static boolean isSubType(ProcessingEnvironment processingEnvironment, TypeElement element, Class<?> klass) {
        return containsSubType(processingEnvironment, element, klass);
    }

    public static boolean isBaseDao(ProcessingEnvironment processingEnvironment, TypeElement element) {
        return containsSubType(processingEnvironment, element, BaseDao.class);
    }

    private static boolean containsSubType(ProcessingEnvironment processingEnvironment, TypeElement element, Class<?> klass) {
        for (TypeMirror typeMirror : element.getInterfaces()) {
            if (typeMirror.toString().contains(klass.getName())) {
                return true;
            }

            TypeElement typeElement = (TypeElement) processingEnvironment.getTypeUtils().asElement(typeMirror);
            boolean isBaseDao = containsSubType(processingEnvironment, typeElement, klass);
            if (isBaseDao) {
                return true;
            }
        }

        return false;
    }

    public static String getBaseDaoGeneric(ProcessingEnvironment processingEnvironment, TypeElement element) {
        String res = findBaseDaoGeneric(processingEnvironment, element.asType());

        if (res != null) {
            return res;
        }

        throw new ProcessorException("Can't find generic type of BaseDao");
    }

    private static String findBaseDaoGeneric(ProcessingEnvironment processingEnvironment, TypeMirror type) {
        for (TypeMirror typeMirror : processingEnvironment.getTypeUtils().directSupertypes(type)) {
            if (typeMirror.toString().contains(BaseDao.class.getName())) {
                return typeMirror.toString().replace(BaseDao.class.getName(), "")
                        .replace(STARTING_GENERIC, "")
                        .replace(ENDING_GENERIC, "");
            }

            if (!Object.class.getName().equalsIgnoreCase(typeMirror.toString())) {
                String res = findBaseDaoGeneric(processingEnvironment, typeMirror);
                if (res != null) {
                    return res;
                }
            }
        }

        return null;
    }

    public static List<Element> getAllValidElements(ProcessingEnvironment processingEnvironment,
                                                    TypeElement entity) {
        List<Element> elements = new ArrayList<>(getAllElements(processingEnvironment, entity));
        TypeElement objectElements = processingEnvironment.getElementUtils().getTypeElement("java.lang.Object");
        List<Element> notValidElements = processingEnvironment.getElementUtils().getAllMembers(objectElements)
                .stream()
                .filter(ele -> {
                    Set<Modifier> modifiers = ele.getModifiers();
                    return modifiers.contains(Modifier.PROTECTED) || isNotSupportedModifier(modifiers);
                }).collect(Collectors.toList());
        elements.removeAll(notValidElements);
        elements.removeIf(ele -> {
            Set<Modifier> modifiers = ele.getModifiers();
            return (modifiers.contains(Modifier.PROTECTED) && !ele.getKind().isField())
                    || (modifiers.contains(Modifier.PRIVATE) && !ele.getKind().isField())
                    || isNotSupportedModifier(modifiers);
        });
        return elements;
    }

    private static boolean isNotSupportedModifier(Set<Modifier> modifiers) {
        return modifiers.contains(Modifier.FINAL)
                || modifiers.contains(Modifier.NATIVE)
                || modifiers.contains(Modifier.STATIC);
    }

    @SuppressWarnings("unchecked")
    private static List<Element> getAllElements(ProcessingEnvironment processingEnvironment, TypeElement entity) {
        List<Element> elements = new ArrayList<>(processingEnvironment.getElementUtils().getAllMembers(entity));
        for (TypeMirror mirror : processingEnvironment.getTypeUtils().directSupertypes(entity.asType())) {
            TypeElement typeElement = extractWrapperGeneric(processingEnvironment, mirror.toString());
            if (mirror.toString().equalsIgnoreCase("java.lang.Object")
                    || ElementKind.INTERFACE.equals(typeElement.getKind())) {
                continue;
            }
            List<Element> allMembers = (List<Element>) processingEnvironment.getElementUtils().getAllMembers(typeElement);
            elements.addAll(allMembers);
            elements.addAll(getAllElements(processingEnvironment, typeElement));
        }
        return elements.stream()
                .filter(distinctByKey(Element::getSimpleName))
                .collect(Collectors.toList());
    }

    private static TypeElement extractWrapperGeneric(ProcessingEnvironment processingEnvironment, String typeElement) {
        String name = typeElement;
        if (typeElement.contains(STARTING_GENERIC)) {
            name = typeElement.substring(0, typeElement.indexOf(STARTING_GENERIC));
        }

        return processingEnvironment.getElementUtils().getTypeElement(name);
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
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

    public static boolean isLombokMock(ProcessingEnvironment processingEnvironment, TypeElement entity, Element e) {
        return findGetterOpt(processingEnvironment, entity, e.getSimpleName())
                .map(LombokMock.class::isInstance)
                .orElse(false)
                || findSetterOpt(processingEnvironment, entity, e.getSimpleName())
                .map(LombokMock.class::isInstance)
                .orElse(false);
    }

    public static void generateSpi(ProcessingEnvironment environment, List<GeneratedFile> generatedFiles, Class<?> serviceClass) {
        try {
            if (generatedFiles.isEmpty()) {
                return;
            }
            Path services = ConfigHolder.getServices();
            if (services == null) {
                fallbackGenerateSpi(environment, generatedFiles, serviceClass);
                return;
            }
            Map.Entry<Path, Boolean> entry = getOrCreateFile(serviceClass);
            readAndMergeLines(generatedFiles, entry.getKey());
        } catch (IOException ex) {
            StringWriter writer = new StringWriter();
            PrintWriter p = new PrintWriter(writer);
            ex.printStackTrace(p);
            throw new ProcessorException("Error during Resource creation : " + writer);
        }
    }

    private static void fallbackGenerateSpi(ProcessingEnvironment processingEnvironment, List<GeneratedFile> generatedFiles, Class<?> serviceClass) {
        try {
            String name = serviceClass.getName();
            FileObject resource = processingEnvironment.getFiler().createResource(
                    StandardLocation.CLASS_OUTPUT, "", String.format("META-INF/services/%s", name));

            if (resource.toUri().toString().startsWith("mem")) {
                return;
            }

            readAndMergeLines(generatedFiles, Paths.get(resource.toUri()));
        } catch (IOException ex) {
            throw new ProcessorException("Error during Resource creation : " + ex.getMessage());
        }
    }

    private static void readAndMergeLines(List<GeneratedFile> generatedFiles, Path path) throws IOException {
        List<String> written = Files.readAllLines(path);
        List<String> toGen = generatedFiles.stream()
                .map(generatedFile -> String.format("%s.%s",
                        generatedFile.getPackageName(),
                        generatedFile.getSpec().name))
                .collect(Collectors.toList());
        Set<String> toWrite = new HashSet<>();
        toWrite.addAll(written);
        toWrite.addAll(toGen);
        Files.write(path, String.join("\n", toWrite).getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static Map.Entry<Path, Boolean> getOrCreateFile(Class<?> serviceClass) throws IOException {
        boolean missing = false;
        Path services = ConfigHolder.getServices();
        createIfMissing(services);
        Path spi = services.resolve(serviceClass.getName());
        if (!Files.exists(spi)) {
            Files.createFile(spi);
            missing = true;
        }
        return new AbstractMap.SimpleEntry<>(spi, missing);
    }

    private static void createIfMissing(Path services) throws IOException {
        Path meta = services.getParent();
        if (!Files.exists(meta)) {
            Files.createDirectory(meta);
        }
        if (!Files.exists(services)) {
            Files.createDirectory(services);
        }
    }

    public static List<ExecutableElement> getAllMethods(ProcessingEnvironment processingEnvironment, TypeElement element) {
        List<? extends Element> elements = getAllValidElements(processingEnvironment, element);
        List<ExecutableElement> methods = elements.stream()
                .filter(ExecutableElement.class::isInstance)
                .map(ExecutableElement.class::cast)
                .filter(e -> !e.getKind().equals(ElementKind.CONSTRUCTOR))
                .filter(e -> !e.isDefault())
                .collect(Collectors.toList());
        methods.addAll(ProcessorUtils.appendExternalGeneratedMethods(processingEnvironment, element, elements));
        return methods;
    }

    public static String extractParameterNames(ExecutableElement m) {
        if (!m.getParameters().isEmpty()) {
            return m.getParameters().stream()
                    .map(VariableElement::getSimpleName)
                    .collect(Collectors.joining(","));
        }

        return "";
    }

    public static MethodSpec buildDelegateMethod(ExecutableElement m, TypeElement entity, boolean forEntity) {
        MethodSpec.Builder builder = MethodSpec.overriding(m)
                .addStatement(REQUIRE_NON_NULL, Objects.class);
        String variables = ProcessorUtils.extractParameterNames(m);

        if (m.getSimpleName().contentEquals("equals")) {
            builder.addCode(buildCustomEquals(m.getParameters().get(0).getSimpleName().toString(), entity));
        } else if (m.getReturnType() instanceof NoType) {
            if (forEntity) {
                builder.addStatement("this.modified = true");
            }
            builder.addStatement("this.entity.$L($L)", m.getSimpleName(), variables);
        } else {
            builder.addStatement("return this.entity.$L($L)", m.getSimpleName(), variables);
        }
        return builder.build();
    }

    private static CodeBlock buildCustomEquals(String paramName, TypeElement entity) {
        return CodeBlock.builder()
                .beginControlFlow("if (getClass().isInstance($L))", paramName)
                .addStatement("return this.entity.equals((($TDelegate) $L).entity)", entity, paramName)
                .endControlFlow()
                .addStatement("return this.entity.equals($L)", paramName)
                .build();
    }

    public static List<TypeMirror> getBeforeConversionTypes(ProcessingEnvironment processingEnvironment, VariableElement field) {
        List<TypeElement> typeGenerics = ProcessorUtils.getConverterTypes(processingEnvironment, field);
        TypeMirror toConversion = typeGenerics.get(1).asType();
        PrimitiveType unboxed = ProcessorUtils.getUnboxed(processingEnvironment, typeGenerics.get(1));
        return Stream.of(toConversion, unboxed).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static boolean hasExternalConstructor(TypeElement entity) {
        LombokSupport lombokSupport = LombokSupport.getInstance();
        return lombokSupport.hasLombokNoArgs(entity);
    }

    public static VariableElement getFieldFromName(TypeElement type, String name) {
        return type.getEnclosedElements()
                .stream()
                .filter(e -> e.getSimpleName().contentEquals(name))
                .findFirst()
                .map(VariableElement.class::cast)
                .orElseThrow(() -> new ProcessorException("Can't find field with name " + name));
    }

    public static String getSqlOrSqlFromFile(String sql, ProcessingEnvironment processingEnvironment) {
        if (sql.endsWith(".sql")) {
            try {
                FileObject resource = getResource(sql, processingEnvironment);
                URI uri = resource.toUri();
                return String.join("", Files.readAllLines(Paths.get(uri)));
            } catch (IOException ex) {
                throw new ProcessorException(ex.getMessage());
            }
        }

        return sql;
    }

    private static FileObject getResource(String sql, ProcessingEnvironment processingEnvironment) throws IOException {
        if (sql.startsWith(File.separator)) {
            return processingEnvironment.getFiler().getResource(StandardLocation.CLASS_PATH, "", sql.substring(1));
        } else {
            return processingEnvironment.getFiler().getResource(StandardLocation.CLASS_PATH, "", sql);
        }
    }
}
