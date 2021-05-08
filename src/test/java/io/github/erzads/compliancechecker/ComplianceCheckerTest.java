package io.github.erzads.compliancechecker;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Test;

/**
 * @author danilo.saita on 08/05/2021.
 */
@Ignore
class ComplianceCheckerTest {

    @Test
    void test(){
        ComplianceChecker.check("io.github.erzads");
    }

}