/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.model.contingency;

import com.powsybl.nc.model.NcObject;
import com.powsybl.nc.model.io.NcConstants;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public record NcContingencyEquipment(String mrid, String contingency, String contingentStatus, String equipment) implements NcObject {

    public boolean isEquipmentOutOfService() {
        return NcConstants.OUT_OF_SERVICE_CONTINGENT_STATUS.equals(contingentStatus);
    }

    public static NcContingencyEquipment fromPropertyBag(PropertyBag propertyBag) {
        return new NcContingencyEquipment(
            propertyBag.get(NcConstants.REQUEST_CONTINGENCY_EQUIPMENT),
            propertyBag.getId(NcConstants.REQUEST_CONTINGENCY),
            propertyBag.get(NcConstants.REQUEST_CONTINGENCIES_CONTINGENT_STATUS),
            propertyBag.getId(NcConstants.REQUEST_CONTINGENCIES_EQUIPMENT_ID)
        );
    }
}
