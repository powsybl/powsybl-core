/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.measurements;

import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.export.Exporters;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Properties;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class MyTest {

    @Test
    public void test() {
        Properties parameters = new Properties();
        parameters.put("iidm.import.cgmes.post-processors", Collections.singletonList("measurements"));
        Network network = Importers.loadNetwork(Paths.get("/home/ralambotianamio/data/cim/RTE_France_22-March-2021_EQ.xml"),
                LocalComputationManager.getDefault(), ImportConfig.load(), parameters);
        Exporters.export("XIIDM", network, null, Paths.get("/home/ralambotianamio/tmp/cim_March_3.xml"));
    }
}
