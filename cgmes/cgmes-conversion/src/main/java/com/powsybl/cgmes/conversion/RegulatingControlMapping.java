/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBag;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class RegulatingControlMapping {

    static final String MISSING_IIDM_TERMINAL = "IIDM terminal for this CGMES terminal: %s";

    private final RegulatingControlMappingForGenerators regulatingControlMappingForGenerators;
    private final RegulatingControlMappingForTransformers regulatingControlMappingForTransformers;
    private final RegulatingControlMappingForShuntCompensators regulatingControlMappingForShuntCompensators;
    private final RegulatingControlMappingForStaticVarCompensators regulatingControlMappingForStaticVarCompensators;
    private final RegulatingControlMappingForVscConverters regulatingControlMappingForVscConverters;
    private final Map<String, RegulatingControl> cachedRegulatingControls = new HashMap<>();

    RegulatingControlMapping(Context context) {
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

    public RegulatingControl fetchRegulatingControl(String regulatingControlId) {
        return cachedRegulatingControls.get(regulatingControlId);
    }

    void storeRegulatingControl(PropertyBag p) {
        String regulatingControlId = p.getId(CgmesNames.REGULATING_CONTROL);
        cachedRegulatingControls.put(regulatingControlId, new RegulatingControl(p));
    }

    void setAllRegulatingControls(Network network) {
        regulatingControlMappingForGenerators.applyRegulatingControls(network);
        regulatingControlMappingForTransformers.applyTapChangersRegulatingControl(network);
        regulatingControlMappingForShuntCompensators.applyRegulatingControls(network);
        regulatingControlMappingForStaticVarCompensators.applyRegulatingControls(network);
        regulatingControlMappingForVscConverters.applyRegulatingControls(network);
        cachedRegulatingControls.clear();
    }

    public static boolean isControlModeVoltage(String controlMode) {
        return controlMode != null && controlMode.endsWith(CgmesNames.VOLTAGE_TAG);
    }

    public static boolean isControlModeReactivePower(String controlMode) {
        return controlMode != null && controlMode.toLowerCase(Locale.ROOT).endsWith(CgmesNames.REACTIVE_POWER);
    }

    protected static String getRegulatingControlId(PropertyBag p) {
        return p.getId(CgmesNames.REGULATING_CONTROL);
    }

    public record RegulatingControl(String mode, String terminalId) {

        public RegulatingControl(PropertyBag p) {
            this(p.get("mode").toLowerCase(Locale.ROOT), p.getId(CgmesNames.TERMINAL));
        }
    }
}
