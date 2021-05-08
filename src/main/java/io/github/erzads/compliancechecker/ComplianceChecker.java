package io.github.erzads.compliancechecker;

import io.github.erzads.compliancechecker.exception.ComplianceCheckerException;
import io.github.erzads.compliancechecker.exception.ComplianceCheckerValidationException;
import io.github.erzads.compliancechecker.rule.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author danilo.saita on 08/05/2021.
 */
public final class ComplianceChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComplianceChecker.class);

    public static void check(String basePackage) {
        List<Rule> rules = setupRules(basePackage);
        if (rules == null || rules.isEmpty()) {
            LOGGER.info("No rule was found.");
            return;
        }
        boolean noError = true;
        for (Rule rule : rules) {
            noError = noError && rule.isCompliant();
        }
        if (noError) {
            LOGGER.info("All validations succeeded.");
        } else {
            throw new ComplianceCheckerValidationException("Validation errors! Check your logs for more information.");
        }
    }

    private static List<Rule> setupRules(String basePackage) {
        Path path;
        try {
            URL resource = ComplianceChecker.class.getResource("/compliance-checker.yaml");
            if (resource != null) {
                path = Paths.get(resource.toURI());
                byte[] bytes;
                try {
                    bytes = Files.readAllBytes(path);
                } catch (IOException e) {
                    throw new ComplianceCheckerException("Could not read content from file compliance-checker.config", e);
                }
                Yaml yaml = new Yaml();
                List<Map<String, List<String>>> fileContent = yaml.load(new ByteArrayInputStream(bytes));
                return processConfigFile(fileContent, basePackage);
            }
            LOGGER.warn("Could not find the configuration file: compliance-checker.config");
        } catch (URISyntaxException e) {
            LOGGER.warn("Could not find the configuration file: compliance-checker.config");
        }
        return Collections.emptyList();
    }

    private static List<Rule> processConfigFile(List<Map<String, List<String>>> fileContent, String basePackage) {
        List<Rule> rules = new ArrayList<>();
        for (Map<String, List<String>> ruleConfig : fileContent) {
            for (String key : ruleConfig.keySet()) {
                List<String> ruleParams = ruleConfig.get(key);
                ruleParams.add(0, basePackage);
                rules.add(createRule(key, ruleParams));
            }
        }
        return rules;
    }

    private static Rule createRule(String ruleName, List<String> ruleParams) {
        try {
            Class<?> ruleClass = ComplianceChecker.class.getClassLoader().loadClass("io.github.erzads.compliancechecker.rule." + ruleName);
            try {
                Rule rule = (Rule) ruleClass.getDeclaredConstructors()[0].newInstance();
                return rule.setUp(ruleParams);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new ComplianceCheckerException("Error configuring rules for rule name: " + ruleName + " and params: " + ruleParams, e);
            }
        } catch (ClassNotFoundException e) {
            throw new ComplianceCheckerException("Error configuring rules for rule name: " + ruleName, e);
        }
    }

}
