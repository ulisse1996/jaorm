package io.github.ulisse1996.jaorm;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

public class ExceptionLogger implements TestExecutionExceptionHandler {

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        System.out.println(throwable.getMessage());
        throwable.printStackTrace();
        throw throwable;
    }
}
