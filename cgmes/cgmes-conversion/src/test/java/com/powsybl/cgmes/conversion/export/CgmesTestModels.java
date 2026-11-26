/**
 * Copyright (c) 2026, Elia Group (http://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.NetworkSerDe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_REGULATING_CONTROL;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CGMES models for the tests of the SSH export, built from networks in code.
 *
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class CgmesTestModels {

    public static final String SHARED_CONTROL_GENERATOR = EurostagTutorialExample1Factory.GENERATOR_ID;
    public static final String SHARED_CONTROL_SHUNT = "SHUNT1";
    public static final String SHARED_CONTROL_SHUNT_2 = "SHUNT2";

    /**
     * The state variables of the IIDM data: the update of a partial SSH does not keep the state variables of the network.
     */
    private static final Pattern STATE_VARIABLE = Pattern.compile(" (p|q|p1|q1|p2|q2|p3|q3|v|angle|solvedTapPosition|solvedSectionCount)=\"[^\"]*\"");

    /**
     * A bus of the bus view of a node-breaker voltage level: the IIDM data contains it only with its state variables.
     */
    private static final Pattern CALCULATED_BUS = Pattern.compile("\\s*<iidm:bus nodes=\"[^\"]*\"/>");

    /**
     * The SSH update recomputes the maximum active power of an HVDC line (issue 4111).
     */
    private static final Pattern HVDC_LINE_MAX_P = Pattern.compile("(<iidm:hvdcLine .*?) maxP=\"[^\"]*\"");

    private CgmesTestModels() {
    }

    /**
     * The CGMES files of the network, in memory.
     */
    public static MemDataSource cgmesFiles(Network network) {
        MemDataSource dataSource = new MemDataSource();
        network.write("CGMES", null, dataSource);
        return dataSource;
    }

    /**
     * The network exported to CGMES and imported again, so that it has the CGMES properties and aliases of an import.
     */
    public static Network cgmesRoundTrip(Network network) {
        return Network.read(cgmesFiles(network));
    }

    /**
     * The CGMES files of Eurostag with two shunt compensators at the bus of the generator.
     * In the EQ, the two shunt compensators refer to the regulating control of the generator,
     * so the generator and the shunt compensators share one regulating control.
     */
    public static MemDataSource sharedControlFiles() {
        Network base = EurostagTutorialExample1Factory.create();
        VoltageLevel vlgen = base.getVoltageLevel(EurostagTutorialExample1Factory.VLGEN);
        for (String shuntId : List.of(SHARED_CONTROL_SHUNT, SHARED_CONTROL_SHUNT_2)) {
            vlgen.newShuntCompensator()
                .setId(shuntId)
                .setBus(EurostagTutorialExample1Factory.NGEN)
                .setConnectableBus(EurostagTutorialExample1Factory.NGEN)
                .setSectionCount(1)
                .newVoltageRegulation()
                    .withMode(RegulationMode.VOLTAGE)
                    .withRegulating(true)
                    .withTargetDeadband(0.5)
                    .add()
                .setLocalTargetV(24.5)
                .newLinearModel()
                    .setMaximumSectionCount(1)
                    .setBPerSection(1e-5)
                    .add()
                .add();
        }
        MemDataSource dataSource = cgmesFiles(base);
        try {
            String eqName = dataSource.listNames(".*_EQ.*").iterator().next();
            String eq;
            try (InputStream is = dataSource.newInputStream(eqName)) {
                eq = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
            Matcher generatorControl = regulatingControlReference(SHARED_CONTROL_GENERATOR).matcher(eq);
            assertTrue(generatorControl.find());
            String controlId = generatorControl.group(3);
            for (String shuntId : List.of(SHARED_CONTROL_SHUNT, SHARED_CONTROL_SHUNT_2)) {
                Matcher shuntControl = regulatingControlReference(shuntId).matcher(eq);
                assertTrue(shuntControl.find());
                eq = shuntControl.replaceFirst(Matcher.quoteReplacement(shuntControl.group(1) + controlId + shuntControl.group(4)));
            }
            try (OutputStream os = dataSource.newOutputStream(eqName, false)) {
                os.write(eq.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return dataSource;
    }

    /**
     * The network of {@link #sharedControlFiles()}: the generator and the two shunt compensators share one regulating control.
     */
    public static Network withSharedControl() {
        Network network = Network.read(sharedControlFiles());
        String sharedControl = sharedControl(network);
        assertNotNull(sharedControl);
        assertEquals(sharedControl, network.getShuntCompensator(SHARED_CONTROL_SHUNT).getProperty(PROPERTY_REGULATING_CONTROL));
        assertEquals(sharedControl, network.getShuntCompensator(SHARED_CONTROL_SHUNT_2).getProperty(PROPERTY_REGULATING_CONTROL));
        return network;
    }

    /**
     * The id of the regulating control that the generator and the shunt compensators of {@link #withSharedControl()} share.
     */
    public static String sharedControl(Network network) {
        return network.getGenerator(SHARED_CONTROL_GENERATOR).getProperty(PROPERTY_REGULATING_CONTROL);
    }

    /**
     * The XIIDM of the network, without the state variables and without the data that the SSH update recomputes.
     * Two networks with the same SSH data and the same equipment data have the same IIDM data.
     */
    public static String iidmData(Network network) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        NetworkSerDe.write(network, os);
        StringBuilder data = new StringBuilder();
        for (String line : os.toString(StandardCharsets.UTF_8).split("\\R")) {
            String withoutStateVariables = STATE_VARIABLE.matcher(line).replaceAll("");
            if (!CALCULATED_BUS.matcher(withoutStateVariables).matches()) {
                data.append(HVDC_LINE_MAX_P.matcher(withoutStateVariables).replaceAll("$1")).append('\n');
            }
        }
        return data.toString();
    }

    /**
     * The reference from a regulating conducting equipment of the EQ to its regulating control: group 3 is the id of the control.
     */
    private static Pattern regulatingControlReference(String holderId) {
        return Pattern.compile("(<cim:(\\w+) rdf:ID=\"_?" + Pattern.quote(holderId)
            + "\">.*?<cim:RegulatingCondEq.RegulatingControl rdf:resource=\"#_?)([^\"]+)(\".*?</cim:\\2>)", Pattern.DOTALL);
    }
}
