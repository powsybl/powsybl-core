package com.powsybl.ampl.executor;

import java.util.Collection;

/**
 * Parameters to interface extension to the defaults Ampl model files needed.
 */
public interface IAmplParameters {
    /**
     * Collection of input files to add before launching an Ampl solve.
     */
    Collection<IAmplInputFile> getInputParameters();

    /**
     * Collection of output files to read after an Ampl solve.
     */
    Collection<IAmplOutputFile> getOutputParameters();
}
