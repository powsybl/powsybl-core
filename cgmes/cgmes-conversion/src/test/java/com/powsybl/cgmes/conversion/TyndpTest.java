/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author Mathieu BAGUE {@literal <mathieu.bague at rte-france.com>}
 */
public class TyndpTest {

    @Test
    public void test() throws IOException {
        Properties properties = new Properties();
        properties.put(CgmesImport.BOUNDARY_LOCATION, "/home/baguemat/Mathieu/projects/powsybl/runtime/data/cases/TYNDP_dataset_level_3/Boundary file");

        Path directory = Paths.get("/home/baguemat/Mathieu/projects/powsybl/runtime/data/cases/TYNDP_dataset_level_3/CE");
        Files.list(directory).forEach(file -> {
            System.out.println("Import " + file.toString());
            try {
                Importers.loadNetwork(file, LocalComputationManager.getDefault(), ImportConfig.load(), properties);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });

        /*
        Path directory = Paths.get("/home/baguemat/Mathieu/projects/powsybl/runtime/data/cases/TYNDP_dataset_level_3/Baltics");
        Files.list(directory).forEach(file -> {
            System.out.println("Import " + file.toString());
            try {
                Importers.loadNetwork(file, LocalComputationManager.getDefault(), ImportConfig.load(), properties);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
        */

        // Importers.loadNetwork(directory.resolve("NL_3PQT_v2.zip"), LocalComputationManager.getDefault(), ImportConfig.load(), properties);
        // Importers.loadNetwork(directory.resolve("ES_3PQT_v2.zip"), LocalComputationManager.getDefault(), ImportConfig.load(), properties);
    }
}


