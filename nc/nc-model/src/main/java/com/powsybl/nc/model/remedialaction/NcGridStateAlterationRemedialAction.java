/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.model.remedialaction;

import com.powsybl.nc.model.NcIdentifiedObjectWithOperator;
import com.powsybl.nc.model.io.NcConstants;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public record NcGridStateAlterationRemedialAction(String mrid, String name, String operator, String kind, boolean available,
                                                String timeToImplement, boolean isManual) implements NcIdentifiedObjectWithOperator {
    public static NcGridStateAlterationRemedialAction fromPropertyBag(PropertyBag propertyBag) {
        return new NcGridStateAlterationRemedialAction(
            propertyBag.getId(NcConstants.GRID_STATE_ALTERATION_REMEDIAL_ACTION),
            propertyBag.get(NcConstants.REMEDIAL_ACTION_NAME),
            propertyBag.get(NcConstants.TSO),
            propertyBag.get(NcConstants.KIND),
            Boolean.parseBoolean(propertyBag.get(NcConstants.AVAILABLE)),
            propertyBag.get(NcConstants.TIME_TO_IMPLEMENT),
            Boolean.parseBoolean(propertyBag.getOrDefault(NcConstants.IS_MANUAL, "true"))
        );
    }
}
