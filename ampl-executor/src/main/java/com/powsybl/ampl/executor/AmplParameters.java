/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.executor;

import com.powsybl.ampl.converter.AmplExportConfig;

import java.util.Collection;

/**
 * Parameters to interface extension to the defaults Ampl model files needed.
 */
public interface AmplParameters {
    /**
     * Collection of input files to add before launching an Ampl solve.
     */
    Collection<AmplInputFile> getInputParameters();

    /**
     * Collection of output files to read after an Ampl solve.
     * All the files will be read, even if some throws IOExceptions while parsing.
     *
     * @param hasConverged boolean indicating if the model has converged.
     * @see AmplModel#checkModelConvergence
     */
    Collection<AmplOutputFile> getOutputParameters(boolean hasConverged);

    /**
     * Check if run is done in debug mode. In debug mode, AMPL temporary files are not deleted.
     *
     * @return true if debug mode is on, else otherwise
     */
    boolean isDebug();

    /**
     * Get the directory where execution files will be dumped
     *
     * @return the debug directory
     */
    String getDebugDir();

    /**
     * Configuration for AmplExporter
     */
    AmplExportConfig getAmplExportConfig();
}
