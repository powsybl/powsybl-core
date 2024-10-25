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
public class RegulatedTerminalControllers {

    private final Network network;
    private final Map<Terminal, List<Identifiable<?>>> controllers;

    public RegulatedTerminalControllers(Network network) {
        this.network = network;
        this.controllers = new HashMap<>();
        findRegulatedTerminalControllers();
    }

    private void findRegulatedTerminalControllers() {
        network.getIdentifiables().forEach(identifiable -> {
            List<Terminal> regulatedTerminals = findRegulatedTerminals(identifiable);
            regulatedTerminals.forEach(regulatedTerminal -> controllers.computeIfAbsent(regulatedTerminal, k -> new ArrayList<>()).add(identifiable));
        });
    }

    private List<Terminal> findRegulatedTerminals(Identifiable<?> identifiable) {
        List<Terminal> terminals = findRegulatedTerminalsInModel(identifiable);
        terminals.addAll(findRegulatedTerminalsInExtensions(identifiable));
        return terminals;
    }

    private List<Terminal> findRegulatedTerminalsInModel(Identifiable<?> identifiable) {
        List<Terminal> regulatedTerminals = new ArrayList<>();
        switch (identifiable.getType()) {
            case TWO_WINDINGS_TRANSFORMER -> {
                TwoWindingsTransformer t2w = (TwoWindingsTransformer) identifiable;
                t2w.getOptionalRatioTapChanger().ifPresent(rtc -> add(regulatedTerminals, rtc.getRegulationTerminal()));
                t2w.getOptionalPhaseTapChanger().ifPresent(ptc -> add(regulatedTerminals, ptc.getRegulationTerminal()));
            }
            case THREE_WINDINGS_TRANSFORMER -> {
                ThreeWindingsTransformer t3w = (ThreeWindingsTransformer) identifiable;
                t3w.getLeg1().getOptionalRatioTapChanger().ifPresent(rtc -> add(regulatedTerminals, rtc.getRegulationTerminal()));
                t3w.getLeg1().getOptionalPhaseTapChanger().ifPresent(ptc -> add(regulatedTerminals, ptc.getRegulationTerminal()));
                t3w.getLeg2().getOptionalRatioTapChanger().ifPresent(rtc -> add(regulatedTerminals, rtc.getRegulationTerminal()));
                t3w.getLeg2().getOptionalPhaseTapChanger().ifPresent(ptc -> add(regulatedTerminals, ptc.getRegulationTerminal()));
                t3w.getLeg3().getOptionalRatioTapChanger().ifPresent(rtc -> add(regulatedTerminals, rtc.getRegulationTerminal()));
                t3w.getLeg3().getOptionalPhaseTapChanger().ifPresent(ptc -> add(regulatedTerminals, ptc.getRegulationTerminal()));
            }
            case GENERATOR -> {
                Generator generator = (Generator) identifiable;
                add(regulatedTerminals, generator.getRegulatingTerminal());
            }
            case SHUNT_COMPENSATOR -> {
                ShuntCompensator shuntCompensator = (ShuntCompensator) identifiable;
                add(regulatedTerminals, shuntCompensator.getRegulatingTerminal());
            }
            case STATIC_VAR_COMPENSATOR -> {
                StaticVarCompensator staticVarCompensator = (StaticVarCompensator) identifiable;
                add(regulatedTerminals, staticVarCompensator.getRegulatingTerminal());
            }
            case HVDC_CONVERTER_STATION -> {
                HvdcConverterStation<?> hvdcConverterStation = (HvdcConverterStation<?>) identifiable;
                if (hvdcConverterStation.getHvdcType().equals(HvdcConverterStation.HvdcType.VSC)) {
                    VscConverterStation vscConverterStation = (VscConverterStation) hvdcConverterStation;
                    add(regulatedTerminals, vscConverterStation.getRegulatingTerminal());
                }
            }
            default -> {
                // do nothing
            }
        }
        return regulatedTerminals;
    }

    private static void add(List<Terminal> regulatedTerminals, Terminal regulatedTerminal) {
        if (regulatedTerminal != null) {
            regulatedTerminals.add(regulatedTerminal);
        }
    }

    private List<Terminal> findRegulatedTerminalsInExtensions(Identifiable<?> identifiable) {
        List<Terminal> regulatedTerminals = new ArrayList<>();
        identifiable.getExtensions().stream().map(Extension::getName).forEach(extensionName ->
                add(regulatedTerminals, findRegulatedTerminalInExtension(identifiable, extensionName)));
        return regulatedTerminals;
    }

