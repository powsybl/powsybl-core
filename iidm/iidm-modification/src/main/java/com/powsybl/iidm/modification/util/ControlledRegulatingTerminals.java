/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.extensions.VoltageRegulation;

import java.util.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class ControlledRegulatingTerminals {

    private final Network network;
    private final Map<Terminal, List<Identifiable<?>>> controllers;

    public ControlledRegulatingTerminals(Network network) {
        this.network = network;
        this.controllers = new HashMap<>();
        findControlledRegulatingTerminals();
    }

    private void findControlledRegulatingTerminals() {
        network.getIdentifiables().forEach(identifiable -> {
            List<Terminal> regulatingTerminals = findRegulatingTerminals(identifiable);
            regulatingTerminals.forEach(regulatingTerminal -> controllers.computeIfAbsent(regulatingTerminal, k -> new ArrayList<>()).add(identifiable));
        });
    }

    private List<Terminal> findRegulatingTerminals(Identifiable<?> identifiable) {
        List<Terminal> terminals = findRegulatingTerminalsInModel(identifiable);
        terminals.addAll(findRegulatingTerminalsInExtensions(identifiable));
        return terminals;
    }

    private List<Terminal> findRegulatingTerminalsInModel(Identifiable<?> identifiable) {
        List<Terminal> regulatingTerminals = new ArrayList<>();
        switch (identifiable.getType()) {
            case NETWORK, SUBSTATION, VOLTAGE_LEVEL, AREA, HVDC_LINE, BUS, SWITCH, BUSBAR_SECTION, LINE, TIE_LINE, BATTERY, LOAD, DANGLING_LINE, OVERLOAD_MANAGEMENT_SYSTEM, GROUND -> {
            }
            case TWO_WINDINGS_TRANSFORMER -> {
                TwoWindingsTransformer t2w = (TwoWindingsTransformer) identifiable;
                t2w.getOptionalRatioTapChanger().ifPresent(rtc -> add(regulatingTerminals, rtc.getRegulationTerminal()));
                t2w.getOptionalPhaseTapChanger().ifPresent(ptc -> add(regulatingTerminals, ptc.getRegulationTerminal()));
            }
            case THREE_WINDINGS_TRANSFORMER -> {
                ThreeWindingsTransformer t3w = (ThreeWindingsTransformer) identifiable;
                t3w.getLeg1().getOptionalRatioTapChanger().ifPresent(rtc -> add(regulatingTerminals, rtc.getRegulationTerminal()));
                t3w.getLeg1().getOptionalPhaseTapChanger().ifPresent(ptc -> add(regulatingTerminals, ptc.getRegulationTerminal()));
                t3w.getLeg2().getOptionalRatioTapChanger().ifPresent(rtc -> add(regulatingTerminals, rtc.getRegulationTerminal()));
                t3w.getLeg2().getOptionalPhaseTapChanger().ifPresent(ptc -> add(regulatingTerminals, ptc.getRegulationTerminal()));
                t3w.getLeg3().getOptionalRatioTapChanger().ifPresent(rtc -> add(regulatingTerminals, rtc.getRegulationTerminal()));
                t3w.getLeg3().getOptionalPhaseTapChanger().ifPresent(ptc -> add(regulatingTerminals, ptc.getRegulationTerminal()));
            }
            case GENERATOR -> {
                Generator generator = (Generator) identifiable;
                add(regulatingTerminals, generator.getRegulatingTerminal());
            }
            case SHUNT_COMPENSATOR -> {
                ShuntCompensator shuntCompensator = (ShuntCompensator) identifiable;
                add(regulatingTerminals, shuntCompensator.getRegulatingTerminal());
            }
            case STATIC_VAR_COMPENSATOR -> {
                StaticVarCompensator staticVarCompensator = (StaticVarCompensator) identifiable;
                add(regulatingTerminals, staticVarCompensator.getRegulatingTerminal());
            }
            case HVDC_CONVERTER_STATION -> {
                HvdcConverterStation<?> hvdcConverterStation = (HvdcConverterStation<?>) identifiable;
                if (hvdcConverterStation.getHvdcType().equals(HvdcConverterStation.HvdcType.VSC)) {
                    VscConverterStation vscConverterStation = (VscConverterStation) hvdcConverterStation;
                    add(regulatingTerminals, vscConverterStation.getRegulatingTerminal());
                }
            }
        }
        return regulatingTerminals;
    }

    private static void add(List<Terminal> regulatingTerminals, Terminal regulatingTerminal) {
        if (regulatingTerminal != null) {
            regulatingTerminals.add(regulatingTerminal);
        }
    }

    private List<Terminal> findRegulatingTerminalsInExtensions(Identifiable<?> identifiable) {
        List<Terminal> regulatingTerminals = new ArrayList<>();
        identifiable.getExtensions().stream().map(Extension::getName).forEach(extensionName ->
                add(regulatingTerminals, findRegulatingTerminalInExtension(identifiable, extensionName)));
        return regulatingTerminals;
    }

    private static Terminal findRegulatingTerminalInExtension(Identifiable<?> identifiable, String extensionName) {
        switch (extensionName) {
            case "voltageRegulation" -> {
                Battery battery = (Battery) identifiable;
                VoltageRegulation voltageRegulation = battery.getExtension(VoltageRegulation.class);
                return voltageRegulation.getRegulatingTerminal();
            }
            case "generatorRemoteReactivePowerControl" -> {
                Generator generator = (Generator) identifiable;
                RemoteReactivePowerControl remoteReactivePowerControl = generator.getExtension(RemoteReactivePowerControl.class);
                return remoteReactivePowerControl.getRegulatingTerminal();
            }
            case "slackTerminal" -> {
                VoltageLevel voltageLevel = (VoltageLevel) identifiable;
                SlackTerminal slackTerminal = voltageLevel.getExtension(SlackTerminal.class);
                return slackTerminal.getTerminal();
            }
            default -> {
                return null;
            }
        }
    }

    public boolean usedAsRegulatingTerminal(Terminal regulatingTerminal) {
        return controllers.containsKey(regulatingTerminal);
    }

    public void replaceRegulatingTerminal(Terminal regulatingTerminal, Terminal newRegulatingTerminal) {
        if (controllers.containsKey(regulatingTerminal)) {
            controllers.get(regulatingTerminal).forEach(identifiable -> replaceRegulatingTerminal(identifiable, regulatingTerminal, newRegulatingTerminal));
        }
    }

    private static void replaceRegulatingTerminal(Identifiable<?> identifiable, Terminal expectedRegulatingTerminal, Terminal newRegulatingTerminal) {
        switch (identifiable.getType()) {
            case TWO_WINDINGS_TRANSFORMER ->
                    replaceRegulatingTerminalTwoWindingsTransformer((TwoWindingsTransformer) identifiable, expectedRegulatingTerminal, newRegulatingTerminal);
            case THREE_WINDINGS_TRANSFORMER ->
                    replaceRegulatingTerminalThreeWindingsTransformer((ThreeWindingsTransformer) identifiable, expectedRegulatingTerminal, newRegulatingTerminal);
            case GENERATOR ->
                    replaceRegulatingTerminalGenerator((Generator) identifiable, expectedRegulatingTerminal, newRegulatingTerminal);
            case SHUNT_COMPENSATOR ->
                    replaceRegulatingTerminalShuntCompensator((ShuntCompensator) identifiable, expectedRegulatingTerminal, newRegulatingTerminal);
            case STATIC_VAR_COMPENSATOR ->
                    replaceRegulatingTerminalStaticVarCompensator((StaticVarCompensator) identifiable, expectedRegulatingTerminal, newRegulatingTerminal);
            case HVDC_CONVERTER_STATION ->
                    replaceRegulatingTerminalHvdcConverterStation((HvdcConverterStation<?>) identifiable, expectedRegulatingTerminal, newRegulatingTerminal);
            case BATTERY ->
                    replaceRegulatingTerminalBattery((Battery) identifiable, expectedRegulatingTerminal, newRegulatingTerminal);
            case VOLTAGE_LEVEL ->
                    replaceRegulatingTerminalVoltageLevel((VoltageLevel) identifiable, expectedRegulatingTerminal, newRegulatingTerminal);
            default -> throw new PowsyblException("unexpected identifiable type: " + identifiable.getType());
        }
    }

    private static void replaceRegulatingTerminalTwoWindingsTransformer(TwoWindingsTransformer t2w, Terminal expectedRegulatingTerminal, Terminal newRegulatingTerminal) {
        t2w.getOptionalRatioTapChanger().ifPresent(rtc -> replace(rtc, expectedRegulatingTerminal, newRegulatingTerminal));
        t2w.getOptionalPhaseTapChanger().ifPresent(ptc -> replace(ptc, expectedRegulatingTerminal, newRegulatingTerminal));
    }

    private static void replaceRegulatingTerminalThreeWindingsTransformer(ThreeWindingsTransformer t3w, Terminal expectedRegulatingTerminal, Terminal newRegulatingTerminal) {
        t3w.getLeg1().getOptionalRatioTapChanger().ifPresent(rtc -> replace(rtc, expectedRegulatingTerminal, newRegulatingTerminal));
        t3w.getLeg1().getOptionalPhaseTapChanger().ifPresent(ptc -> replace(ptc, expectedRegulatingTerminal, newRegulatingTerminal));
        t3w.getLeg2().getOptionalRatioTapChanger().ifPresent(rtc -> replace(rtc, expectedRegulatingTerminal, newRegulatingTerminal));
        t3w.getLeg2().getOptionalPhaseTapChanger().ifPresent(ptc -> replace(ptc, expectedRegulatingTerminal, newRegulatingTerminal));
        t3w.getLeg3().getOptionalRatioTapChanger().ifPresent(rtc -> replace(rtc, expectedRegulatingTerminal, newRegulatingTerminal));
        t3w.getLeg3().getOptionalPhaseTapChanger().ifPresent(ptc -> replace(ptc, expectedRegulatingTerminal, newRegulatingTerminal));
    }

    private static void replace(TapChanger<?, ?, ?, ?> tc, Terminal expectedRegulatingTerminal, Terminal newRegulatingTerminal) {
        if (tc.getRegulationTerminal().equals(expectedRegulatingTerminal)) {
            tc.setRegulationTerminal(newRegulatingTerminal);
        }
    }

    private static void replaceRegulatingTerminalGenerator(Generator generator, Terminal expectedRegulatingTerminal, Terminal newRegulatingTerminal) {
        if (generator.getRegulatingTerminal().equals(expectedRegulatingTerminal)) {
            generator.setRegulatingTerminal(newRegulatingTerminal);
        } else {
            RemoteReactivePowerControl remoteReactivePowerControl = generator.getExtension(RemoteReactivePowerControl.class);
            if (remoteReactivePowerControl != null && remoteReactivePowerControl.getRegulatingTerminal().equals(expectedRegulatingTerminal)) {
                remoteReactivePowerControl.setRegulatingTerminal(newRegulatingTerminal);
            }
        }
    }

    private static void replaceRegulatingTerminalShuntCompensator(ShuntCompensator shuntCompensator, Terminal expectedRegulatingTerminal, Terminal newRegulatingTerminal) {
        if (shuntCompensator.getRegulatingTerminal().equals(expectedRegulatingTerminal)) {
            shuntCompensator.setRegulatingTerminal(newRegulatingTerminal);
        }
    }

    private static void replaceRegulatingTerminalStaticVarCompensator(StaticVarCompensator staticVarCompensator, Terminal expectedRegulatingTerminal, Terminal newRegulatingTerminal) {
        if (staticVarCompensator.getRegulatingTerminal().equals(expectedRegulatingTerminal)) {
            staticVarCompensator.setRegulatingTerminal(newRegulatingTerminal);
        }
    }

    private static void replaceRegulatingTerminalHvdcConverterStation(HvdcConverterStation<?> hvdcConverterStation, Terminal expectedRegulatingTerminal, Terminal newRegulatingTerminal) {
        if (hvdcConverterStation.getHvdcType().equals(HvdcConverterStation.HvdcType.VSC)) {
            VscConverterStation vscConverterStation = (VscConverterStation) hvdcConverterStation;
            if (vscConverterStation.getRegulatingTerminal().equals(expectedRegulatingTerminal)) {
                vscConverterStation.setRegulatingTerminal(newRegulatingTerminal);
            }
        }
    }

    private static void replaceRegulatingTerminalBattery(Battery battery, Terminal expectedRegulatingTerminal, Terminal newRegulatingTerminal) {
        VoltageRegulation voltageRegulation = battery.getExtension(VoltageRegulation.class);
        if (voltageRegulation != null && voltageRegulation.getRegulatingTerminal().equals(expectedRegulatingTerminal)) {
            voltageRegulation.setRegulatingTerminal(newRegulatingTerminal);
        }
    }

    private static void replaceRegulatingTerminalVoltageLevel(VoltageLevel voltageLevel, Terminal expectedRegulatingTerminal, Terminal newRegulatingTerminal) {
        SlackTerminal slackTerminal = voltageLevel.getExtension(SlackTerminal.class);
        if (slackTerminal != null && slackTerminal.getTerminal().equals(expectedRegulatingTerminal)) {
            slackTerminal.setTerminal(newRegulatingTerminal);
        }
    }
}