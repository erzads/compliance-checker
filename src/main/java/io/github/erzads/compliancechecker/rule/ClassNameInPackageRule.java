package io.github.erzads.compliancechecker.rule;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author danilo.saita on 08/05/2021.
 */
public class ClassNameInPackageRule implements Rule {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassNameInPackageRule.class);

    private String basePackage;

    private String packageNameRegex;

    private Pattern classNameRegex;

    private Pattern classNameWhiteListRegex;

    @Override
    public Rule setUp(List<String> ruleParams) {
        this.basePackage = ruleParams.get(0);
        this.packageNameRegex = ruleParams.get(1);
        this.classNameRegex = Pattern.compile(ruleParams.get(2));
        this.classNameWhiteListRegex = Pattern.compile(ruleParams.size() == 4 ? ruleParams.get(3) : "$^");
        return this;
    }

    @Override
    public boolean isCompliant() {
        boolean isCompliant = true;
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false))
                .setUrls(ClasspathHelper.forPackage(this.basePackage))
                .filterInputsBy(new FilterBuilder().include(packageNameRegex)));

        Set<Class<?>> foundClasses = reflections.getSubTypesOf(Object.class);
        for (Class<?> foundClass : foundClasses) {
            if (!classNameWhiteListRegex.matcher(foundClass.getName()).matches()) {
                if (classNameRegex.matcher(foundClass.getName()).matches()) {
                    LOGGER.info("[COMPLIANT] - {}", foundClass.getName());
                } else {
                    isCompliant = false;
                    LOGGER.error("[NON-COMPLIANT] - {} should match {}", foundClass.getName(), classNameRegex.pattern());
                }
            }
        }
        return isCompliant;
    }
}
