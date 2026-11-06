/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.model.remedialaction;

import com.powsybl.nc.model.io.NcConstants;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public record NcRotatingMachineAction(String mrid, String rotatingMachineId, NcPropertyReference propertyReference, boolean enabled,
                                    String gridStateAlterationRemedialAction, String gridStateAlterationCollection) implements NcGridStateAlteration {
    public static NcRotatingMachineAction fromPropertyBag(PropertyBag propertyBag) {
        return new NcRotatingMachineAction(
            propertyBag.getId(NcConstants.ROTATING_MACHINE_ACTION),
            propertyBag.getId(NcConstants.ROTATING_MACHINE),
            NcPropertyReference.fromUri(propertyBag.get(NcConstants.GRID_ALTERATION_PROPERTY_REFERENCE)),
            Boolean.parseBoolean(propertyBag.getOrDefault(NcConstants.ENABLED, "true")),
            propertyBag.getId(NcConstants.REQUEST_GRID_STATE_ALTERATION_REMEDIAL_ACTION),
            propertyBag.getId(NcConstants.GRID_STATE_ALTERATION_COLLECTION)
        );
    }
}
