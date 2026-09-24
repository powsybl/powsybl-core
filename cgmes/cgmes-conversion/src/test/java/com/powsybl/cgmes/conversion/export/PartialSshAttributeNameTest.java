/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.iidm.network.DcSwitch;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkEventRecorder;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.events.UpdateNetworkEvent;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.test.PhaseShifterTestCaseFactory;
import com.powsybl.iidm.network.test.ShuntTestCaseFactory;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.powsybl.cgmes.conversion.export.PartialSshEventTranslator.ACTIVE_POWER_SETPOINT;
import static com.powsybl.cgmes.conversion.export.PartialSshEventTranslator.OPEN;
import static com.powsybl.cgmes.conversion.export.PartialSshEventTranslator.P0;
import static com.powsybl.cgmes.conversion.export.PartialSshEventTranslator.PHASE_TAP_CHANGER_PREFIX;
import static com.powsybl.cgmes.conversion.export.PartialSshEventTranslator.Q0;
import static com.powsybl.cgmes.conversion.export.PartialSshEventTranslator.RATIO_TAP_CHANGER_PREFIX;
import static com.powsybl.cgmes.conversion.export.PartialSshEventTranslator.REACTIVE_POWER_SETPOINT;
import static com.powsybl.cgmes.conversion.export.PartialSshEventTranslator.SECTION_COUNT;
import static com.powsybl.cgmes.conversion.export.PartialSshEventTranslator.TAP_POSITION_SUFFIX;
import static com.powsybl.cgmes.conversion.export.PartialSshEventTranslator.TARGET_P;
import static com.powsybl.cgmes.conversion.export.PartialSshEventTranslator.TARGET_Q;
import static com.powsybl.cgmes.conversion.export.PartialSshEventTranslator.TARGET_V;
import static com.powsybl.cgmes.conversion.export.PartialSshEventTranslator.VOLTAGE_REGULATOR_ON;
import static com.powsybl.cgmes.conversion.export.PartialSshEventTranslator.VOLTAGE_SETPOINT;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins the attribute names {@link PartialSshEventTranslator} recognises to the names the IIDM implementation
 * reports in its update events.
 *
 * <p>The names are plain strings scattered over the iidm module, so nothing but this test ties the two sides
 * together: a rename there would otherwise turn every change of that attribute into an unsupported one, failing
 * exports under {@code FAIL} and silently dropping the change under {@code IGNORE}.</p>
 *
 * @author Nico Westerbeck {@literal <nico.westerbeck at 50hertz.com>}
 */
class PartialSshAttributeNameTest {

    @Test
    void switchState() {
        Network network = HvdcTestNetwork.createVsc();
        Switch breaker = network.getSwitch("BK1");
        assertEquals(List.of(OPEN), attributesUpdatedBy(network, () -> breaker.setOpen(!breaker.isOpen())));
    }

    @Test
    void dcSwitchState() {
        Network network = DcDetailedNetworkFactory.createLccBipoleGroundReturn();
        DcSwitch dcSwitch = network.getDcSwitch("dcSwitchFrPosBypass");
        assertEquals(List.of(OPEN), attributesUpdatedBy(network, () -> dcSwitch.setOpen(!dcSwitch.isOpen())));
    }

    @Test
    void loadSetpoints() {
        Network network = EurostagTutorialExample1Factory.create();
        Load load = network.getLoad("LOAD");
        assertEquals(List.of(P0), attributesUpdatedBy(network, () -> load.setP0(load.getP0() + 1.0)));
        assertEquals(List.of(Q0), attributesUpdatedBy(network, () -> load.setQ0(load.getQ0() + 1.0)));
    }

    @Test
    void generatorTargetsAndRegulation() {
        Network network = EurostagTutorialExample1Factory.create();
        Generator generator = network.getGenerator("GEN");
        assertEquals(List.of(TARGET_P), attributesUpdatedBy(network, () -> generator.setTargetP(generator.getTargetP() + 1.0)));
        assertEquals(List.of(TARGET_Q), attributesUpdatedBy(network, () -> generator.setTargetQ(generator.getTargetQ() + 1.0)));
        assertEquals(List.of(TARGET_V), attributesUpdatedBy(network, () -> generator.setTargetV(generator.getTargetV() + 1.0)));
        assertEquals(List.of(VOLTAGE_REGULATOR_ON),
                attributesUpdatedBy(network, () -> generator.setVoltageRegulatorOn(!generator.isVoltageRegulatorOn())));
    }

