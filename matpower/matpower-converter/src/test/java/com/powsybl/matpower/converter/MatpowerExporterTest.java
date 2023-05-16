/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.matpower.model.MatpowerModel;
import com.powsybl.matpower.model.MatpowerReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class MatpowerExporterTest {

    private static void exportToMatAndCompareTo(Network network, String refJsonFile) throws IOException {
        MemDataSource dataSource = new MemDataSource();
        new MatpowerExporter().export(network, null, dataSource);
        byte[] mat = dataSource.getData(null, "mat");
        MatpowerModel model = MatpowerReader.read(new ByteArrayInputStream(mat), network.getId());
        String json = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(model);
        assertEquals(new String(ByteStreams.toByteArray(Objects.requireNonNull(MatpowerExporterTest.class.getResourceAsStream(refJsonFile))), StandardCharsets.UTF_8),
                json);
    }

    @Test
    void testEsgTutu1() throws IOException {
        var network = EurostagTutorialExample1Factory.create();
        exportToMatAndCompareTo(network, "/sim1.json");
    }

    @Test
    void testMicroGridBe() throws IOException {
        Network network = Network.read(CgmesConformity1ModifiedCatalog.microGridBaseCaseBERatioPhaseTapChangerTabular().dataSource());
        exportToMatAndCompareTo(network, "/be.json");
    }

    @Test
    void testWithTieLines() throws IOException {
        var network = EurostagTutorialExample1Factory.createWithTieLine();
        exportToMatAndCompareTo(network, "/sim1-with-tie-lines.json");
    }

    @Test
    void testWithHvdcLines() throws IOException {
        var network = FourSubstationsNodeBreakerFactory.create();
        exportToMatAndCompareTo(network, "/fourSubstationFactory.json");
    }
}
