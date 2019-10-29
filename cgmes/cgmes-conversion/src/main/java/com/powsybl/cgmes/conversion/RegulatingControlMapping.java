/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class RegulatingControlMapping {

    static final String REGULATING_CONTROL = "RegulatingControl";
    static final String TAP_CHANGER_CONTROL = "TapChangerControl";
    private static final String TERMINAL = "Terminal";
    static final String MISSING_IIDM_TERMINAL = "IIDM terminal for this CGMES topological node: %s";
    private static final String VOLTAGE = "voltage";

    private final Context context;
    private final RegulatingControlMappingForGenerators regulatingControlMappingForGenerators;
    private final RegulatingControlMappingForTransformers regulatingControlMappingForTransformers;
    private final RegulatingControlMappingForStaticVarCompensators regulatingControlMappingForStaticVarCompensators;

    public RegulatingControlMapping(Context context) {
        this.context = context;
        regulatingControlMappingForGenerators = new RegulatingControlMappingForGenerators(this);
        regulatingControlMappingForTransformers = new RegulatingControlMappingForTransformers(this);
        regulatingControlMappingForStaticVarCompensators = new RegulatingControlMappingForStaticVarCompensators(this);
    }

    public RegulatingControlMappingForGenerators forGenerators() {
        return regulatingControlMappingForGenerators;
    }

    public RegulatingControlMappingForTransformers forTransformers() {
        return regulatingControlMappingForTransformers;
    }

    public RegulatingControlMappingForStaticVarCompensators forStaticVarCompensators() {
        return regulatingControlMappingForStaticVarCompensators;
    }

    public Context context() {
        return context;
    }

    static class RegulatingControl {
        final String mode;
        final String cgmesTerminal;
        final String topologicalNode;
        final boolean enabled;
        final double targetValue;
        final double targetDeadband;
        private Boolean correctlySet = false;

        RegulatingControl(PropertyBag p) {
            this.mode = p.get("mode").toLowerCase();
            this.cgmesTerminal = p.getId(TERMINAL);
            this.topologicalNode = p.getId("topologicalNode");
            this.enabled = p.asBoolean("enabled", true);
            this.targetValue = p.asDouble("targetValue");
            this.targetDeadband = p.asDouble("targetDeadband", Double.NaN);
        }

        void hasCorrectlySet() {
            correctlySet = true;
        }
    }

    private Map<String, RegulatingControl> cachedRegulatingControls = new HashMap<>();

    Map<String, RegulatingControl> cachedRegulatingControls() {
        return cachedRegulatingControls;
    }

    void cacheRegulatingControls(PropertyBag p) {
        cachedRegulatingControls.put(p.getId(REGULATING_CONTROL), new RegulatingControl(p));
    }

    public void setAllRegulatingControls(Network network) {
        regulatingControlMappingForGenerators.applyRegulatingControls(network);
        regulatingControlMappingForTransformers.applyTapChangersRegulatingControl(network);
        regulatingControlMappingForStaticVarCompensators.applyRegulatingControls(network);

        cachedRegulatingControls.entrySet().removeIf(entry -> {
            if (entry.getValue().correctlySet) {
                return true;
            }
            return false;
        });

        cachedRegulatingControls.forEach((key, value) -> context.pending("Regulating terminal",
                String.format("The setting of the regulating terminal of the regulating control %s is not entirely handled.", key)));
        cachedRegulatingControls.clear();
    }

    public Terminal findRegulatingTerminal(String cgmesTerminal, String topologicalNode) {
        return Optional.ofNullable(context.terminalMapping().find(cgmesTerminal))
                .orElseGet(() -> context.terminalMapping().findFromTopologicalNode(topologicalNode));
    }

    static boolean isControlModeVoltage(String controlMode) {
        return controlMode != null && controlMode.endsWith(VOLTAGE);
    }
}
