/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.executor;

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
     */
    Collection<AmplOutputFile> getOutputParameters();
}
