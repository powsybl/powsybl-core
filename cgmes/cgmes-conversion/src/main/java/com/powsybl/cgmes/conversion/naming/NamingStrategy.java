/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.naming;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Identifiable;

import static com.powsybl.cgmes.conversion.Conversion.*;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.*;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.Part.*;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.ref;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public interface NamingStrategy {

    String getName();

    String getIidmId(String type, String id);

    String getIidmName(String type, String name);

    String getCgmesId(Identifiable<?> identifiable);

    String getCgmesId(CgmesObjectReference... refs);

    String getCgmesId(String identifier);

    String getCgmesIdFromAlias(Identifiable<?> identifiable, String aliasType);

    String getCgmesIdFromProperty(Identifiable<?> identifiable, String propertyName);

    void debug(String baseName, DataSource ds);

    default CgmesObjectReference[] getCgmesObjectReferences(Identifiable<?> identifiable, String aliasOrProperty) {
        return switch (aliasOrProperty) {
            case ALIAS_DC_TERMINAL1 -> new CgmesObjectReference[] {refTyped(identifiable), refDcTerminal(identifiable), ref(1)};
            case ALIAS_DC_TERMINAL2 -> new CgmesObjectReference[] {refTyped(identifiable), refDcTerminal(identifiable), ref(2)};
            case ALIAS_PHASE_TAP_CHANGER1 -> new CgmesObjectReference[] {refTyped(identifiable), PHASE_TAP_CHANGER, ref(1)};
            case ALIAS_PHASE_TAP_CHANGER2 -> new CgmesObjectReference[] {refTyped(identifiable), PHASE_TAP_CHANGER, ref(2)};
            case ALIAS_PHASE_TAP_CHANGER3 -> new CgmesObjectReference[] {refTyped(identifiable), PHASE_TAP_CHANGER, ref(3)};
            case ALIAS_RATIO_TAP_CHANGER1 -> new CgmesObjectReference[] {refTyped(identifiable), RATIO_TAP_CHANGER, ref(1)};
            case ALIAS_RATIO_TAP_CHANGER2 -> new CgmesObjectReference[] {refTyped(identifiable), RATIO_TAP_CHANGER, ref(2)};
            case ALIAS_RATIO_TAP_CHANGER3 -> new CgmesObjectReference[] {refTyped(identifiable), RATIO_TAP_CHANGER, ref(3)};
            case ALIAS_TERMINAL_BOUNDARY -> new CgmesObjectReference[] {refTyped(identifiable), BOUNDARY_TERMINAL};
            case ALIAS_TERMINAL1 -> getTerminal1References(identifiable);
            case ALIAS_TERMINAL2 -> new CgmesObjectReference[] {refTyped(identifiable), TERMINAL, ref(2)};
            case ALIAS_TERMINAL3 -> new CgmesObjectReference[] {refTyped(identifiable), TERMINAL, ref(3)};
            case ALIAS_TRANSFORMER_END1 -> new CgmesObjectReference[] {ref(identifiable), combo(TRANSFORMER_END, ref(1))};
            case ALIAS_TRANSFORMER_END2 -> new CgmesObjectReference[] {ref(identifiable), combo(TRANSFORMER_END, ref(2))};
            case ALIAS_TRANSFORMER_END3 -> new CgmesObjectReference[] {ref(identifiable), combo(TRANSFORMER_END, ref(3))};
            case PROPERTY_EQUIVALENT_INJECTION -> new CgmesObjectReference[] {refTyped(identifiable), EQUIVALENT_INJECTION};
            case PROPERTY_EQUIVALENT_INJECTION_TERMINAL -> new CgmesObjectReference[] {refTyped(identifiable), EQUIVALENT_INJECTION, TERMINAL};
            case PROPERTY_GENERATING_UNIT -> getGeneratingUnitReferences(identifiable);
            case PROPERTY_REGULATING_CONTROL -> new CgmesObjectReference[] {ref(identifiable), REGULATING_CONTROL};
            case PROPERTY_TOPOLOGICAL_NODE_BOUNDARY -> new CgmesObjectReference[] {refTyped(identifiable), TOPOLOGICAL_NODE};
            default -> throw new IllegalStateException("Unexpected value: " + aliasOrProperty);
        };
    }

    private CgmesObjectReference[] getTerminal1References(Identifiable<?> identifiable) {
        if (identifiable instanceof DanglingLine) {
            return new CgmesObjectReference[] {refTyped(identifiable), TERMINAL};
        }
        return new CgmesObjectReference[] {refTyped(identifiable), TERMINAL, ref(1)};
    }

    private CgmesObjectReference[] getGeneratingUnitReferences(Identifiable<?> identifiable) {
        if (identifiable instanceof Generator generator) {
            return new CgmesObjectReference[]{ref(generator), refGeneratingUnit(generator)};
        }
        return new CgmesObjectReference[]{refTyped(identifiable), Part.GENERATING_UNIT};
    }
}
