/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;
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

    RegulatingControlMapping(Context context) {
        this.context = context;
        regulatingControlMappingForGenerators = new RegulatingControlMappingForGenerators(this, context);
        regulatingControlMappingForTransformers = new RegulatingControlMappingForTransformers(this, context);
        regulatingControlMappingForStaticVarCompensators = new RegulatingControlMappingForStaticVarCompensators(this, context);
        regulatingControlMappingForShuntCompensators = new RegulatingControlMappingForShuntCompensators(this, context);
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

    static class RegulatingControl {
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

    }

    private Map<String, RegulatingControl> cachedRegulatingControls = new HashMap<>();

    Map<String, RegulatingControl> cachedRegulatingControls() {
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

        cachedRegulatingControls.forEach((key, value) -> {
            if (value.correctlySet == null || !value.correctlySet) {
                context.pending(REGULATING_TERMINAL,
                    () -> String.format("The setting of the regulating control %s is not entirely handled.", key));
            }
        });

        cachedRegulatingControls.clear();
    }

    Terminal findRegulatingTerminal(String cgmesTerminalId) {
        return Optional.ofNullable(context.terminalMapping().find(cgmesTerminalId)).filter(Terminal::isConnected)
                .orElseGet(() -> {
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

    static String getRegulatingControlId(PropertyBag p) {
        return p.getId(REGULATING_CONTROL);
    }

    Terminal getRegulatingTerminal(Injection injection, String cgmesTerminal) {
        // Will take default terminal ONLY if it has not been explicitly defined in CGMES
        // the default terminal is the local terminal
        Terminal terminal = injection.getTerminal();
        if (cgmesTerminal != null) {
            terminal = findRegulatingTerminal(cgmesTerminal);
            // If terminal is null here it means that no IIDM terminal has been found
            // from the initial CGMES terminal or topological node,
            // we will consider the regulating control invalid,
            // in this case we will not use the default terminal
            // (no localization of regulating controls)
        }
        return terminal;
    }
}
