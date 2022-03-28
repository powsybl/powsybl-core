/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.SwitchTerminal.TerminalAndSign;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class RegulatingControlMapping {

    static final String MISSING_IIDM_TERMINAL = "IIDM terminal for this CGMES terminal: %s";

    private static final String REGULATING_TERMINAL = "Regulating Terminal";
    private static final String REGULATING_CONTROL = "RegulatingControl";
    private static final String TERMINAL = "Terminal";
    private static final String VOLTAGE = "voltage";

    private final Context context;
    private final RegulatingControlMappingForGenerators regulatingControlMappingForGenerators;
    private final RegulatingControlMappingForTransformers regulatingControlMappingForTransformers;
    private final RegulatingControlMappingForShuntCompensators regulatingControlMappingForShuntCompensators;
    private final RegulatingControlMappingForStaticVarCompensators regulatingControlMappingForStaticVarCompensators;
    private final RegulatingControlMappingForVscConverters regulatingControlMappingForVscConverters;

    RegulatingControlMapping(Context context) {
        this.context = context;
        regulatingControlMappingForGenerators = new RegulatingControlMappingForGenerators(this, context);
        regulatingControlMappingForTransformers = new RegulatingControlMappingForTransformers(this, context);
        regulatingControlMappingForStaticVarCompensators = new RegulatingControlMappingForStaticVarCompensators(this, context);
        regulatingControlMappingForShuntCompensators = new RegulatingControlMappingForShuntCompensators(this, context);
        regulatingControlMappingForVscConverters = new RegulatingControlMappingForVscConverters(this, context);
    }

    public RegulatingControlMappingForGenerators forGenerators() {
        return regulatingControlMappingForGenerators;
    }

    public RegulatingControlMappingForTransformers forTransformers() {
        return regulatingControlMappingForTransformers;
    }

    public RegulatingControlMappingForShuntCompensators forShuntCompensators() {
        return regulatingControlMappingForShuntCompensators;
    }

    public RegulatingControlMappingForStaticVarCompensators forStaticVarCompensators() {
        return regulatingControlMappingForStaticVarCompensators;
    }

    public RegulatingControlMappingForVscConverters forVscConverters() {
        return regulatingControlMappingForVscConverters;
    }

    public static class RegulatingControl {
        final String mode;
        final String cgmesTerminal;
        final boolean enabled;
        final double targetValue;
        final double targetDeadband;
        private Boolean correctlySet;

        RegulatingControl(PropertyBag p) {
            this.mode = p.get("mode").toLowerCase();
            this.cgmesTerminal = p.getId(TERMINAL);
            this.enabled = p.asBoolean("enabled", true);
            this.targetValue = p.asDouble("targetValue");
            // targetDeadband is optional in CGMES,
            // If not explicitly given it should be interpreted as zero
            this.targetDeadband = p.asDouble("targetDeadband", 0);
        }

        void setCorrectlySet(boolean okSet) {
            if (okSet) {
                if (correctlySet != null && !correctlySet) {
                    return;
                }
                correctlySet = true;
            } else {
                correctlySet = false;
            }
        }

        public double getTargetValue() {
            return targetValue;
        }

        public double getTargetDeadBand() {
            return targetDeadband;
        }
    }

    private Map<String, RegulatingControl> cachedRegulatingControls = new HashMap<>();

    public Map<String, RegulatingControl> cachedRegulatingControls() {
        return cachedRegulatingControls;
    }

    void cacheRegulatingControls(PropertyBag p) {
        cachedRegulatingControls.put(p.getId(REGULATING_CONTROL), new RegulatingControl(p));
    }

    void setAllRegulatingControls(Network network) {
        regulatingControlMappingForGenerators.applyRegulatingControls(network);
        regulatingControlMappingForTransformers.applyTapChangersRegulatingControl(network);
        regulatingControlMappingForShuntCompensators.applyRegulatingControls(network);
        regulatingControlMappingForStaticVarCompensators.applyRegulatingControls(network);
        regulatingControlMappingForVscConverters.applyRegulatingControls(network);

        cachedRegulatingControls.forEach((key, value) -> {
            if (value.correctlySet == null || !value.correctlySet) {
                context.pending(REGULATING_TERMINAL,
                    () -> String.format("The setting of the regulating control %s is not entirely handled.", key));
            }
        });

        cachedRegulatingControls.clear();
    }

    Optional<Terminal> getRegulatingTerminalVoltageControl(String cgmesTerminalId) {
        if (cgmesTerminalId == null) {
            return Optional.empty();
        }

        CgmesTerminal cgmesTerminal = context.cgmes().terminal(cgmesTerminalId);
        if (cgmesTerminal != null && SwitchTerminal.isSwitch(cgmesTerminal.conductingEquipmentType())) {
            Switch sw = context.network().getSwitch(cgmesTerminal.conductingEquipment());
            if (sw == null) {
                Optional<Terminal> ot1 = Optional.ofNullable(context.terminalMapping().find(cgmesTerminalId));
                trace1(cgmesTerminalId, ot1, "SwNull", context);
                return ot1;
            }
            Optional<Terminal> ot = new SwitchTerminal(sw.getVoltageLevel(), sw, context.cgmes().isNodeBreaker()).getTerminalInTopologicalNode();
            trace1(cgmesTerminalId, ot, sw.getId(), context);
            return ot;
        }
        Optional<Terminal> ot = Optional.ofNullable(context.terminalMapping().find(cgmesTerminalId));
        trace1(cgmesTerminalId, ot, "NoSW", context);
        return ot;
    }

    // TODO delete trace1
    private static void trace1(String cgmesTerminalId, Optional<Terminal> ot, String tag, Context context) {
        if (ot.isPresent()) {
            System.err.printf("TRACE-T getRegulatingTerminalVoltageControl  cgmesTerminalId %s SwitchTag %s terminal %s Eq %s Type %s %n",
                cgmesTerminalId, tag, ot.get(), ot.get().getConnectable().getId(),
                ot.get().getConnectable().getType());
        } else {
            CgmesTerminal cgmesTerminal = context.cgmes().terminal(cgmesTerminalId);
            if (cgmesTerminal != null) {
                System.err.printf("TRACE-N getRegulatingTerminalVoltageControl  cgmesTerminalId %s SwitchTag %s terminal Null CEq %s Type %s %n",
                    cgmesTerminalId, tag, cgmesTerminal.conductingEquipment(), cgmesTerminal.conductingEquipmentType());
            } else {
                System.err.printf("TRACE-N getRegulatingTerminalVoltageControl  cgmesTerminalId %s SwitchTag %s terminal Null cgmesTerminal Null %n",
                    cgmesTerminalId, tag);
            }
        }
    }

    Optional<TerminalAndSign> getRegulatingTerminalFlowControl(String cgmesTerminalId) {
        if (cgmesTerminalId == null) {
            return Optional.empty();
        }

        CgmesTerminal cgmesTerminal = context.cgmes().terminal(cgmesTerminalId);
        if (cgmesTerminal != null && SwitchTerminal.isSwitch(cgmesTerminal.conductingEquipmentType())) {
            Switch sw = context.network().getSwitch(cgmesTerminal.conductingEquipment());
            if (sw == null) {
                Optional<Terminal> ot = Optional.ofNullable(context.terminalMapping().find(cgmesTerminalId));
                Optional<TerminalAndSign> ot1 =  ot.isPresent() ? Optional.of(new TerminalAndSign(ot.get(), 1)) : Optional.empty();
                trace2(cgmesTerminalId, ot1, "SwNull", context);
                return ot1;
            }
            Branch.Side side = sequenceNumberToSide(cgmesTerminal.getSequenceNumber());
            Optional<TerminalAndSign> ot = new SwitchTerminal(sw.getVoltageLevel(), sw, context.cgmes().isNodeBreaker()).getTerminalInSwitchesChain(side);
            trace2(cgmesTerminalId, ot, sw.getId(), context);
            return ot;
        }
        Optional<Terminal> ot = Optional.ofNullable(context.terminalMapping().find(cgmesTerminalId));
        Optional<TerminalAndSign> ot2 = ot.isPresent() ? Optional.of(new TerminalAndSign(ot.get(), 1)) : Optional.empty();
        trace2(cgmesTerminalId, ot2, "noSw", context);
        return ot2;
    }

    // TODO delete trace2
    private static void trace2(String cgmesTerminalId, Optional<TerminalAndSign> ot, String tag, Context context) {
        if (ot.isPresent()) {
            System.err.printf("TRACE-T getRegulatingTerminalFlowControl  cgmesTerminalId %s SwitchTag %s terminal %s sign %d Eq %s Type %s %n",
                cgmesTerminalId, tag, ot.get().getTerminal(), ot.get().getSign(), ot.get().getTerminal().getConnectable().getId(),
                ot.get().getTerminal().getConnectable().getType());
        } else {
            CgmesTerminal cgmesTerminal = context.cgmes().terminal(cgmesTerminalId);
            if (cgmesTerminal != null) {
                System.err.printf("TRACE-N getRegulatingTerminalFlowControl  cgmesTerminalId %s SwitchTag %s terminal Null CEq %s Type %s %n",
                    cgmesTerminalId, tag, cgmesTerminal.conductingEquipment(), cgmesTerminal.conductingEquipmentType());
            } else {
                System.err.printf("TRACE-N getRegulatingTerminalFlowControl  cgmesTerminalId %s SwitchTag %s terminal Null cgmesTerminal Null %n",
                    cgmesTerminalId, tag);
            }
        }
    }

    private static Branch.Side sequenceNumberToSide(int sequenceNumber) {
        if (sequenceNumber == 1) {
            return Branch.Side.ONE;
        } else if (sequenceNumber == 2) {
            return Branch.Side.TWO;
        } else {
            throw new PowsyblException(String.format("Unexpected sequenceNumber %d", sequenceNumber));
        }
    }

    // TODO delete
    Terminal findRegulatingTerminalDelete(String cgmesTerminalId) {
        return findRegulatingTerminalDelete(cgmesTerminalId, false);
    }

    Terminal findRegulatingTerminalDelete(String cgmesTerminalId, boolean canBeNull) {
        return Optional.ofNullable(context.terminalMapping().find(cgmesTerminalId)).filter(Terminal::isConnected)
                .orElseGet(() -> {
                    if (canBeNull) {
                        return null;
                    }
                    CgmesTerminal cgmesTerminal = context.cgmes().terminal(cgmesTerminalId);
                    if (cgmesTerminal != null) {
                        // Try to obtain Terminal from TopologicalNode
                        String topologicalNode = cgmesTerminal.topologicalNode();
                        context.invalid(REGULATING_TERMINAL, () -> String.format("No connected IIDM terminal has been found for CGMES terminal %s. " +
                            "A connected terminal linked to the topological node %s is searched.",
                            cgmesTerminalId, topologicalNode));
                        return context.terminalMapping().findFromTopologicalNode(topologicalNode);
                    } else {
                        context.invalid(REGULATING_TERMINAL, "No CGMES terminal found with identifier " + cgmesTerminalId);
                        return null;
                    }
                });
    }

    static boolean isControlModeVoltage(String controlMode) {
        return controlMode != null && controlMode.endsWith(VOLTAGE);
    }

    static boolean isControlModeReactivePower(String controlMode) {
        return controlMode != null && controlMode.toLowerCase().endsWith("reactivepower");
    }

    static String getRegulatingControlId(PropertyBag p) {
        return p.getId(REGULATING_CONTROL);
    }

    Terminal getRegulatingTerminalDelete(Injection injection, String cgmesTerminal) {
        // Will take default terminal ONLY if it has not been explicitly defined in CGMES
        // the default terminal is the local terminal
        Terminal terminal = injection.getTerminal();
        System.err.printf("getRegulatingTerminalJAM cgmesTerminal %s terminal %s %n", cgmesTerminal, terminal);
        if (cgmesTerminal != null) {
            terminal = findRegulatingTerminalDelete(cgmesTerminal);
            System.err.printf("getRegulatingTerminalJAM cgmesTerminal %s terminal find %s %n", cgmesTerminal, terminal);
            // If terminal is null here it means that no IIDM terminal has been found
            // from the initial CGMES terminal or topological node,
            // we will consider the regulating control invalid,
            // in this case we will not use the default terminal
            // (no localization of regulating controls)
        }
        return terminal;
    }
}
