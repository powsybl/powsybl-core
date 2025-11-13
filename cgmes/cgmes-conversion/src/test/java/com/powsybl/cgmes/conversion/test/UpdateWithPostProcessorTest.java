/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

public class UpdateWithPostProcessorTest {

    public static final String DUMMY = "dummy";
    private static final String DIR = "/update/line/";

    @Test
    void testUpdateWithPostProcessorDoesntThrow() {
        ComputationManager computationManager = LocalComputationManager.getDefault();
        ImportConfig importConfig = new ImportConfig(DUMMY);
        ImportersLoaderList loader = new ImportersLoaderList(List.of(new CgmesImport()), List.of(new DummyPostProcessor()));
        Properties importParams = new Properties();

        ReadOnlyDataSource dsEq = new ResourceDataSource("EQ file", new ResourceSet(DIR, "line_EQ.xml"));
        Network network = Network.read(dsEq, computationManager, importConfig, importParams, NetworkFactory.findDefault(), loader, ReportNode.NO_OP);

        ReadOnlyDataSource dsSsh = new ResourceDataSource("SSH file", new ResourceSet(DIR, "line_SSH.xml"));
        Assertions.assertDoesNotThrow(() -> network.update(dsSsh, computationManager, importConfig, importParams, loader, ReportNode.NO_OP));
    }

    public static class DummyPostProcessor implements ImportPostProcessor {
        @Override
        public String getName() {
            return DUMMY;
        }

        @Override
        public void process(Network network, ComputationManager computationManager, ReportNode reportNode) {
            // Does nothing
        }
    }
}