    @Test
    void twoWindingsTransformerTapPositions() {
        Network withRatioTapChanger = EurostagTutorialExample1Factory.create();
        assertEquals(List.of(RATIO_TAP_CHANGER_PREFIX + TAP_POSITION_SUFFIX), attributesUpdatedBy(withRatioTapChanger,
                () -> withRatioTapChanger.getTwoWindingsTransformer(EurostagTutorialExample1Factory.NHV2_NLOAD).getRatioTapChanger().setTapPosition(2)));

        Network withPhaseTapChanger = PhaseShifterTestCaseFactory.create();
        assertEquals(List.of(PHASE_TAP_CHANGER_PREFIX + TAP_POSITION_SUFFIX), attributesUpdatedBy(withPhaseTapChanger,
                () -> withPhaseTapChanger.getTwoWindingsTransformer("PS1").getPhaseTapChanger().setTapPosition(2)));
    }

    /** The end number sits between the tap changer prefix and the position suffix, and only there. */
    @Test
    void threeWindingsTransformerTapPositions() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer("3WT");
        transformer.getLeg1().newPhaseTapChanger()
                .setTapPosition(0)
                .setRegulating(false)
                .beginStep().setAlpha(0.0).endStep()
                .beginStep().setAlpha(5.0).endStep()
                .add();

        assertEquals(List.of(RATIO_TAP_CHANGER_PREFIX + "2" + TAP_POSITION_SUFFIX),
                attributesUpdatedBy(network, () -> transformer.getLeg2().getRatioTapChanger().setTapPosition(1)));
        assertEquals(List.of(PHASE_TAP_CHANGER_PREFIX + "1" + TAP_POSITION_SUFFIX),
                attributesUpdatedBy(network, () -> transformer.getLeg1().getPhaseTapChanger().setTapPosition(1)));
    }

    @Test
    void shuntCompensatorOperatingValues() {
        Network network = ShuntTestCaseFactory.create();
        ShuntCompensator shunt = network.getShuntCompensator("SHUNT");
        assertEquals(List.of(SECTION_COUNT), attributesUpdatedBy(network, () -> shunt.setSectionCount(0)));
        assertEquals(List.of(TARGET_V), attributesUpdatedBy(network, () -> shunt.setTargetV(shunt.getTargetV() + 1.0)));
        assertEquals(List.of(VOLTAGE_REGULATOR_ON),
                attributesUpdatedBy(network, () -> shunt.setVoltageRegulatorOn(!shunt.isVoltageRegulatorOn())));
    }

    @Test
    void staticVarCompensatorSetpoints() {
        Network network = SvcTestCaseFactory.create();
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        assertEquals(List.of(VOLTAGE_SETPOINT), attributesUpdatedBy(network, () -> svc.setVoltageSetpoint(svc.getVoltageSetpoint() + 1.0)));
        assertEquals(List.of(REACTIVE_POWER_SETPOINT), attributesUpdatedBy(network, () -> svc.setReactivePowerSetpoint(100.0)));
    }

    @Test
    void hvdcSetpoints() {
        Network network = HvdcTestNetwork.createVsc();
        assertEquals(List.of(ACTIVE_POWER_SETPOINT),
                attributesUpdatedBy(network, () -> network.getHvdcLine("L").setActivePowerSetpoint(290.0)));

        VscConverterStation voltageRegulating = network.getVscConverterStation("C1");
        assertEquals(List.of(VOLTAGE_SETPOINT),
                attributesUpdatedBy(network, () -> voltageRegulating.setVoltageSetpoint(voltageRegulating.getVoltageSetpoint() + 1.0)));

        VscConverterStation reactivePowerRegulating = network.getVscConverterStation("C2");
        assertEquals(List.of(REACTIVE_POWER_SETPOINT),
                attributesUpdatedBy(network, () -> reactivePowerRegulating.setReactivePowerSetpoint(100.0)));
    }

    /** The attribute names of the update events that the given change causes, in the order they were reported. */
    private static List<String> attributesUpdatedBy(Network network, Runnable change) {
        NetworkEventRecorder recorder = new NetworkEventRecorder();
        network.addListener(recorder);
        try {
            change.run();
        } finally {
            network.removeListener(recorder);
        }
        return recorder.getEvents().stream()
                .filter(UpdateNetworkEvent.class::isInstance)
                .map(UpdateNetworkEvent.class::cast)
                .map(UpdateNetworkEvent::attribute)
                .toList();
    }
}
