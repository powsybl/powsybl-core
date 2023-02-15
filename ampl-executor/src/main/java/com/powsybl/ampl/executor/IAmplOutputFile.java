/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.executor;

import java.io.IOException;
import java.nio.file.Path;

/**
 * An additional file to read as output of an Ampl run.
 *
 * @author Nicolas PIERRE <nicolas.pierre@artelys.com>
 */
public interface IAmplOutputFile {
    /**
     * Name of the file to read
     */
    String getFileName();

    /**
     * Consummer of the output file.
     * Will be called if the Ampl solve is successful.
     * <p>
     * No check is done on the presence of the file.
     *
     * @param outputPath Path representing the outputfile
     */
    void read(Path outputPath) throws IOException;
}
