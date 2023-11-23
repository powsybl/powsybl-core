/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import java.time.ZonedDateTime;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
final class TopologyTestUtils {

    static final String VOLTAGE_LEVEL_ID = "NHV1_NHV2_1_VL#0";
    static final String BBS = "bbs";
    static final String VLTEST = "VLTEST";

    static Network createNbNetworkWithBusbarSection() {
        VoltageLevel vl = createNbNetwork().getVoltageLevel(VLTEST);
        vl.getNodeBreakerView().newBusbarSection().setId("bbs").setNode(0).add();
        return vl.getNetwork();
    }

    static Network createNbNetwork() {
        Network network = FictitiousSwitchFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2021-08-27T14:44:56.567+02:00"));
        network.newVoltageLevel().setId(VLTEST).setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        return network;
    }

    static Network createNbBbNetwork() {
        Network network = createNetwork();
        VoltageLevel vl = network.newVoltageLevel().setId("NHV1_NHV2_1_VL#0").setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl.getNodeBreakerView().newBusbarSection().setId("bbs").setNode(0).add();
        return network;
    }

    static Network createBbNetwork() {
        Network network = createNetwork();
        VoltageLevel vl = network.newVoltageLevel().setId("NHV1_NHV2_1_VL#0").setNominalV(380).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        vl.getBusBreakerView().newBus().setId("bus").add();
        return network;
    }

    private static Network createNetwork() {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2021-08-27T14:44:56.567+02:00"));
        return network;
    }

    static void testReporter(ReporterModel reporter, String reporterFile) {
        Optional<Report> report = reporter.getReports().stream().findFirst();
        assertTrue(report.isPresent());

        StringWriter sw = new StringWriter();
        reporter.export(sw);

        InputStream refStream = TopologyTestUtils.class.getResourceAsStream(reporterFile);
        ComparisonUtils.compareTxt(refStream, sw.toString());
    }

    private TopologyTestUtils() {
    }
}
