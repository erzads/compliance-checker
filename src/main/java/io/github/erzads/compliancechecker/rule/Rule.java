package io.github.erzads.compliancechecker.rule;

import java.util.List;

/**
 * @author danilo.saita on 08/05/2021.
 */
public interface Rule {

    Rule setUp(List<String> ruleParams);

    boolean isCompliant();
}
