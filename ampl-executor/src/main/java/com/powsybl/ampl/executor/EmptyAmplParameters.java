package com.powsybl.ampl.executor;

import java.util.Collection;
import java.util.Collections;

public class EmptyAmplParameters implements AmplParameters {

    @Override
    public Collection<AmplInputFile> getInputParameters() {
        return Collections.emptyList();
    }

    @Override
    public Collection<AmplOutputFile> getOutputParameters() {
        return Collections.emptyList();
    }
}
