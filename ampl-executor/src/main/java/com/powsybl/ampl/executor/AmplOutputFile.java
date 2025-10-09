/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.executor;

import com.powsybl.ampl.converter.AmplSubset;
import com.powsybl.commons.util.StringToIntMapper;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * An additional file to read as output of an Ampl run.
 *
 * @author Nicolas PIERRE {@literal <nicolas.pierre@artelys.com>}
 */
public interface AmplOutputFile {
    /**
     * Name of the file to read
     */
    String getFileName();

    /**
     * if <code>true</code> and the file is missing, then ampl executor will throw.
     */
    boolean throwOnMissingFile();

    /**
     * Read data from a file though a {@link BufferedReader}.
     * Will be called if the Ampl solve is successful.
     *
     * @param reader            reader to the file, opened in UTF-8
     * @param networkAmplMapper mapper to convert {@link com.powsybl.iidm.network.Network} IDs to Ampl IDs
     *                          and vice versa
     */
    void read(BufferedReader reader, StringToIntMapper<AmplSubset> networkAmplMapper) throws IOException;
}
