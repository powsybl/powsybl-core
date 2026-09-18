/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.model.io;

import java.util.List;

/**
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
public enum NcOverridingQuery {
    CONTINGENCY("contingencyOverriding", NcOverridingObjectsFields.CONTINGENCY),
    ASSESSED_ELEMENT("assessedElementOverriding", NcOverridingObjectsFields.ASSESSED_ELEMENT),
    ASSESSED_ELEMENT_WITH_CONTINGENCY("assessedElementWithContingencyOverriding", NcOverridingObjectsFields.ASSESSED_ELEMENT_WITH_CONTINGENCY),
    ASSESSED_ELEMENT_WITH_REMEDIAL_ACTION("assessedElementWithRemedialActionOverriding", NcOverridingObjectsFields.ASSESSED_ELEMENT_WITH_REMEDIAL_ACTION),
    CONTINGENCY_WITH_REMEDIAL_ACTION("contingencyWithRemedialActionOverriding", NcOverridingObjectsFields.CONTINGENCY_WITH_REMEDIAL_ACTION),
    GRID_STATE_ALTERATION_REMEDIAL_ACTION("gridStateAlterationRemedialActionOverriding", NcOverridingObjectsFields.GRID_STATE_ALTERATION_REMEDIAL_ACTION),
    GRID_STATE_ALTERATION("gridStateAlterationOverriding", NcOverridingObjectsFields.GRID_STATE_ALTERATION),
    STATIC_PROPERTY_RANGE("staticPropertyRangeOverriding", NcOverridingObjectsFields.STATIC_PROPERTY_RANGE),
    REMEDIAL_ACTION_SCHEME("remedialActionSchemeOverriding", NcOverridingObjectsFields.REMEDIAL_ACTION_SCHEME),
    TOPOLOGY_ACTION("topologyActionOverriding", NcOverridingObjectsFields.TOPOLOGY_ACTION),
    ROTATING_MACHINE_ACTION("rotatingMachineActionOverriding", NcOverridingObjectsFields.ROTATING_MACHINE_ACTION),
    SHUNT_COMPENSATOR_MODIFICATION("shuntCompensatorModificationOverriding", NcOverridingObjectsFields.SHUNT_COMPENSATOR_MODIFICATION),
    TAP_POSITION_ACTION("tapPositionActionOverriding", NcOverridingObjectsFields.TAP_POSITION_ACTION),
    SCHEME_REMEDIAL_ACTION("schemeRemedialActionOverriding", NcOverridingObjectsFields.SCHEME_REMEDIAL_ACTION),
    SCHEME_REMEDIAL_ACTION_DEPENDENCY("remedialActionDependencyOverriding", NcOverridingObjectsFields.SCHEME_REMEDIAL_ACTION_DEPENDENCY);

    private final String requestName;
    private final List<NcOverridingObjectsFields> fields;

    NcOverridingQuery(String requestName, NcOverridingObjectsFields... fields) {
        this.requestName = requestName;
        this.fields = List.of(fields);
    }

    public String getRequestName() {
        return requestName;
    }

    public List<NcOverridingObjectsFields> getFields() {
        return fields;
    }
}
