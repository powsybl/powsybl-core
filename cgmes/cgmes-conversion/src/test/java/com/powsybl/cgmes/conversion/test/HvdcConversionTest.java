/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.test.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.triplestore.api.TripleStoreFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class HvdcConversionTest {

    @Test
    public void smallNodeBreakerHvdc() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1Catalog.smallNodeBreakerHvdc(), config);

        assertEquals(4, n.getHvdcConverterStationCount());
        assertEquals(2, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "_7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "_9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsHvdcLine(n, "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "_7393a68e-c4e6-48dd-9347-543858363fdb", "_9793118d-5ba1-4a9c-b2e0-db1d15be5913", 12.3, 63.8, 76.55999999999999));

        assertTrue(containsVscConverter(n, "_b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32362458, 218.47, 0.0));
        assertTrue(containsVscConverter(n, "_b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32467532, 213.54, 0.0));
        assertTrue(containsHvdcLine(n, "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine2",
            "_b46bfb8e-7af6-459e-acf3-53a42c943a7c", "_b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 154.0, 184.2));
    }

    @Test
    public void smallNodeBreakerHvdcDcLine2BothConvertersTargetPpcc1inverter2rectifier() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcDcLine2BothConvertersTargetPpcc1inverter2rectifier(), config);

        assertEquals(4, n.getHvdcConverterStationCount());
        assertEquals(2, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "_7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "_9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsHvdcLine(n, "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "_7393a68e-c4e6-48dd-9347-543858363fdb", "_9793118d-5ba1-4a9c-b2e0-db1d15be5913", 12.3, 63.8, 76.55999999999999));

        assertTrue(containsVscConverter(n, "_b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.49261084, 218.47, 0.0));
        assertTrue(containsVscConverter(n, "_b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.49019608, 213.54, 0.0));
        assertTrue(containsHvdcLine(n, "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, "dcLine2",
            "_b46bfb8e-7af6-459e-acf3-53a42c943a7c", "_b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 101.5, 122.39999999999999));
    }

    @Test
    public void smallNodeBreakerHvdcDcLine2BothConvertersTargetPpcc1rectifier2inverter() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcDcLine2BothConvertersTargetPpcc1rectifier2inverter(), config);

        assertEquals(4, n.getHvdcConverterStationCount());
        assertEquals(2, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "_7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "_9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsHvdcLine(n, "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "_7393a68e-c4e6-48dd-9347-543858363fdb", "_9793118d-5ba1-4a9c-b2e0-db1d15be5913", 12.3, 63.8, 76.55999999999999));

        assertTrue(containsVscConverter(n, "_b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.5, 218.47, 0.0));
        assertTrue(containsVscConverter(n, "_b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.5025126, 213.54, 0.0));
        assertTrue(containsHvdcLine(n, "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine2",
            "_b46bfb8e-7af6-459e-acf3-53a42c943a7c", "_b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 99.5, 120.0));
    }

    @Test
    public void smallNodeBreakerHvdcDcLine2Inverter1Rectifier2() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcDcLine2Inverter1Rectifier2(), config);

        assertEquals(4, n.getHvdcConverterStationCount());
        assertEquals(2, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "_7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "_9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsHvdcLine(n, "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "_7393a68e-c4e6-48dd-9347-543858363fdb", "_9793118d-5ba1-4a9c-b2e0-db1d15be5913", 12.3, 63.8, 76.55999999999999));

        assertTrue(containsVscConverter(n, "_b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.49751243, 218.47, 0.0));
        assertTrue(containsVscConverter(n, "_b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.4950495, 213.54, 0.0));
        assertTrue(containsHvdcLine(n, "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, "dcLine2",
            "_b46bfb8e-7af6-459e-acf3-53a42c943a7c", "_b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 100.5, 120.0));
    }

    @Test
    public void smallNodeBreakerHvdcVscReactiveQPcc() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcVscReactiveQPcc(), config);

        assertEquals(4, n.getHvdcConverterStationCount());
        assertEquals(2, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "_7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "_9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsHvdcLine(n, "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "_7393a68e-c4e6-48dd-9347-543858363fdb", "_9793118d-5ba1-4a9c-b2e0-db1d15be5913", 12.3, 63.8, 76.55999999999999));

        assertTrue(containsVscConverter(n, "_b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32362458, 0.0, 0.0));
        assertTrue(containsVscConverter(n, "_b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32467532, 0.0, 0.0));
        assertTrue(containsHvdcLine(n, "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine2",
            "_b46bfb8e-7af6-459e-acf3-53a42c943a7c", "_b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 154.0, 184.2));
    }

    @Test
    public void smallNodeBrokerHvdcMissingDCLineSegment() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBrokerHvdcMissingDCLineSegment(), config);

        assertEquals(0, n.getHvdcConverterStationCount());
        assertEquals(0, n.getHvdcLineCount());
    }

    @Test
    public void smallNodeBrokerHvdcMissingAcDcConverters() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBrokerHvdcMissingAcDcConverters(), config);

        assertEquals(0, n.getHvdcConverterStationCount());
        assertEquals(0, n.getHvdcLineCount());
    }

    @Test
    public void smallNodeBrokerHvdcNanTargetPpcc() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBrokerHvdcNanTargetPpcc(), config);

        assertEquals(4, n.getHvdcConverterStationCount());
        assertEquals(2, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "_7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "_9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsHvdcLine(n, "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "_7393a68e-c4e6-48dd-9347-543858363fdb", "_9793118d-5ba1-4a9c-b2e0-db1d15be5913", 12.3, 0.0, 0.0));

        assertTrue(containsVscConverter(n, "_b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.0, 218.47, 0.0));
        assertTrue(containsVscConverter(n, "_b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.0, 213.54, 0.0));
        assertTrue(containsHvdcLine(n, "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, "dcLine2",
            "_b46bfb8e-7af6-459e-acf3-53a42c943a7c", "_b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 0.0, 0.0));
    }

    @Test
    public void smallNodeBrokerHvdcTwoDcLineSegments() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBrokerHvdcTwoDcLineSegments(), config);

        assertEquals(4, n.getHvdcConverterStationCount());
        assertEquals(2, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "_7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "_9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsHvdcLine(n, "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "_7393a68e-c4e6-48dd-9347-543858363fdb", "_9793118d-5ba1-4a9c-b2e0-db1d15be5913", 5.60575, 63.8, 76.55999999999999));

        assertTrue(containsVscConverter(n, "_b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32362458, 218.47, 0.0));
        assertTrue(containsVscConverter(n, "_b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32467532, 213.54, 0.0));
        assertTrue(containsHvdcLine(n, "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine2",
            "_b46bfb8e-7af6-459e-acf3-53a42c943a7c", "_b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 154.0, 184.2));
    }

    @Test
    public void smallNodeBrokerHvdcTwoAcDcConvertersOneDcLineSegments() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBrokerHvdcTwoAcDcConvertersOneDcLineSegments(), config);

        assertEquals(6, n.getHvdcConverterStationCount());
        assertEquals(3, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "_7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "_7393a68f-c4e6-48dd-9347-543858363fdb", "Conv1b", "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0#0", 0.0, 0.8));

        assertTrue(containsLccConverter(n, "_9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsLccConverter(n, "_9793118e-5ba1-4a9c-b2e0-db1d15be5913", "Conv2b", "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0#0", 0.0, -0.75741));

        assertTrue(containsHvdcLine(n, "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "_7393a68e-c4e6-48dd-9347-543858363fdb", "_9793118d-5ba1-4a9c-b2e0-db1d15be5913", 12.3, 63.8, 76.55999999999999));
        assertTrue(containsHvdcLine(n, "_11d10c55-94cc-47e4-8e24-bc5ac4d026c0#0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "_7393a68f-c4e6-48dd-9347-543858363fdb", "_9793118e-5ba1-4a9c-b2e0-db1d15be5913", 12.3, 63.8, 76.55999999999999));

        assertTrue(containsVscConverter(n, "_b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32362458, 218.47, 0.0));
        assertTrue(containsVscConverter(n, "_b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32467532, 213.54, 0.0));
        assertTrue(containsHvdcLine(n, "_d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine2",
            "_b46bfb8e-7af6-459e-acf3-53a42c943a7c", "_b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 154.0, 184.2));
    }

    private Network networkModel(TestGridModel testGridModel, Conversion.Config config) throws IOException {

        ReadOnlyDataSource ds = testGridModel.dataSource();
        String impl = TripleStoreFactory.defaultImplementation();

        CgmesModel cgmes = CgmesModelFactory.create(ds, impl);

        config.setConvertSvInjections(true);
        config.setProfileUsedForInitialStateValues(Conversion.Config.StateProfile.SSH.name());
        Conversion c = new Conversion(cgmes, config);
        Network n = c.convert();

        return n;
    }

    private boolean containsLccConverter(Network n, String id, String name,
        String hvdcLineId, double lossFactor, double powerFactor) {
        ReferenceHvdcConverter referenceHvdcConverter = new ReferenceHvdcConverter(id, HvdcConverterStation.HvdcType.LCC, name,
            hvdcLineId, lossFactor, powerFactor);
        return containsLccConverter(n, referenceHvdcConverter);
    }

    private boolean containsLccConverter(Network n, ReferenceHvdcConverter referenceConverter) {
        LccConverterStation lccConverter = n.getLccConverterStation(referenceConverter.id);
        if (lccConverter == null) {
            LOG.info("HvdcConverterStation {} not found", referenceConverter.id);
            return false;
        }
        if (referenceConverter.hvdcType != lccConverter.getHvdcType()) {
            LOG.info("Different hvdcType {} reference {}", lccConverter.getHvdcType(), referenceConverter.hvdcType);
            return false;
        }
        if (referenceConverter.name.compareTo(lccConverter.getNameOrId()) != 0) {
            LOG.info("Different name {} reference {}", lccConverter.getNameOrId(), referenceConverter.name);
            return false;
        }
        if (referenceConverter.hvdcLineId.compareTo(lccConverter.getHvdcLine().getId()) != 0) {
            LOG.info("Different hvdcLineId {} reference {}", lccConverter.getHvdcLine().getId(), referenceConverter.hvdcLineId);
            return false;
        }
        double tolerance = 0.0001;
        if (Math.abs(referenceConverter.lossFactor - lccConverter.getLossFactor()) > tolerance) {
            LOG.info("Different lossFactor {} reference {}", lccConverter.getLossFactor(), referenceConverter.lossFactor);
            return false;
        }
        if (Math.abs(referenceConverter.powerFactor - lccConverter.getPowerFactor()) > tolerance) {
            LOG.info("Different powerFactor {} reference {}", lccConverter.getPowerFactor(), referenceConverter.powerFactor);
            return false;
        }
        return true;
    }

    private boolean containsVscConverter(Network n, String id, String name,
        String hvdcLineId, double lossFactor, double voltageSetPoint, double reactivePowerSetPoint) {
        ReferenceHvdcConverter referenceHvdcConverter = new ReferenceHvdcConverter(id, HvdcConverterStation.HvdcType.VSC, name,
            hvdcLineId, lossFactor, voltageSetPoint, reactivePowerSetPoint);
        return containsVscConverter(n, referenceHvdcConverter);
    }

    private boolean containsVscConverter(Network n, ReferenceHvdcConverter referenceConverter) {
        VscConverterStation vscConverter = n.getVscConverterStation(referenceConverter.id);
        if (vscConverter == null) {
            LOG.info("HvdcConverterStation {} not found", referenceConverter.id);
            return false;
        }
        if (referenceConverter.hvdcType != vscConverter.getHvdcType()) {
            LOG.info("Different hvdcType {} reference {}", vscConverter.getHvdcType(), referenceConverter.hvdcType);
            return false;
        }
        if (referenceConverter.name.compareTo(vscConverter.getNameOrId()) != 0) {
            LOG.info("Different name {} reference {}", vscConverter.getNameOrId(), referenceConverter.name);
            return false;
        }
        if (referenceConverter.hvdcLineId.compareTo(vscConverter.getHvdcLine().getId()) != 0) {
            LOG.info("Different hvdcLineId {} reference {}", vscConverter.getHvdcLine().getId(), referenceConverter.hvdcLineId);
            return false;
        }
        double tolerance = 0.0001;
        if (Math.abs(referenceConverter.lossFactor - vscConverter.getLossFactor()) > tolerance) {
            LOG.info("Different lossFactor {} reference {}", vscConverter.getLossFactor(), referenceConverter.lossFactor);
            return false;
        }
        if (Math.abs(referenceConverter.voltageSetPoint - vscConverter.getVoltageSetpoint()) > tolerance) {
            LOG.info("Different voltageSetPoint {} reference {}", vscConverter.getVoltageSetpoint(), referenceConverter.voltageSetPoint);
            return false;
        }
        if (Math.abs(referenceConverter.reactivePowerSetPoint - vscConverter.getReactivePowerSetpoint()) > tolerance) {
            LOG.info("Different reactivePowerSetPoint {} reference {}", vscConverter.getReactivePowerSetpoint(), referenceConverter.reactivePowerSetPoint);
            return false;
        }
        return true;
    }

    private boolean containsHvdcLine(Network n, String id, HvdcLine.ConvertersMode convertersMode, String name,
        String converterStation1, String converterStation2, double r, double activePowerSetPoint, double maxP) {
        ReferenceHvdcLine referenceHvdcLine = new ReferenceHvdcLine(id, convertersMode, name,
            converterStation1, converterStation2, r, activePowerSetPoint, maxP);
        return containsHvdcLine(n, referenceHvdcLine);
    }

    private boolean containsHvdcLine(Network n, ReferenceHvdcLine referenceHvdcLine) {
        HvdcLine hvdcLine = n.getHvdcLine(referenceHvdcLine.id);
        if (hvdcLine == null) {
            LOG.info("HvdcLine {} not found", referenceHvdcLine.id);
            return false;
        }
        if (referenceHvdcLine.convertersMode != hvdcLine.getConvertersMode()) {
            LOG.info("Different hvdcType {} reference {}", hvdcLine.getConvertersMode(), referenceHvdcLine.convertersMode);
            return false;
        }
        if (referenceHvdcLine.name.compareTo(hvdcLine.getNameOrId()) != 0) {
            LOG.info("Different name {} reference {}", hvdcLine.getNameOrId(), referenceHvdcLine.name);
            return false;
        }
        if (referenceHvdcLine.converterStation1.compareTo(hvdcLine.getConverterStation1().getId()) != 0) {
            LOG.info("Different converterStation1 {} reference {}", hvdcLine.getConverterStation1().getId(), referenceHvdcLine.converterStation1);
            return false;
        }
        if (referenceHvdcLine.converterStation2.compareTo(hvdcLine.getConverterStation2().getId()) != 0) {
            LOG.info("Different converterStation2 {} reference {}", hvdcLine.getConverterStation2().getId(), referenceHvdcLine.converterStation2);
            return false;
        }
        double tolerance = 0.0001;
        if (Math.abs(referenceHvdcLine.r - hvdcLine.getR()) > tolerance) {
            LOG.info("Different R {} reference {}", hvdcLine.getR(), referenceHvdcLine.r);
            return false;
        }
        if (Math.abs(referenceHvdcLine.activePowerSetPoint - hvdcLine.getActivePowerSetpoint()) > tolerance) {
            LOG.info("Different activePowerSetPoint {} reference {}", hvdcLine.getActivePowerSetpoint(), referenceHvdcLine.activePowerSetPoint);
            return false;
        }
        if (Math.abs(referenceHvdcLine.maxP - hvdcLine.getMaxP()) > tolerance) {
            LOG.info("Different maxP {} reference {}", hvdcLine.getMaxP(), referenceHvdcLine.maxP);
            return false;
        }
        return true;
    }

    private void logHvdcConverterStations(Network n) {
        n.getHvdcConverterStationStream().forEach(c -> logHvdcConverterStation(n, c));
    }

    private void logHvdcConverterStation(Network n, HvdcConverterStation<?> hvdcConverterStation) {
        LOG.info("HvdcConverterStation");
        LOG.info("Id: {}", hvdcConverterStation.getId());
        LOG.info("HvdcType: {}", hvdcConverterStation.getHvdcType());
        LOG.info("Name: {}", hvdcConverterStation.getNameOrId());
        LOG.info("HvdcLine Id: {}", hvdcConverterStation.getHvdcLine().getId());
        LOG.info("LossFactor: {}", hvdcConverterStation.getLossFactor());
        if (hvdcConverterStation.getHvdcType() == HvdcConverterStation.HvdcType.LCC) {
            LccConverterStation lccConverterStation = n.getLccConverterStation(hvdcConverterStation.getId());
            LOG.info("PowerFactor: {}", lccConverterStation.getPowerFactor());
        } else if (hvdcConverterStation.getHvdcType() == HvdcConverterStation.HvdcType.VSC) {
            VscConverterStation vscConverterStation = n.getVscConverterStation(hvdcConverterStation.getId());
            LOG.info("voltageSetPoint: {}", vscConverterStation.getVoltageSetpoint());
            LOG.info("reactivePowerSetPoint: {}", vscConverterStation.getReactivePowerSetpoint());
        }
    }

    private void logHvdcLines(Network n) {
        n.getHvdcLineStream().forEach(dc -> logHvdcLine(dc));
    }

    private void logHvdcLine(HvdcLine hvdcLine) {
        LOG.info("HvdcLine");
        LOG.info("Id: {}", hvdcLine.getId());
        LOG.info("convertersMode: {}", hvdcLine.getConvertersMode());
        LOG.info("Name: {}", hvdcLine.getNameOrId());
        LOG.info("converterStationId1: {}", hvdcLine.getConverterStation1().getId());
        LOG.info("converterStationId2: {}", hvdcLine.getConverterStation2().getId());
        LOG.info("R: {}", hvdcLine.getR());
        LOG.info("activePowerSetPoint: {}", hvdcLine.getActivePowerSetpoint());
        LOG.info("MaxP: {}", hvdcLine.getMaxP());
    }

    static class ReferenceHvdcConverter {
        String id;
        HvdcConverterStation.HvdcType hvdcType;
        String name;
        String hvdcLineId;
        double lossFactor = 0.0;
        double powerFactor = 0.0;
        double voltageSetPoint = 0.0;
        double reactivePowerSetPoint = 0.0;

        ReferenceHvdcConverter(String id, HvdcConverterStation.HvdcType hvdcType, String name, String hvdcLineId,
            double lossFactor, double powerFactor) {
            this.id = id;
            this.hvdcType = hvdcType;
            this.name = name;
            this.hvdcLineId = hvdcLineId;
            this.lossFactor = lossFactor;
            this.powerFactor = powerFactor;
        }

        ReferenceHvdcConverter(String id, HvdcConverterStation.HvdcType hvdcType, String name, String hvdcLineId,
            double lossFactor, double voltageSetPoint, double reactivePowerSetPoint) {
            this.id = id;
            this.hvdcType = hvdcType;
            this.name = name;
            this.hvdcLineId = hvdcLineId;
            this.lossFactor = lossFactor;
            this.voltageSetPoint = voltageSetPoint;
            this.reactivePowerSetPoint = reactivePowerSetPoint;
        }
    }

    static class ReferenceHvdcLine {
        String id;
        HvdcLine.ConvertersMode convertersMode;
        String name;
        String converterStation1;
        String converterStation2;
        double r = 0.0;
        double activePowerSetPoint = 0.0;
        double maxP = 0.0;

        ReferenceHvdcLine(String id, HvdcLine.ConvertersMode convertersMode, String name, String converterStation1,
            String converterStation2, double r, double activePowerSetPoint, double maxP) {
            this.id = id;
            this.convertersMode = convertersMode;
            this.name = name;
            this.converterStation1 = converterStation1;
            this.converterStation2 = converterStation2;
            this.r = r;
            this.activePowerSetPoint = activePowerSetPoint;
            this.maxP = maxP;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(HvdcConversionTest.class);
}
