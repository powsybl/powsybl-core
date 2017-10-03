/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.import_.ImportersLoaderList;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
/**
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public class UcteImporterTest {
    @Test
    public void trimIssueTest() throws Exception  {
        // Import network that could fail because of id conflicts due to trim mechanism
        Importers.loadNetwork("importIssue.uct", getClass().getResourceAsStream("/importIssue.uct"),
                Mockito.mock(ComputationManager.class), new ImportConfig(), null,
                new ImportersLoaderList(Collections.singletonList(new UcteImporter()), Collections.emptyList()));
    }
}
