/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.model.contingency;

import com.powsybl.nc.model.NcAssociation;
import com.powsybl.nc.model.io.NcConstants;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public record NcContingencyWithRemedialAction(String mrid, String contingency, String remedialAction, String combinationConstraintKind, boolean enabled) implements NcAssociation {
    public static NcContingencyWithRemedialAction fromPropertyBag(PropertyBag propertyBag) {
        return new NcContingencyWithRemedialAction(
            propertyBag.getId(NcConstants.REQUEST_CONTINGENCY_WITH_REMEDIAL_ACTION),
            propertyBag.getId(NcConstants.REQUEST_CONTINGENCY),
            propertyBag.getId(NcConstants.REQUEST_REMEDIAL_ACTION),
            propertyBag.get(NcConstants.COMBINATION_CONSTRAINT_KIND),
            Boolean.parseBoolean(propertyBag.getOrDefault(NcConstants.ENABLED, "true"))
        );
    }
}
