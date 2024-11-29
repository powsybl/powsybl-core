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
    private final Map<TerminalRef, List<Identifiable<?>>> controllers;

    public RegulatedTerminalControllers(Network network) {
        this.network = network;
        this.controllers = new HashMap<>();
        findRegulatedTerminalControllers();
    }

    private void findRegulatedTerminalControllers() {
        network.getIdentifiables().forEach(identifiable -> {
            List<TerminalRef> regulatedTerminals = findRegulatedTerminals(identifiable);
            regulatedTerminals.forEach(regulatedTerminal -> controllers.computeIfAbsent(regulatedTerminal, k -> new ArrayList<>()).add(identifiable));
        });
    }

    private List<TerminalRef> findRegulatedTerminals(Identifiable<?> identifiable) {
        List<TerminalRef> terminals = findRegulatedTerminalsInModel(identifiable);
        terminals.addAll(findRegulatedTerminalsInExtensions(identifiable));
        return terminals;
    }

    private List<TerminalRef> findRegulatedTerminalsInModel(Identifiable<?> identifiable) {
        List<TerminalRef> regulatedTerminals = new ArrayList<>();
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
                if (hvdcConverterStation.getHvdcType() == HvdcConverterStation.HvdcType.VSC) {
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

    private static void add(List<TerminalRef> regulatedTerminals, Terminal regulatedTerminal) {
        if (regulatedTerminal != null) {
            regulatedTerminals.add(newTerminalRef(regulatedTerminal));
        }
    }

    private List<TerminalRef> findRegulatedTerminalsInExtensions(Identifiable<?> identifiable) {
        List<TerminalRef> regulatedTerminals = new ArrayList<>();
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
        Objects.requireNonNull(regulatedTerminal);
        return controllers.containsKey(newTerminalRef(regulatedTerminal));
    }

    public void replaceRegulatedTerminal(Terminal currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        Objects.requireNonNull(currentRegulatedTerminal);
        Objects.requireNonNull(newRegulatedTerminal);
        TerminalRef currentRegulatedTerminalRef = newTerminalRef(currentRegulatedTerminal);
        if (controllers.containsKey(currentRegulatedTerminalRef)) {
            controllers.get(currentRegulatedTerminalRef).forEach(identifiable -> replaceRegulatedTerminal(identifiable, currentRegulatedTerminalRef, newRegulatedTerminal));
        }
    }

    private static void replaceRegulatedTerminal(Identifiable<?> identifiable, TerminalRef currentRegulatedTerminal, Terminal newRegulatedTerminal) {
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

    private static void replaceRegulatedTerminalTwoWindingsTransformer(TwoWindingsTransformer t2w, TerminalRef currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        t2w.getOptionalRatioTapChanger().ifPresent(rtc -> replace(rtc, currentRegulatedTerminal, newRegulatedTerminal));
        t2w.getOptionalPhaseTapChanger().ifPresent(ptc -> replace(ptc, currentRegulatedTerminal, newRegulatedTerminal));
    }

    private static void replaceRegulatedTerminalThreeWindingsTransformer(ThreeWindingsTransformer t3w, TerminalRef currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        t3w.getLeg1().getOptionalRatioTapChanger().ifPresent(rtc -> replace(rtc, currentRegulatedTerminal, newRegulatedTerminal));
        t3w.getLeg1().getOptionalPhaseTapChanger().ifPresent(ptc -> replace(ptc, currentRegulatedTerminal, newRegulatedTerminal));
        t3w.getLeg2().getOptionalRatioTapChanger().ifPresent(rtc -> replace(rtc, currentRegulatedTerminal, newRegulatedTerminal));
        t3w.getLeg2().getOptionalPhaseTapChanger().ifPresent(ptc -> replace(ptc, currentRegulatedTerminal, newRegulatedTerminal));
        t3w.getLeg3().getOptionalRatioTapChanger().ifPresent(rtc -> replace(rtc, currentRegulatedTerminal, newRegulatedTerminal));
        t3w.getLeg3().getOptionalPhaseTapChanger().ifPresent(ptc -> replace(ptc, currentRegulatedTerminal, newRegulatedTerminal));
    }

    private static void replace(TapChanger<?, ?, ?, ?> tc, TerminalRef currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        if (tc.getRegulationTerminal() != null && currentRegulatedTerminal.equals(newTerminalRef(tc.getRegulationTerminal()))) {
            tc.setRegulationTerminal(newRegulatedTerminal);
        }
    }

    private static void replaceRegulatedTerminalGenerator(Generator generator, TerminalRef currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        if (generator.getRegulatingTerminal() != null && currentRegulatedTerminal.equals(newTerminalRef(generator.getRegulatingTerminal()))) {
            generator.setRegulatingTerminal(newRegulatedTerminal);
        } else {
            RemoteReactivePowerControl remoteReactivePowerControl = generator.getExtension(RemoteReactivePowerControl.class);
            if (remoteReactivePowerControl != null
                    && remoteReactivePowerControl.getRegulatingTerminal() != null
                    && currentRegulatedTerminal.equals(newTerminalRef(remoteReactivePowerControl.getRegulatingTerminal()))) {
                remoteReactivePowerControl.setRegulatingTerminal(newRegulatedTerminal);
            }
        }
    }

    private static void replaceRegulatedTerminalShuntCompensator(ShuntCompensator shuntCompensator, TerminalRef currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        if (shuntCompensator.getRegulatingTerminal() != null && currentRegulatedTerminal.equals(newTerminalRef(shuntCompensator.getRegulatingTerminal()))) {
            shuntCompensator.setRegulatingTerminal(newRegulatedTerminal);
        }
    }

    private static void replaceRegulatedTerminalStaticVarCompensator(StaticVarCompensator staticVarCompensator, TerminalRef currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        if (staticVarCompensator.getRegulatingTerminal() != null && currentRegulatedTerminal.equals(newTerminalRef(staticVarCompensator.getRegulatingTerminal()))) {
            staticVarCompensator.setRegulatingTerminal(newRegulatedTerminal);
        }
    }

    private static void replaceRegulatedTerminalHvdcConverterStation(HvdcConverterStation<?> hvdcConverterStation, TerminalRef currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        if (hvdcConverterStation.getHvdcType() == HvdcConverterStation.HvdcType.VSC) {
            VscConverterStation vscConverterStation = (VscConverterStation) hvdcConverterStation;
            if (vscConverterStation.getRegulatingTerminal() != null && currentRegulatedTerminal.equals(newTerminalRef(vscConverterStation.getRegulatingTerminal()))) {
                vscConverterStation.setRegulatingTerminal(newRegulatedTerminal);
            }
        }
    }

    private static void replaceRegulatedTerminalBattery(Battery battery, TerminalRef currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        VoltageRegulation voltageRegulation = battery.getExtension(VoltageRegulation.class);
        if (voltageRegulation != null && voltageRegulation.getRegulatingTerminal() != null && currentRegulatedTerminal.equals(newTerminalRef(voltageRegulation.getRegulatingTerminal()))) {
            voltageRegulation.setRegulatingTerminal(newRegulatedTerminal);
        }
    }

    private static void replaceRegulatedTerminalVoltageLevel(VoltageLevel voltageLevel, TerminalRef currentRegulatedTerminal, Terminal newRegulatedTerminal) {
        SlackTerminal slackTerminal = voltageLevel.getExtension(SlackTerminal.class);
        if (slackTerminal != null && slackTerminal.getTerminal() != null && currentRegulatedTerminal.equals(newTerminalRef(slackTerminal.getTerminal()))) {
            slackTerminal.setTerminal(newRegulatedTerminal);
        }
    }

    private static TerminalRef newTerminalRef(Terminal terminal) {
        Objects.requireNonNull(terminal);
        return new TerminalRef(terminal.getConnectable().getId(), terminal.getSide());
    }

    // To avoid comparing regulating terminal objects, with custom IIDM implementations could be problematic.
    private record TerminalRef(String identifiableId, ThreeSides side) {
    }
}
