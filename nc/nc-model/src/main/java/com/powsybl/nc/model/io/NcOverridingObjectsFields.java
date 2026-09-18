/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.model.io;

/**
 * @author Jean-Pierre Arnould {@literal <jean-pierre.arnould at rte-france.com>}
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public enum NcOverridingObjectsFields {
    CONTINGENCY(NcConstants.REQUEST_CONTINGENCY, NcConstants.REQUEST_CONTINGENCIES_NORMAL_MUST_STUDY, NcConstants.MUST_STUDY),
    ASSESSED_ELEMENT(NcConstants.REQUEST_ASSESSED_ELEMENT, NcConstants.NORMAL_ENABLED, NcConstants.ENABLED),
    ASSESSED_ELEMENT_WITH_CONTINGENCY(NcConstants.REQUEST_ASSESSED_ELEMENT_WITH_CONTINGENCY, NcConstants.NORMAL_ENABLED, NcConstants.ENABLED),
    ASSESSED_ELEMENT_WITH_REMEDIAL_ACTION(NcConstants.REQUEST_ASSESSED_ELEMENT_WITH_REMEDIAL_ACTION, NcConstants.NORMAL_ENABLED, NcConstants.ENABLED),
    CONTINGENCY_WITH_REMEDIAL_ACTION(NcConstants.REQUEST_CONTINGENCY_WITH_REMEDIAL_ACTION, NcConstants.NORMAL_ENABLED, NcConstants.ENABLED),
    GRID_STATE_ALTERATION_REMEDIAL_ACTION(NcConstants.REQUEST_GRID_STATE_ALTERATION_REMEDIAL_ACTION, NcConstants.NORMAL_AVAILABLE, NcConstants.AVAILABLE),
    GRID_STATE_ALTERATION(NcConstants.GRID_STATE_ALTERATION, NcConstants.NORMAL_ENABLED, NcConstants.ENABLED),
    STATIC_PROPERTY_RANGE(NcConstants.STATIC_PROPERTY_RANGE, NcConstants.NORMAL_VALUE, NcConstants.VALUE),
    REMEDIAL_ACTION_SCHEME(NcConstants.REMEDIAL_ACTION_SCHEME, NcConstants.NORMAL_ARMED, NcConstants.ARMED),
    TOPOLOGY_ACTION(NcConstants.REQUEST_TOPOLOGY_ACTION, NcConstants.NORMAL_ENABLED, NcConstants.ENABLED),
    ROTATING_MACHINE_ACTION(NcConstants.REQUEST_ROTATING_MACHINE_ACTION, NcConstants.NORMAL_ENABLED, NcConstants.ENABLED),
    SHUNT_COMPENSATOR_MODIFICATION(NcConstants.REQUEST_SHUNT_COMPENSATOR_MODIFICATION, NcConstants.NORMAL_ENABLED, NcConstants.ENABLED),
    TAP_POSITION_ACTION(NcConstants.REQUEST_TAP_POSITION_ACTION, NcConstants.NORMAL_ENABLED, NcConstants.ENABLED),
    SCHEME_REMEDIAL_ACTION(NcConstants.REQUEST_SCHEME_REMEDIAL_ACTION, NcConstants.NORMAL_AVAILABLE, NcConstants.AVAILABLE),
    SCHEME_REMEDIAL_ACTION_DEPENDENCY(NcConstants.REQUEST_REMEDIAL_ACTION_DEPENDENCY, NcConstants.NORMAL_ENABLED, NcConstants.ENABLED);

    final String objectName;
    final String baselineFieldName;
    final String effectiveFieldName;

    NcOverridingObjectsFields(String objectName, String baselineFieldName, String effectiveFieldName) {
        this.objectName = objectName;
        this.baselineFieldName = baselineFieldName;
        this.effectiveFieldName = effectiveFieldName;
    }

    public String getObjectName() {
        return this.objectName;
    }

    public String getBaselineFieldName() {
        return this.baselineFieldName;
    }

    public String getEffectiveFieldName() {
        return this.effectiveFieldName;
    }
}
