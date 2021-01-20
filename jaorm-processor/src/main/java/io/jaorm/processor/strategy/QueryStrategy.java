package io.jaorm.processor.strategy;

import com.squareup.javapoet.CodeBlock;
import io.jaorm.entity.sql.SqlAccessor;
import io.jaorm.entity.sql.SqlParameter;
import io.jaorm.processor.annotation.Param;
import io.jaorm.processor.exception.ProcessorException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum QueryStrategy implements ParametersStrategy {

    WILDCARD() {
        @Override
        public boolean isValid(String query) {
            return Pattern.compile("\\?\\B").matcher(query).find();
        }

        @Override
        public int getParamNumber(String query) {
            int num = 0;
            Matcher matcher = Pattern.compile("\\?\\B").matcher(query);
            while (matcher.find()) {
                num++;
            }
            return num;
        }

        @Override
        public List<CodeBlock> extract(ProcessingEnvironment procEnv, String query, ExecutableElement method) {
            return IntStream.range(0, getParamNumber(query))
                    .mapToObj(i -> {
                        VariableElement param = method.getParameters().get(i);
                        TypeMirror type = param.asType();
                        return getStatement(param, type);
                    }).collect(Collectors.toList());
        }

        @Override
        public String replaceQuery(String query) {
            return query;
        }
    },
    NAMED {

        private static final String REGEX = ":\\w*";

        @Override
        public boolean isValid(String query) {
            return Pattern.compile(REGEX).matcher(query).find();
        }

        @Override
        public int getParamNumber(String query) {
            return new HashSet<>(getWords(REGEX, query)).size();
        }

        @Override
        public List<CodeBlock> extract(ProcessingEnvironment procEnv, String query, ExecutableElement method) {
            return getCodeBlocks(query, method);
        }

        private List<CodeBlock> getCodeBlocks(String query, ExecutableElement method) {
            return getWords(REGEX, query)
                    .stream()
                    .map(s -> {
                        VariableElement element = getVariable(method, s);
                        TypeMirror type = element.asType();
                        return getStatement(element, type);
                    }).collect(Collectors.toList());
        }

        private VariableElement getVariable(ExecutableElement method, String s) {
            return getVariableElement(method, s);
        }

        @Override
        public String replaceQuery(String query) {
            return Pattern.compile(REGEX).matcher(query).replaceAll("?");
        }
    },
    ORDERED_WILDCARD {
        private static final String REGEX = "(\\?\\d*)";

        @Override
        public boolean isValid(String query) {
            return Pattern.compile(REGEX).matcher(query).find();
        }

        @Override
        public int getParamNumber(String query) {
            List<Integer> num = getNumbers(query);
            return num.stream().max(Integer::compareTo).orElse(-1);
        }

        private List<Integer> getNumbers(String query) {
            List<Integer> num = new ArrayList<>();
            Matcher matcher = Pattern.compile(REGEX).matcher(query);
            while (matcher.find()) {
                num.add(Integer.valueOf(matcher.group().substring(1)));
            }
            return num;
        }

        @Override
        public List<CodeBlock> extract(ProcessingEnvironment procEnv, String query, ExecutableElement method) {
            return getNumbers(query)
                    .stream()
                    .map(i -> {
                        int realNum = i - 1; // We start at 1 as sql
                        VariableElement param = method.getParameters().get(realNum);
                        TypeMirror type = param.asType();
                        return QueryStrategy.getStatement(param, type);
                    }).collect(Collectors.toList());
        }

        @Override
        public String replaceQuery(String query) {
            return Pattern.compile(REGEX).matcher(query).replaceAll("?");
        }
    },
    AT_NAMED {

        private static final String REGEX = "@\\w*";

        @Override
        public boolean isValid(String query) {
            return Pattern.compile(REGEX).matcher(query).find();
        }

        @Override
        public int getParamNumber(String query) {
            return new HashSet<>(QueryStrategy.getWords(REGEX, query)).size();
        }

        @Override
        public List<CodeBlock> extract(ProcessingEnvironment procEnv, String query, ExecutableElement method) {
            return getCodeBlocks(query, method);
        }

        private List<CodeBlock> getCodeBlocks(String query, ExecutableElement method) {
            return getWords(REGEX, query)
                    .stream()
                    .map(s -> {
                        VariableElement element = getVariable(method, s);
                        TypeMirror type = element.asType();
                        return getStatement(element, type);
                    }).collect(Collectors.toList());
        }

        private VariableElement getVariable(ExecutableElement method, String s) {
            return getVariableElement(method, s);
        }

        @Override
        public String replaceQuery(String query) {
            return Pattern.compile(REGEX).matcher(query).replaceAll("?");
        }
    };

    private static VariableElement getVariableElement(ExecutableElement method, String s) {
        return method.getParameters()
                .stream()
                .filter(p -> {
                    Param param = p.getAnnotation(Param.class);
                    if (param != null) {
                        return param.name().equalsIgnoreCase(s);
                    } else {
                        return p.getSimpleName().toString().equalsIgnoreCase(s);
                    }
                }).findFirst()
                .orElseThrow(() -> new ProcessorException("Can't find parameter with name " + s));
    }

    private static List<String> getWords(String regex, String query) {
        List<String> foundWords = new ArrayList<>();
        Matcher matcher = Pattern.compile(regex).matcher(query);
        while (matcher.find()) {
            foundWords.add(matcher.group().substring(1));
        }
        return foundWords;
    }

    private static CodeBlock getStatement(VariableElement element, TypeMirror type) {
        return CodeBlock.builder()
                .addStatement("params.add(new $T($L, $T.find($T.class).getSetter()))",
                        SqlParameter.class, element.getSimpleName(), SqlAccessor.class, type)
                .build();
    }
}