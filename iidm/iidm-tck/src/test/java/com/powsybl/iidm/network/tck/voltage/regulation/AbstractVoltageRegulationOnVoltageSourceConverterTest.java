/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.voltage.regulation;

import com.powsybl.iidm.network.AcDcConverter;
import com.powsybl.iidm.network.DcNode;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.VoltageSourceConverter;
import com.powsybl.iidm.network.VoltageSourceConverterAdder;
import com.powsybl.iidm.network.regulation.RegulationMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public abstract class AbstractVoltageRegulationOnVoltageSourceConverterTest extends AbstractVoltageRegulationCommon<VoltageSourceConverter> {
    private Terminal lineTerminal;

    @Override
    @BeforeEach
    void initNetwork() {
        super.initNetwork();
        lineTerminal = network.getLine("NHV1_NHV2_1").getTerminal1();
    }

    @Test
    void shouldUpdatePccTerminalAndVoltageRegulationTerminalWhenVoltageRegulationIsMissing() {
        // GIVEN
        String vscId = "vsc_setPccTerminal";
        double localTargetQ = 10.0;
        DataVoltageRegulationHolderCreator dataVoltageRegulationHolderCreator = new DataVoltageRegulationHolderCreator(vscId,
            null,
            false,
            Double.NaN,
            Double.NaN,
            localTargetQ,
            false);
        VoltageSourceConverter voltageSourceConverter = createVoltageSourceConverter(dataVoltageRegulationHolderCreator);
        // WHEN
        voltageSourceConverter.setPccTerminal(lineTerminal);
        // THEN
        assertEquals(lineTerminal, voltageSourceConverter.getPccTerminal());
        assertEquals(lineTerminal, voltageSourceConverter.getVoltageRegulation().getTerminal());
        assertFalse(voltageSourceConverter.getVoltageRegulation().isRegulating());
    }

    @Test
    void shouldUpdatePccTerminalAndVoltageRegulationTerminal() {
        // GIVEN
        String vscId = "vsc_setPccTerminal";
        double localTargetQ = 10.0;
        double localTargetV = 200.0;
        int targetValue = 110;
        DataVoltageRegulationHolderCreator dataVoltageRegulationHolderCreator = new DataVoltageRegulationHolderCreator(vscId,
            RegulationMode.VOLTAGE,
            false,
            Double.NaN,
            localTargetV,
            localTargetQ,
            true);
        VoltageSourceConverter voltageSourceConverter = createVoltageSourceConverter(dataVoltageRegulationHolderCreator);
        Terminal terminal1 = voltageSourceConverter.getTerminal1();
        // WHEN setting the local terminal on the pccTerminal
        voltageSourceConverter.setPccTerminal(terminal1);
        // THEN pccTerminal = terminal1 and voltageRegulation.terminal = null
        assertLocalPccTerminalAndVoltageRegulationTerminalNull(terminal1, voltageSourceConverter);

        // WHEN setting a remote terminal on the pccTerminal with missing targetValue
        ValidationException validationException = assertThrows(ValidationException.class, () -> voltageSourceConverter.setPccTerminal(lineTerminal));
        // THEN ValidationException is thrown
        assertEquals("AC/DC Voltage Source Converter 'vsc_setPccTerminal': Undefined value for voltageRegulation.targetValue," +
            " expected defined value when a terminal is set", validationException.getMessage());
        assertLocalPccTerminalAndVoltageRegulationTerminalNull(terminal1, voltageSourceConverter);

        // WHEN setting a remote terminal on the pccTerminal with a targetValue set
        voltageSourceConverter.getVoltageRegulation().setRegulating(false).setTargetValue(targetValue);
        voltageSourceConverter.setPccTerminal(lineTerminal);
        voltageSourceConverter.getVoltageRegulation().setRegulating(true);
        // THEN pccTerminal = remoteTerminal and voltageRegulation.terminal = remoteTerminal
        assertPccTerminalAndVoltageRegulationTerminal(lineTerminal, voltageSourceConverter, targetValue);

        // WHEN setting a new remote terminal on the pccTerminal
        Terminal newRemoteTerminal = voltageSourceConverter.getTerminal2().get();
        voltageSourceConverter.setPccTerminal(newRemoteTerminal);
        // THEN pccTerminal = remoteTerminal and voltageRegulation.terminal = remoteTerminal
        assertPccTerminalAndVoltageRegulationTerminal(newRemoteTerminal, voltageSourceConverter, targetValue);

        // WHEN setting the local terminal on the pccTerminal
        voltageSourceConverter.setPccTerminal(voltageSourceConverter.getTerminal1());
        // THEN pccTerminal = terminal1 and voltageRegulation.terminal = null
        assertLocalPccTerminalAndVoltageRegulationTerminalNull(terminal1, voltageSourceConverter);

        // WHEN setting the local terminal (null) on the voltageRegulation terminal
        voltageSourceConverter.getVoltageRegulation().setTerminal(null, Double.NaN);
        // THEN pccTerminal = terminal1 and voltageRegulation.terminal = null
        assertLocalPccTerminalAndVoltageRegulationTerminalNull(terminal1, voltageSourceConverter);

        // WHEN setting the remote terminal on the voltageRegulation terminal
        voltageSourceConverter.getVoltageRegulation().setTerminal(lineTerminal, targetValue);
        // THEN pccTerminal = remoteTerminal and voltageRegulation.terminal = remoteTerminal
        assertPccTerminalAndVoltageRegulationTerminal(lineTerminal, voltageSourceConverter, targetValue);

        // WHEN setting the new remote terminal on the voltageRegulation terminal
        voltageSourceConverter.getVoltageRegulation().setTerminal(newRemoteTerminal, targetValue);
        // THEN pccTerminal = newRemoteTerminal and voltageRegulation.terminal = newRemoteTerminal
        assertPccTerminalAndVoltageRegulationTerminal(newRemoteTerminal, voltageSourceConverter, targetValue);

        // WHEN setting the explicit local terminal on the voltageRegulation terminal
        voltageSourceConverter.getVoltageRegulation().setTerminal(terminal1, targetValue);
        // THEN pccTerminal = terminal1 and voltageRegulation.terminal = terminal1
        assertPccTerminalAndVoltageRegulationTerminal(terminal1, voltageSourceConverter, targetValue);

        // WHEN setting the local terminal (null) on the voltageRegulation terminal
        voltageSourceConverter.getVoltageRegulation().setTerminal(null, Double.NaN);
        // THEN pccTerminal = terminal1 and voltageRegulation.terminal = null
        assertLocalPccTerminalAndVoltageRegulationTerminalNull(terminal1, voltageSourceConverter);
    }

    @Test
    void shouldThrowExceptionWhenUsingTheVoltageSourceConverterAdderWithTwoDifferentTerminals() {
        // GIVEN
        Terminal line2Terminal = network.getLine("NHV1_NHV2_2").getTerminal1();
        VoltageSourceConverterAdder adder = newVoltageSourceConverterAdder("vsc_id");
        adder.setPccTerminal(lineTerminal);
        adder.newVoltageRegulation()
            .withRegulating(true)
            .withTerminal(line2Terminal)
            .withTargetValue(120)
            .withMode(RegulationMode.VOLTAGE)
            .add();
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, adder::add);
        // THEN
        assertEquals("AC/DC Voltage Source Converter 'vsc_id': pccTerminal and voltageRegulation.terminal must refer to the same terminal",
            validationException.getMessage());
    }

    @Test
    void shouldSetPccTerminalAndVoltageRegulationTerminalWhenUsingTheVoltageSourceConverterAdderWithSameTerminal() {
        // GIVEN
        VoltageSourceConverterAdder adder = newVoltageSourceConverterAdder("vsc_id");
        adder.setPccTerminal(lineTerminal);
        adder.newVoltageRegulation()
            .withRegulating(true)
            .withTerminal(lineTerminal)
            .withTargetValue(120)
            .withMode(RegulationMode.VOLTAGE)
            .add();
        // WHEN
        VoltageSourceConverter voltageSourceConverter = adder.add();
        // THEN
        assertEquals(lineTerminal, voltageSourceConverter.getPccTerminal());
        assertEquals(lineTerminal, voltageSourceConverter.getVoltageRegulation().getTerminal());
    }

    @Test
    void shouldThrowExceptionWhenUsingTheVoltageSourceConverterAdderWithOnlyVoltageRegulationTerminal() {
        // GIVEN
        Terminal line2Terminal = network.getLine("NHV1_NHV2_2").getTerminal1();
        VoltageSourceConverterAdder adder = newVoltageSourceConverterAdder("vsc_id");
        adder.newVoltageRegulation()
            .withRegulating(true)
            .withTerminal(line2Terminal)
            .withTargetValue(120)
            .withMode(RegulationMode.VOLTAGE)
            .add();
        // WHEN
        ValidationException validationException = assertThrows(ValidationException.class, adder::add);
        // THEN
        assertEquals("AC/DC Voltage Source Converter 'vsc_id': pccTerminal and voltageRegulation.terminal must refer to the same terminal",
            validationException.getMessage());
    }

    @Test
    void shouldSetPccTerminalWhenUsingTheVoltageSourceConverterAdderWithOnlyPccTerminal() {
        // GIVEN
        VoltageSourceConverterAdder adder = newVoltageSourceConverterAdder("vsc_id");
        adder.setLocalTargetV(20);
        adder.setPccTerminal(lineTerminal);
        adder.newVoltageRegulation()
            .withRegulating(true)
            .withMode(RegulationMode.VOLTAGE)
            .add();
        // WHEN
        VoltageSourceConverter voltageSourceConverter = adder.add();
        // THEN
        assertEquals(lineTerminal, voltageSourceConverter.getPccTerminal());
        assertNull(voltageSourceConverter.getVoltageRegulation().getTerminal());
    }

    private static void assertPccTerminalAndVoltageRegulationTerminal(Terminal terminal1, VoltageSourceConverter voltageSourceConverter, int targetValue) {
        assertEquals(terminal1, voltageSourceConverter.getPccTerminal());
        assertEquals(terminal1, voltageSourceConverter.getVoltageRegulation().getTerminal());
        assertEquals(targetValue, voltageSourceConverter.getVoltageRegulation().getTargetValue());
    }

    private static void assertLocalPccTerminalAndVoltageRegulationTerminalNull(Terminal expectedTerminal, VoltageSourceConverter voltageSourceConverter) {
        assertEquals(expectedTerminal, voltageSourceConverter.getPccTerminal());
        assertNull(voltageSourceConverter.getVoltageRegulation().getTerminal());
        assertTrue(Double.isNaN(voltageSourceConverter.getVoltageRegulation().getTargetValue()));
    }

    private VoltageSourceConverterAdder newVoltageSourceConverterAdder(String id) {
        DcNode dcNode1 = network.newDcNode().setId("dcNode1").setNominalV(500.).add();
        DcNode dcNode2 = network.newDcNode().setId("dcNode2").setNominalV(500.).add();

        return voltageLevel.newVoltageSourceConverter()
            .setId(id)
            .setBus1("NGEN")
            .setBus2("NGEN")
            .setDcNode1(dcNode1.getId())
            .setDcNode2(dcNode2.getId())
            .setMinP(12.0)
            .setMaxP(120.0)
            .setControlMode(AcDcConverter.ControlMode.P_PCC)
            .setTargetP(100.0);
    }

    private VoltageSourceConverter createVoltageSourceConverter(DataVoltageRegulationHolderCreator dataVoltageRegulationHolderCreator) {
        VoltageSourceConverterAdder adder = newVoltageSourceConverterAdder(dataVoltageRegulationHolderCreator.id())
            .setLocalTargetV(dataVoltageRegulationHolderCreator.localTargetV())
            .setLocalTargetQ(dataVoltageRegulationHolderCreator.localTargetQ());
        if (dataVoltageRegulationHolderCreator.mode() != null) {
            adder.newVoltageRegulation()
                .withRegulating(dataVoltageRegulationHolderCreator.regulating())
                .withMode(dataVoltageRegulationHolderCreator.mode())
                .withTargetValue(dataVoltageRegulationHolderCreator.targetValue())
                .withTerminal(dataVoltageRegulationHolderCreator.remoteTerminal() ? remoteTerminal : null)
                .add();
        }
        // TODO MSA useless ?
        if (dataVoltageRegulationHolderCreator.remoteTerminal()) {
            adder.setPccTerminal(remoteTerminal);
        }
        return adder.add();
    }
}
