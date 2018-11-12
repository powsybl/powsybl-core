/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import java.io.InputStream;

/**
 * Used for distributed computations of action-simulator:
 * in charge of reading results from output files, and then merging them.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public interface ResultMerger {

    /**
     * Reads results from the underlying input stream.
     * If relevant for the type of result, it is assumed to have been written in JSON format.
     */
    void readResult(InputStream is);

    /**
     * Merge the list of results read before.
     */
    void mergeResults();
}
