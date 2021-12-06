package io.github.ulisse1996.jaorm.processor.validation.impl;

import io.github.ulisse1996.jaorm.annotation.Graph;
import io.github.ulisse1996.jaorm.annotation.Relationship;
import io.github.ulisse1996.jaorm.processor.exception.ProcessorException;
import io.github.ulisse1996.jaorm.processor.util.ProcessorUtils;
import io.github.ulisse1996.jaorm.processor.validation.Validator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraphsValidator extends Validator {

    public GraphsValidator(ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
    }

    @Override
    public void validate(List<? extends Element> annotated) {
        Set<String> names = new HashSet<>();
        for (Element element : annotated) {
            TypeElement type = (TypeElement) element;
            Graph[] graphs = type.getAnnotationsByType(Graph.class);
            for (Graph graph : graphs) {
               if (!names.add(graph.name())) {
                   throw new ProcessorException(String.format("Found not unique name %s ! " +
                           "Please choose a different name", graph.name()));
               }

               String[] parts = graph.nodes();
               for (String name : parts) {
                   VariableElement field = ProcessorUtils.getFieldFromName(type, name);
                   if (field.getAnnotation(Relationship.class) == null) {
                       throw new ProcessorException(String.format("Field with name %s is not annotated with @Relationship !", name));
                   }
               }
            }
        }
    }
}
