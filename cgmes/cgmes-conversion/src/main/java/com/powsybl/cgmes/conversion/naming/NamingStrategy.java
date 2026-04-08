/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.naming;

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

    /**
     * Get the Name of the implemented NamingStrategy.
     */
    String getName();

    /**
     * Get the IIDM id for the given CGMES type and id.
     */
    String getIidmId(String type, String id);

    /**
     * Get the IIDM name for the given CGMES type and id.
     */
    String getIidmName(String type, String name);

    /**
     * Get the CGMES id for the given IIDM identifiable.
     * All implementations must ensure a deterministic id generation.
     */
    String getCgmesId(Identifiable<?> identifiable);

    /**
     * Get the CGMES id for the given array of references.
     * All implementations must ensure a deterministic id generation.
     */
    String getCgmesId(CgmesObjectReference... refs);

    /**
     * Get the CGMES id for the given identifier.
     * All implementations must ensure a deterministic id generation.
     */
    String getCgmesId(String identifier);

    /**
     * Get the CGMES id for the given IIDM identifiable and alias type.
     * All implementations must ensure a deterministic id generation.
     */
    String getCgmesIdFromAlias(Identifiable<?> identifiable, String aliasType);

    /**
     * Get the CGMES id for the given IIDM identifiable and propertyName.
     * All implementations must ensure a deterministic id generation.
     */
    String getCgmesIdFromProperty(Identifiable<?> identifiable, String propertyName);

    /**
     * Get an array of references for the given IIDM identifiable and alias type or property name.
     * The array can then be used to generate a unique identifier in a deterministic way.
     */
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
        if (identifiable instanceof BoundaryLine) {
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
