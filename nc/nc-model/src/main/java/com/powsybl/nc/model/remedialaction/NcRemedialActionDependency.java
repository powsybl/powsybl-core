/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.model.remedialaction;

import com.powsybl.nc.model.NcObject;
import com.powsybl.nc.model.io.NcConstants;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public record NcRemedialActionDependency(String mrid, String kind, String remedialAction, String dependingRemedialActionGroup, boolean enabled) implements NcObject {
    public static NcRemedialActionDependency fromPropertyBag(PropertyBag propertyBag) {
        return new NcRemedialActionDependency(
            propertyBag.getId(NcConstants.REQUEST_REMEDIAL_ACTION_DEPENDENCY),
            propertyBag.get(NcConstants.KIND),
            propertyBag.getId(NcConstants.REQUEST_REMEDIAL_ACTION),
            propertyBag.getId(NcConstants.DEPENDING_REMEDIAL_ACTION_GROUP),
            Boolean.parseBoolean(propertyBag.getOrDefault(NcConstants.ENABLED, "true"))
        );
    }
}
