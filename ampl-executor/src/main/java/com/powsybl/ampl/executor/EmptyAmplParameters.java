package com.powsybl.ampl.executor;

import java.util.Collection;
import java.util.Collections;

public class EmptyAmplParameters implements IAmplParameters {

    @Override
    public Collection<IAmplInputFile> getInputParameters() {
        return Collections.emptyList();
    }

    @Override
    public Collection<IAmplOutputFile> getOutputParameters() {
        return Collections.emptyList();
    }
}
