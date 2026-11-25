/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.model.contingency;

import com.powsybl.nc.model.NcIdentifiedObjectWithOperator;
import com.powsybl.nc.model.io.NcConstants;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public record NcContingency(String mrid, boolean mustStudy, String name, String operator) implements NcIdentifiedObjectWithOperator {
    public static NcContingency fromPropertyBag(PropertyBag propertyBag) {
        return new NcContingency(
            propertyBag.getId(NcConstants.REQUEST_CONTINGENCY),
            Boolean.parseBoolean(propertyBag.get(NcConstants.MUST_STUDY)),
            propertyBag.getId(NcConstants.REQUEST_CONTINGENCIES_NAME),
            propertyBag.get(NcConstants.REQUEST_CONTINGENCIES_EQUIPMENT_OPERATOR)
        );
    }
}
