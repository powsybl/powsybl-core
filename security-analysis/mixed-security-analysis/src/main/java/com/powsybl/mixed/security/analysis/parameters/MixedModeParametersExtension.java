package com.powsybl.mixed.security.analysis.parameters;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.security.SecurityAnalysisParameters;

import java.util.List;

/**
 * This class contains configuration parameters specific to
 * mixed-mode security analysis.
 * It is designed to be used as an extension of PowSybl's standard parameters.
 * PowSybl will handle reading a configuration file (e.g., YAML) and populate
 * the fields of this class automatically.
 */
public class MixedModeParametersExtension extends AbstractExtension<SecurityAnalysisParameters> {

    public static final String NAME = "mixed-mode-analysis";
    /**
     * The name of the static simulator to use for the first pass.
     */
    private String staticSimulator;

    /**
     * The name of the dynamic simulator to use for the second pass (for complex cases).
     */
    private String dynamicSimulator;

    /**
     * The list of criteria that trigger a switch to the dynamic simulator.
     */
    private List<String> switchCriteria;

    public String getStaticSimulator() {
        return staticSimulator;
    }

    public void setStaticSimulator(String staticSimulator) {
        this.staticSimulator = staticSimulator;
    }

    public String getDynamicSimulator() {
        return dynamicSimulator;
    }

    public void setDynamicSimulator(String dynamicSimulator) {
        this.dynamicSimulator = dynamicSimulator;
    }

    public List<String> getSwitchCriteria() {
        return switchCriteria;
    }

    public void setSwitchCriteria(List<String> switchCriteria) {
        this.switchCriteria = switchCriteria;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
