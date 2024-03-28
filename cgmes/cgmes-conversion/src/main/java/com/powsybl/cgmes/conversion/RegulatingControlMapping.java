/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
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
        regulatingControlMappingForVscConverters = new RegulatingControlMappingForVscConverters(context);
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

    static boolean isControlModeVoltage(String controlMode) {
        return controlMode != null && controlMode.endsWith(VOLTAGE);
    }

    static boolean isControlModeReactivePower(String controlMode) {
        return controlMode != null && controlMode.toLowerCase().endsWith("reactivepower");
    }

    static String getRegulatingControlId(PropertyBag p) {
        return p.getId(REGULATING_CONTROL);
    }
}
