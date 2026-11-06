/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.model.assessedelement;

import com.powsybl.nc.model.NcAssociation;
import com.powsybl.nc.model.io.NcConstants;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public record NcAssessedElementWithRemedialAction(String mrid, String assessedElement, String remedialAction, String combinationConstraintKind, boolean enabled) implements NcAssociation {
    public static NcAssessedElementWithRemedialAction fromPropertyBag(PropertyBag propertyBag) {
        return new NcAssessedElementWithRemedialAction(
            propertyBag.getId(NcConstants.REQUEST_ASSESSED_ELEMENT_WITH_REMEDIAL_ACTION),
            propertyBag.getId(NcConstants.REQUEST_ASSESSED_ELEMENT),
            propertyBag.getId(NcConstants.REQUEST_REMEDIAL_ACTION),
            propertyBag.get(NcConstants.COMBINATION_CONSTRAINT_KIND),
            Boolean.parseBoolean(propertyBag.getOrDefault(NcConstants.ENABLED, "true"))
        );
    }
}
