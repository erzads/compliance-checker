package io.github.erzads.compliancechecker.rule;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author danilo.saita on 08/05/2021.
 */
public class AnnotatedClassMethodReturnTypeRule implements Rule {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotatedClassMethodReturnTypeRule.class);

    private String basePackage;

    private String annotationName;

    private Pattern returnTypeRegex;

    @Override
    public Rule setUp(List<String> ruleParams) {
        this.basePackage = ruleParams.get(0);
        this.annotationName = ruleParams.get(1);
        this.returnTypeRegex = Pattern.compile(ruleParams.get(2));
        return this;
    }

    @Override
    public boolean isCompliant() {
        boolean isCompliant = true;
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false))
                .setUrls(ClasspathHelper.forPackage(this.basePackage)));

        Set<Class<?>> foundClasses = reflections.getSubTypesOf(Object.class);
        for (Class<?> foundClass : foundClasses) {
            Annotation[] declaredAnnotations = foundClass.getDeclaredAnnotations();
            if (Arrays.stream(declaredAnnotations).anyMatch(annotation -> annotationName.equals(annotation.annotationType().getSimpleName()))){
                Method[] methods = foundClass.getDeclaredMethods();
                for (Method method : methods) {
                    boolean isPublic = (method.getModifiers() & Modifier.PUBLIC) != 0;
                    Class<?> returnType = method.getReturnType();
                    if (isPublic && !returnType.isAssignableFrom(void.class)){
                        if (returnTypeRegex.matcher(returnType.getSimpleName()).matches()) {
                            LOGGER.info("[COMPLIANT] - {}", foundClass.getName());
                        } else {
                            isCompliant = false;
                            LOGGER.error("[NON-COMPLIANT] - {} should match {}", foundClass.getName(), returnTypeRegex.pattern());
                        }
                    }
                }
            }
        }
        return isCompliant;
    }
}
