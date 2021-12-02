package io.github.ulisse1996.jaorm.tools.util;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;

import java.lang.annotation.Annotation;
import java.util.Optional;

public class ValidationUtils {

    private ValidationUtils() {}

    public static Expression getExpression(NodeWithAnnotations<?> node, Class<? extends Annotation> annotation, String name) {
        Optional<AnnotationExpr> annExpr = node.getAnnotationByClass(annotation);
        return annExpr.flatMap(annotationExpr -> annotationExpr.findAll(MemberValuePair.class)
                        .stream()
                        .filter(m -> m.getName().getIdentifier().equalsIgnoreCase(name))
                        .map(MemberValuePair::getValue)
                        .findFirst())
                .orElse(null);
    }

    public static Expression getSingleValueExpression(FieldDeclaration field, Class<? extends Annotation> annotation) {
        Optional<AnnotationExpr> annotationByClass = field.getAnnotationByClass(annotation);
        if (!annotationByClass.isPresent() || !(annotationByClass.get() instanceof SingleMemberAnnotationExpr)) {
            return null;
        }

        SingleMemberAnnotationExpr single = ((SingleMemberAnnotationExpr) annotationByClass.get());
        return single.getMemberValue();
    }
}
