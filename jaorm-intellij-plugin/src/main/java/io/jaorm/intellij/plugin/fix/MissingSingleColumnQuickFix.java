package io.jaorm.intellij.plugin.fix;

import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.jaorm.intellij.plugin.JaormMessageBundler;
import io.jaorm.intellij.plugin.util.EntityDefinition;
import org.jetbrains.annotations.NotNull;

public class MissingSingleColumnsQuickFix extends BaseFix {

    private final EntityDefinition.ColumnDefinition columnDefinition;

    public MissingSingleColumnsQuickFix(PsiElement element, EntityDefinition.ColumnDefinition columnDefinition) {
        super(element);
        this.columnDefinition = columnDefinition;
    }

    @Override
    protected void doInvoke(Project project, PsiFile file, PsiElement startElement, PsiElement endElement) {

    }

    @Override
    public @IntentionName @NotNull String getText() {
        return JaormMessageBundler.fixes("jaorm.table.missing.column", columnDefinition.getName());
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return JaormMessageBundler.fixes("jaorm.table.missing.column.family");
    }
}
