/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.mpi;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CsvMpiStatisticsFactory implements MpiStatisticsFactory {

    @Override
    public MpiStatistics create(Path dbDir, String dbName) {
        if (dbDir == null || dbName == null) {
            return new NoMpiStatistics();
        }
        try {
            return new CsvMpiStatistics(dbDir, dbName);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
