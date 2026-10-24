/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
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
public record NcAssessedElementWithContingency(String mrid, String assessedElement, String contingency, String combinationConstraintKind, boolean enabled) implements NcAssociation {
    public static NcAssessedElementWithContingency fromPropertyBag(PropertyBag propertyBag) {
        return new NcAssessedElementWithContingency(
            propertyBag.getId(NcConstants.REQUEST_ASSESSED_ELEMENT_WITH_CONTINGENCY),
            propertyBag.getId(NcConstants.REQUEST_ASSESSED_ELEMENT),
            propertyBag.getId(NcConstants.REQUEST_CONTINGENCY),
            propertyBag.get(NcConstants.COMBINATION_CONSTRAINT_KIND),
            Boolean.parseBoolean(propertyBag.getOrDefault(NcConstants.ENABLED, "true"))
        );
    }
}
