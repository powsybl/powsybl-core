package com.powsybl.hybrid.security.analysis.parameters;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.security.SecurityAnalysisParameters;

/**
 * This class contains configuration parameters specific to
 * hybrid-mode security analysis.
 * @author Riad Benradi {@literal <riad.benradi at rte-france.com>}
 */

public class HybridModeParametersExtension extends AbstractExtension<SecurityAnalysisParameters> {

    public static final String NAME = "hybrid-mode-analysis";

    /**
     * The name of the security analysis provider to use for the first pass.
     */
    private String firstProviderName;

    /**
     * The name of the security analysis provider to use for the second pass.
     */
    private String secondProviderName;

    public String getFirstProviderName() {
        return firstProviderName;
    }

    public void setFirstProviderName(String firstProviderName) {
        this.firstProviderName = firstProviderName;
    }

    public String getSecondProviderName() {
        return secondProviderName;
    }

    public void setSecondProviderName(String secondProviderName) {
        this.secondProviderName = secondProviderName;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
