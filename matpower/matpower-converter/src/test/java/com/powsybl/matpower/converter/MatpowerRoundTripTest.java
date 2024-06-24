/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.matpower.converter;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.DirectoryDataSource;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.matpower.model.MatpowerModel;
import com.powsybl.matpower.model.MatpowerModelFactory;
import com.powsybl.matpower.model.MatpowerWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class MatpowerRoundTripTest {

    private FileSystem fileSystem;

    private Path dir;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        dir = fileSystem.getPath("/work");
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    private static double calculateRatio(Network network, String id) {
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer(id);
        double ratio = twt.getRatedU2() / twt.getRatedU1();
        if (twt.getRatioTapChanger() != null) {
            ratio *= twt.getRatioTapChanger().getCurrentStep().getRho();
        }
        if (twt.getPhaseTapChanger() != null) {
            ratio *= twt.getPhaseTapChanger().getCurrentStep().getRho();
        }
        return ratio;
    }

    @Test
    void test() {
        Network network = EurostagTutorialExample1Factory.create();
        Properties parameters = new Properties();
        parameters.setProperty("matpower.import.ignore-base-voltage", "false");
        new MatpowerExporter().export(network, parameters, new DirectoryDataSource(dir, "test"));
        Network network2 = new MatpowerImporter().importData(new DirectoryDataSource(dir, "test"), NetworkFactory.findDefault(), parameters);
        assertEquals(calculateRatio(network, "NGEN_NHV1"), calculateRatio(network2, "TWT-1-2"), 1e-16);
        assertEquals(calculateRatio(network, "NHV2_NLOAD"), calculateRatio(network2, "TWT-3-4"), 1e-16);
    }

    @Test
    void testRoundTripDcLines() throws IOException {
        MatpowerModel matpowerModel = MatpowerModelFactory.create9Dcline();
        String caseId = matpowerModel.getCaseName();
        Path matFile = dir.resolve(caseId + ".mat");
        MatpowerWriter.write(matpowerModel, matFile, true);

        var network = new MatpowerImporter().importData(new DirectoryDataSource(dir, caseId), NetworkFactory.findDefault(), null);

        new MatpowerExporter().export(network, null, new DirectoryDataSource(dir, "test"));
        Network network1 = new MatpowerImporter().importData(new DirectoryDataSource(dir, "test"), NetworkFactory.findDefault(), null);

        assertEquals(network.getHvdcLineCount(), network1.getHvdcLineCount());
        assertTrue(network.getHvdcLineStream().allMatch(hvdcLine -> existHvdcLineInTheOtherNetworkAndIsEqual(network1, hvdcLine)));
    }

    private static boolean existHvdcLineInTheOtherNetworkAndIsEqual(Network network1, HvdcLine hvdcLine) {
        HvdcLine hvdcLine1 = network1.getHvdcLine(hvdcLine.getId());
        if (hvdcLine1 == null) {
            return false;
        }
        double tol = 0.0001;
        assertEquals(hvdcLine.getR(), hvdcLine1.getR(), tol);
        assertEquals(hvdcLine.getActivePowerSetpoint(), hvdcLine1.getActivePowerSetpoint(), tol);
        assertEquals(hvdcLine.getMaxP(), hvdcLine1.getMaxP(), tol);
        assertEquals(hvdcLine.getNominalV(), hvdcLine1.getNominalV(), tol);
        assertEquals(hvdcLine.getConvertersMode(), hvdcLine1.getConvertersMode());

        assertTrue(convertersAreEqual(hvdcLine.getConverterStation1(), hvdcLine1.getConverterStation1()));
        assertTrue(convertersAreEqual(hvdcLine.getConverterStation2(), hvdcLine1.getConverterStation2()));

        return true;
    }

    private static boolean convertersAreEqual(HvdcConverterStation<?> hvdcConverterStation, HvdcConverterStation<?> hvdcConverterStation1) {
        assertEquals(HvdcConverterStation.HvdcType.VSC, hvdcConverterStation.getHvdcType());
        assertEquals(HvdcConverterStation.HvdcType.VSC, hvdcConverterStation1.getHvdcType());

        double tol = 0.0001;
        assertEquals(hvdcConverterStation.getLossFactor(), hvdcConverterStation1.getLossFactor(), tol);

        VscConverterStation vscConverterStation = (VscConverterStation) hvdcConverterStation;
        VscConverterStation vscConverterStation1 = (VscConverterStation) hvdcConverterStation1;

        assertEquals(vscConverterStation.getVoltageSetpoint(), vscConverterStation1.getVoltageSetpoint(), tol);
        assertEquals(vscConverterStation.getReactivePowerSetpoint(), vscConverterStation1.getReactivePowerSetpoint(), tol);
        if (vscConverterStation.getReactiveLimits().getKind().equals(ReactiveLimitsKind.MIN_MAX) && vscConverterStation1.getReactiveLimits().getKind().equals(ReactiveLimitsKind.MIN_MAX)) {
            assertEquals(vscConverterStation.getReactiveLimits(MinMaxReactiveLimits.class).getMinQ(), vscConverterStation1.getReactiveLimits(MinMaxReactiveLimits.class).getMinQ(), tol);
            assertEquals(vscConverterStation.getReactiveLimits(MinMaxReactiveLimits.class).getMaxQ(), vscConverterStation1.getReactiveLimits(MinMaxReactiveLimits.class).getMaxQ(), tol);
        }
        return true;
    }
}
