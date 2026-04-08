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

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * An additional file provided to an Ampl run.
 * Useful when you need more information for your ampl model.
 *
 * @author Nicolas PIERRE {@literal <nicolas.pierre@artelys.com>}
 */
public interface AmplInputFile {
    /**
     * Name of the file to add
     */
    String getFileName();

    /**
     * Write data to file via a {@link BufferedWriter}. Called before the ampl solve.
     *
     * @param writer            writer encoded in UTF-8 to write data to the file
     * @param networkAmplMapper mapper to convert {@link com.powsybl.iidm.network.Network} IDs to Ampl IDs
     *                          and vice versa
     */
    void write(BufferedWriter writer, StringToIntMapper<AmplSubset> networkAmplMapper) throws IOException;
}
