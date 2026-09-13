/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.model.assessedelement;

import com.powsybl.nc.model.NcIdentifiedObjectWithOperator;
import com.powsybl.nc.model.io.NcConstants;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public record NcAssessedElement(String mrid, boolean inBaseCase, String name, String operator, String conductingEquipment,
                              String operationalLimit, boolean isCombinableWithContingency,
                              boolean enabled, String securedForRegion, String scannedForRegion,
                              double flowReliabilityMargin,
                              String overlappingZone) implements NcIdentifiedObjectWithOperator {
    public static NcAssessedElement fromPropertyBag(PropertyBag propertyBag) {
        return new NcAssessedElement(
            propertyBag.getId(NcConstants.REQUEST_ASSESSED_ELEMENT),
            Boolean.parseBoolean(propertyBag.get(NcConstants.REQUEST_ASSESSED_ELEMENT_IN_BASE_CASE)),
            propertyBag.get(NcConstants.REQUEST_ASSESSED_ELEMENT_NAME),
            propertyBag.get(NcConstants.REQUEST_ASSESSED_ELEMENT_OPERATOR),
            propertyBag.getId(NcConstants.REQUEST_ASSESSED_ELEMENT_CONDUCTING_EQUIPMENT),
            propertyBag.getId(NcConstants.REQUEST_ASSESSED_ELEMENT_OPERATIONAL_LIMIT),
            Boolean.parseBoolean(propertyBag.getOrDefault(NcConstants.REQUEST_ASSESSED_ELEMENT_IS_COMBINABLE_WITH_CONTINGENCY, "true")),
            Boolean.parseBoolean(propertyBag.getOrDefault(NcConstants.ENABLED, "true")),
            propertyBag.get(NcConstants.REQUEST_ASSESSED_ELEMENT_SECURED_FOR_REGION),
            propertyBag.get(NcConstants.REQUEST_ASSESSED_ELEMENT_SCANNED_FOR_REGION),
            propertyBag.get(NcConstants.REQUEST_FLOW_RELIABILITY_MARGIN) == null ? 0d : Double.parseDouble(propertyBag.get(NcConstants.REQUEST_FLOW_RELIABILITY_MARGIN)),
            propertyBag.get(NcConstants.OVERLAPPING_ZONE));
    }
}
