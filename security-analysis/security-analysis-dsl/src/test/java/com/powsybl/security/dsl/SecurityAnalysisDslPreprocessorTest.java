package com.powsybl.security.dsl;

import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.security.SecurityAnalysisInput;
import org.junit.Test;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class SecurityAnalysisDslPreprocessorTest {

    @Test
    public void test() {

        SecurityAnalysisInput input = new SecurityAnalysisInput(EurostagTutorialExample1Factory.create(), INITIAL_VARIANT_ID);

        ByteSource source = Resources.asByteSource(getClass().getResource("/limits-and-contincencies-dsl.groovy"));
        new SecurityAnalysisDslPreprocessor(source).preprocess(input);

        assertEquals(1, input.getContingenciesProvider().getContingencies(null).size());

        assertTrue(input.getLimitViolationDetector() instanceof LimitViolationDetectorWithFactors);
    }

}
