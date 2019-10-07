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
    static final String TERMINAL = "Terminal";
    static final String MISSING_IIDM_TERMINAL = "IIDM terminal for this CGMES topological node: %s";
    static final String VOLTAGE = "voltage";
    static final String REGULATING_CONTROL_REF = "Regulating control %s";
    static final String TAP_CHANGER_CONTROL_ENABLED = "tapChangerControlEnabled";
    static final String PHASE_TAP_CHANGER = "PhaseTapChanger";
    static final String QPERCENT = "qPercent";

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

        RegulatingControl(PropertyBag p) {
            this.mode = p.get("mode").toLowerCase();
            this.cgmesTerminal = p.getId(TERMINAL);
            this.topologicalNode = p.getId("topologicalNode");
            this.enabled = p.asBoolean("enabled", true);
            this.targetValue = p.asDouble("targetValue");
            this.targetDeadband = p.asDouble("targetDeadband", Double.NaN);
        }
    }

    private Map<String, RegulatingControl> cachedRegulatingControls = new HashMap<>();

    public Map<String, RegulatingControl> cachedRegulatingControls() {
        return cachedRegulatingControls;
    }

    public void cacheRegulatingControls(PropertyBag p) {
        cachedRegulatingControls.put(p.getId(REGULATING_CONTROL), new RegulatingControl(p));
    }

    public void setAllRegulatingControls(Network network) {
        regulatingControlMappingForGenerators.apply(network);
        regulatingControlMappingForTransformers.applyTwoWindings(network);
        regulatingControlMappingForTransformers.applyThreeWindings(network);
        regulatingControlMappingForStaticVarCompensators.apply(network);

        cachedRegulatingControls.clear();
    }

    public Terminal findRegulatingTerminal(String cgmesTerminal, String topologicalNode) {
        return Optional.ofNullable(context.terminalMapping().find(cgmesTerminal))
            .orElseGet(() -> context.terminalMapping().findFromTopologicalNode(topologicalNode));
    }

    public double terminalNominalVoltage(Terminal terminal) {
        return terminal.getVoltageLevel().getNominalV();
    }

    public boolean isControlModeVoltage(String controlMode) {
        if (controlMode != null && controlMode.endsWith("voltage")) {
            return true;
        }
        return false;
    }
}