    private static Terminal findRegulatedTerminalInExtension(Identifiable<?> identifiable, String extensionName) {
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

    public boolean usedAsRegulatedTerminal(Terminal regulatedTerminal) {
        return controllers.containsKey(regulatedTerminal);
    }

    public void replaceRegulatedTerminal(Terminal currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        if (controllers.containsKey(currentRegulatedTerminal)) {
            controllers.get(currentRegulatedTerminal).forEach(identifiable -> replaceRegulatedTerminal(identifiable, currentRegulatedTerminal, newRegulatedTerminal));
        }
    }

    private static void replaceRegulatedTerminal(Identifiable<?> identifiable, Terminal currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        switch (identifiable.getType()) {
            case TWO_WINDINGS_TRANSFORMER ->
                    replaceRegulatedTerminalTwoWindingsTransformer((TwoWindingsTransformer) identifiable, currentRegulatedTerminal, newRegulatedTerminal);
            case THREE_WINDINGS_TRANSFORMER ->
                    replaceRegulatedTerminalThreeWindingsTransformer((ThreeWindingsTransformer) identifiable, currentRegulatedTerminal, newRegulatedTerminal);
            case GENERATOR ->
                    replaceRegulatedTerminalGenerator((Generator) identifiable, currentRegulatedTerminal, newRegulatedTerminal);
            case SHUNT_COMPENSATOR ->
                    replaceRegulatedTerminalShuntCompensator((ShuntCompensator) identifiable, currentRegulatedTerminal, newRegulatedTerminal);
            case STATIC_VAR_COMPENSATOR ->
                    replaceRegulatedTerminalStaticVarCompensator((StaticVarCompensator) identifiable, currentRegulatedTerminal, newRegulatedTerminal);
            case HVDC_CONVERTER_STATION ->
                    replaceRegulatedTerminalHvdcConverterStation((HvdcConverterStation<?>) identifiable, currentRegulatedTerminal, newRegulatedTerminal);
            case BATTERY ->
                    replaceRegulatedTerminalBattery((Battery) identifiable, currentRegulatedTerminal, newRegulatedTerminal);
            case VOLTAGE_LEVEL ->
                    replaceRegulatedTerminalVoltageLevel((VoltageLevel) identifiable, currentRegulatedTerminal, newRegulatedTerminal);
            default -> throw new PowsyblException("unexpected identifiable type: " + identifiable.getType());
        }
    }

    private static void replaceRegulatedTerminalTwoWindingsTransformer(TwoWindingsTransformer t2w, Terminal currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        t2w.getOptionalRatioTapChanger().ifPresent(rtc -> replace(rtc, currentRegulatedTerminal, newRegulatedTerminal));
        t2w.getOptionalPhaseTapChanger().ifPresent(ptc -> replace(ptc, currentRegulatedTerminal, newRegulatedTerminal));
    }

    private static void replaceRegulatedTerminalThreeWindingsTransformer(ThreeWindingsTransformer t3w, Terminal currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        t3w.getLeg1().getOptionalRatioTapChanger().ifPresent(rtc -> replace(rtc, currentRegulatedTerminal, newRegulatedTerminal));
        t3w.getLeg1().getOptionalPhaseTapChanger().ifPresent(ptc -> replace(ptc, currentRegulatedTerminal, newRegulatedTerminal));
        t3w.getLeg2().getOptionalRatioTapChanger().ifPresent(rtc -> replace(rtc, currentRegulatedTerminal, newRegulatedTerminal));
        t3w.getLeg2().getOptionalPhaseTapChanger().ifPresent(ptc -> replace(ptc, currentRegulatedTerminal, newRegulatedTerminal));
        t3w.getLeg3().getOptionalRatioTapChanger().ifPresent(rtc -> replace(rtc, currentRegulatedTerminal, newRegulatedTerminal));
        t3w.getLeg3().getOptionalPhaseTapChanger().ifPresent(ptc -> replace(ptc, currentRegulatedTerminal, newRegulatedTerminal));
    }

    private static void replace(TapChanger<?, ?, ?, ?> tc, Terminal currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        if (tc.getRegulationTerminal().equals(currentRegulatedTerminal)) {
            tc.setRegulationTerminal(newRegulatedTerminal);
        }
    }

    private static void replaceRegulatedTerminalGenerator(Generator generator, Terminal currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        if (generator.getRegulatingTerminal().equals(currentRegulatedTerminal)) {
            generator.setRegulatingTerminal(newRegulatedTerminal);
        } else {
            RemoteReactivePowerControl remoteReactivePowerControl = generator.getExtension(RemoteReactivePowerControl.class);
            if (remoteReactivePowerControl != null && remoteReactivePowerControl.getRegulatingTerminal().equals(currentRegulatedTerminal)) {
                remoteReactivePowerControl.setRegulatingTerminal(newRegulatedTerminal);
            }
        }
    }

    private static void replaceRegulatedTerminalShuntCompensator(ShuntCompensator shuntCompensator, Terminal currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        if (shuntCompensator.getRegulatingTerminal().equals(currentRegulatedTerminal)) {
            shuntCompensator.setRegulatingTerminal(newRegulatedTerminal);
        }
    }

    private static void replaceRegulatedTerminalStaticVarCompensator(StaticVarCompensator staticVarCompensator, Terminal currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        if (staticVarCompensator.getRegulatingTerminal().equals(currentRegulatedTerminal)) {
            staticVarCompensator.setRegulatingTerminal(newRegulatedTerminal);
        }
    }

    private static void replaceRegulatedTerminalHvdcConverterStation(HvdcConverterStation<?> hvdcConverterStation, Terminal currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        if (hvdcConverterStation.getHvdcType().equals(HvdcConverterStation.HvdcType.VSC)) {
            VscConverterStation vscConverterStation = (VscConverterStation) hvdcConverterStation;
            if (vscConverterStation.getRegulatingTerminal().equals(currentRegulatedTerminal)) {
                vscConverterStation.setRegulatingTerminal(newRegulatedTerminal);
            }
        }
    }

    private static void replaceRegulatedTerminalBattery(Battery battery, Terminal currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        VoltageRegulation voltageRegulation = battery.getExtension(VoltageRegulation.class);
        if (voltageRegulation != null && voltageRegulation.getRegulatingTerminal().equals(currentRegulatedTerminal)) {
            voltageRegulation.setRegulatingTerminal(newRegulatedTerminal);
        }
    }

    private static void replaceRegulatedTerminalVoltageLevel(VoltageLevel voltageLevel, Terminal currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        SlackTerminal slackTerminal = voltageLevel.getExtension(SlackTerminal.class);
        if (slackTerminal != null && slackTerminal.getTerminal().equals(currentRegulatedTerminal)) {
            slackTerminal.setTerminal(newRegulatedTerminal);
        }
    }
}
