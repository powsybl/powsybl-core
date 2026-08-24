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
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkEventRecorder;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.events.NetworkEvent;
import com.powsybl.iidm.network.events.UpdateNetworkEvent;
import com.powsybl.iidm.network.impl.DcSwitchImpl;
import com.powsybl.iidm.network.impl.VoltageSourceConverterImpl;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.ShuntTestCaseFactory;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The attribute names {@link PartialSshEventTranslator} matches on are the ones IIDM puts in an
 * {@link UpdateNetworkEvent}, and IIDM publishes almost none of them as a constant: they are passed to
 * {@code notifyUpdate} as literals, in a module that is not a compile dependency of this one. A rename upstream
 * would therefore not break the build; every change of that attribute would simply stop being recognised, and
 * silently disappear from the exported file whenever unsupported changes are ignored rather than fatal.
 *
 * <p>These tests close that gap by driving the IIDM setters and checking the names that actually come out.</p>
 *
 * @author Nico Westerbeck {@literal <nico.westerbeck at 50hertz.com>}
 */
class PartialSshAttributeNameTest {

    @Test
    void switchOpen() {
        assertAttribute(FourSubstationsNodeBreakerFactory.create(), OPEN, network -> {
            Switch sw = network.getSwitchStream().findFirst().orElseThrow();
            sw.setOpen(!sw.isOpen());
        });
    }

    @Test
    void dcSwitchOpen() {
        assertAttribute(DcDetailedNetworkFactory.createSimple2NodesDcSwitch(), OPEN, network -> {
            DcSwitch dcSwitch = network.getDcSwitchStream().findFirst().orElseThrow();
            dcSwitch.setOpen(!dcSwitch.isOpen());
        });
    }

    @Test
    void loadSetpoints() {
        assertAttribute(FourSubstationsNodeBreakerFactory.create(), P0, network -> load(network).setP0(11.0));
        assertAttribute(FourSubstationsNodeBreakerFactory.create(), Q0, network -> load(network).setQ0(12.0));
    }

    @Test
    void generatorSetpoints() {
        assertAttribute(FourSubstationsNodeBreakerFactory.create(), TARGET_P, network -> generator(network).setTargetP(13.0));
        assertAttribute(FourSubstationsNodeBreakerFactory.create(), TARGET_Q, network -> generator(network).setTargetQ(14.0));
        assertAttribute(FourSubstationsNodeBreakerFactory.create(), TARGET_V, network -> generator(network).setTargetV(401.0));
        assertAttribute(FourSubstationsNodeBreakerFactory.create(), VOLTAGE_REGULATOR_ON, network -> {
            Generator generator = generator(network);
            generator.setTargetQ(15.0);
            generator.setVoltageRegulatorOn(!generator.isVoltageRegulatorOn());
        });
    }

    @Test
    void shuntCompensatorSetpoints() {
        assertAttribute(ShuntTestCaseFactory.createWithActivePower(), SECTION_COUNT,
                network -> shunt(network).setSectionCount(0));
        assertAttribute(ShuntTestCaseFactory.createLocalLinear(), TARGET_V,
                network -> shunt(network).setTargetV(401.0));
        assertAttribute(ShuntTestCaseFactory.createLocalLinear(), VOLTAGE_REGULATOR_ON, network -> {
            ShuntCompensator shunt = shunt(network);
            shunt.setVoltageRegulatorOn(!shunt.isVoltageRegulatorOn());
        });
    }

    @Test
    void staticVarCompensatorSetpoints() {
        assertAttribute(SvcTestCaseFactory.create(), VOLTAGE_SETPOINT,
                network -> svc(network).setVoltageSetpoint(391.0));
        assertAttribute(SvcTestCaseFactory.create(), REACTIVE_POWER_SETPOINT,
                network -> svc(network).setReactivePowerSetpoint(16.0));
    }

    @Test
    void hvdcAndConverterSetpoints() {
        assertAttribute(FourSubstationsNodeBreakerFactory.create(), ACTIVE_POWER_SETPOINT,
                network -> network.getHvdcLineStream().findFirst().map(HvdcLine.class::cast).orElseThrow()
                        .setActivePowerSetpoint(17.0));
        assertAttribute(FourSubstationsNodeBreakerFactory.create(), VOLTAGE_SETPOINT,
                network -> network.getVscConverterStationStream().findFirst().map(VscConverterStation.class::cast)
                        .orElseThrow().setVoltageSetpoint(402.0));
    }

    @Test
    void twoWindingsTransformerTapPositions() {
        assertAttribute(FourSubstationsNodeBreakerFactory.create(), PHASE_TAP_CHANGER_PREFIX + TAP_POSITION_SUFFIX,
                network -> transformer(network).getPhaseTapChanger().setTapPosition(0));
        assertAttribute(FourSubstationsNodeBreakerFactory.create(), RATIO_TAP_CHANGER_PREFIX + TAP_POSITION_SUFFIX,
                network -> transformer(network).getRatioTapChanger().setTapPosition(0));
    }

    /** A leg of a three windings transformer numbers the tap changer, which is what the mapping parses back. */
    @Test
    void threeWindingsTransformerTapPositionCarriesTheLegNumber() {
        assertAttribute(ThreeWindingsTransformerNetworkFactory.create(),
                RATIO_TAP_CHANGER_PREFIX + "2" + TAP_POSITION_SUFFIX,
                network -> network.getThreeWindingsTransformerStream().findFirst()
                        .map(ThreeWindingsTransformer.class::cast).orElseThrow()
                        .getLeg2().getRatioTapChanger().setTapPosition(0));
    }

    /** The few names IIDM does publish as a constant are checked against it directly. */
    @Test
    void publishedIidmConstantsAgree() {
        assertEquals(DcSwitchImpl.OPEN_ATTRIBUTE, OPEN);
        assertEquals(VoltageSourceConverterImpl.VOLTAGE_REGULATOR_ON_ATTRIBUTE, VOLTAGE_REGULATOR_ON);
    }

    private static Load load(Network network) {
        return network.getLoadStream().findFirst().orElseThrow();
    }

    private static Generator generator(Network network) {
        return network.getGeneratorStream().findFirst().orElseThrow();
    }

    private static ShuntCompensator shunt(Network network) {
        return network.getShuntCompensatorStream().findFirst().orElseThrow();
    }

    private static StaticVarCompensator svc(Network network) {
        return network.getStaticVarCompensatorStream().findFirst().orElseThrow();
    }

    private static TwoWindingsTransformer transformer(Network network) {
        return network.getTwoWindingsTransformerStream().findFirst().orElseThrow();
    }

    private static void assertAttribute(Network network, String expected, Consumer<Network> change) {
        NetworkEventRecorder recorder = new NetworkEventRecorder();
        network.addListener(recorder);
        change.accept(network);
        List<String> recorded = recorder.getEvents().stream()
                .filter(UpdateNetworkEvent.class::isInstance)
                .map(NetworkEvent.class::cast)
                .map(event -> ((UpdateNetworkEvent) event).attribute())
                .toList();
        assertTrue(recorded.contains(expected),
                () -> "IIDM no longer reports this change as '" + expected + "', so PartialSshEventTranslator"
                        + " would stop recognising it. Recorded instead: " + recorded);
    }
}
