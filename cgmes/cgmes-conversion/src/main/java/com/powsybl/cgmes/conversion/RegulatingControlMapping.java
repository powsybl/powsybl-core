/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class RegulatingControlMapping {

    private static final String REGULATING_CONTROL = "RegulatingControl";
    private static final String TAP_CHANGER_CONTROL = "TapChangerControl";
    private static final String TERMINAL = "Terminal";

    class RegulatingControl {
        private String mode;
        private String cgmesTerminal;
        private String topologicalNode;
        private boolean enabled;
        private double targetValue;

        RegulatingControl(PropertyBag p) {
            this.mode = p.get("mode").toLowerCase();
            this.cgmesTerminal = p.getId(TERMINAL);
            this.topologicalNode = p.getId("topologicalNode");
            this.enabled = p.asBoolean("enabled", true);
            this.targetValue = p.asDouble("targetValue");
        }
    }

    private Map<String, RegulatingControl> regulatingControlMapping = new HashMap<>();

    public void addRegulatingControl(PropertyBag p) {
        regulatingControlMapping.put(p.getId(REGULATING_CONTROL), new RegulatingControl(p));
    }

    public void setRegulatingControl(String idEq, PropertyBag p, GeneratorAdder adder, VoltageLevel vl, Context context) {
        if (p.containsKey(REGULATING_CONTROL)) {
            String controlId = p.getId(REGULATING_CONTROL);
            RegulatingControl control = regulatingControlMapping.get(controlId);
            if (control != null) {
                if (control.mode != null && control.mode.endsWith("voltage")) {
                    setTargetValue(control.targetValue, vl.getNominalV(), controlId, adder, context);
                    adder.setVoltageRegulatorOn(control.enabled);
                    setRegulatingTerminal(control, p.getId(TERMINAL), idEq, controlId, adder, context);
                    return;
                }
            } else {
                context.missing(String.format("Regulating control %s for equipment %s", controlId, idEq));
            }
        }
        adder.setVoltageRegulatorOn(false)
                .setTargetV(Double.NaN);
    }

    public void setRegulatingControl(PropertyBag p, Terminal defaultTerminal, RatioTapChangerAdder adder, Context context) {
        if (p.containsKey(TAP_CHANGER_CONTROL)) {
            String controlId = p.getId(TAP_CHANGER_CONTROL);
            RegulatingControl control = regulatingControlMapping.get(controlId);
            if (control != null) {
                if (control.mode.endsWith("voltage")) {
                    addRegulatingControlVoltage(p, control, adder, defaultTerminal, context);
                    return;
                } else if (!control.mode.endsWith("fixed")) {
                    context.ignored(control.mode, "Unsupported regulation mode");
                }
            } else {
                context.missing(String.format("Regulating control %s", controlId));
            }
        }
        adder.setLoadTapChangingCapabilities(false);
    }

    private void addRegulatingControlVoltage(PropertyBag p, RegulatingControl control, RatioTapChangerAdder adder, Terminal defaultTerminal, Context context) {
        // Even if regulating is false, we reset the target voltage if it is not valid
        if (control.targetValue <= 0) {
            context.ignored(p.getId(TAP_CHANGER_CONTROL), String.format("Regulating control has a bad target voltage %f", control.targetValue));
            adder.setRegulating(false)
                    .setTargetV(Double.NaN);
        } else {
            adder.setRegulating(control.enabled)
                    .setTargetV(control.targetValue);
        }
        adder.setLoadTapChangingCapabilities(true);
        setRegulatingTerminal(p, control, defaultTerminal, adder, context);
    }

    private static void setTargetValue(double targetValue, double defaultValue, String controlId, GeneratorAdder adder, Context context) {
        if (targetValue == 0 || Double.isNaN(targetValue)) {
            context.fixed(controlId, "Invalid value for regulating target value",
                    targetValue, defaultValue);
            adder.setTargetV(defaultValue);
        } else {
            adder.setTargetV(targetValue);
        }
    }

    private void setRegulatingTerminal(RegulatingControl control, String terminalEq, String idEq, String controlId, GeneratorAdder adder, Context context) {
        if (context.terminalMapping().find(control.cgmesTerminal) != null) {
            adder.setRegulatingTerminal(context.terminalMapping().find(control.cgmesTerminal));
        } else if (!context.terminalMapping().areAssociated(terminalEq, control.topologicalNode)) {
            context.putRemoteRegulatingTerminal(idEq, control.topologicalNode);
            return;
        }
        regulatingControlMapping.remove(controlId);
    }

    private void setRegulatingTerminal(PropertyBag p, RegulatingControl control, Terminal defaultTerminal, RatioTapChangerAdder adder, Context context) {
        if (context.terminalMapping().find(control.cgmesTerminal) != null) {
            adder.setRegulationTerminal(context.terminalMapping().find(control.cgmesTerminal));
        } else {
            adder.setRegulationTerminal(defaultTerminal);
            if (!context.terminalMapping().areAssociated(p.getId(TERMINAL), control.topologicalNode)) {
                context.putRemoteRegulatingTerminal(p.getId("RatioTapChanger"), control.topologicalNode);
                return;
            }
        }
        regulatingControlMapping.remove(p.getId(TAP_CHANGER_CONTROL));
    }
}
