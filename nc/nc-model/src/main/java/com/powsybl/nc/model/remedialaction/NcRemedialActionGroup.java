/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.model.remedialaction;

import com.powsybl.nc.model.NcIdentifiedObject;
import com.powsybl.nc.model.io.NcConstants;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public record NcRemedialActionGroup(String mrid, String name) implements NcIdentifiedObject {
    public static NcRemedialActionGroup fromPropertyBag(PropertyBag propertyBag) {
        return new NcRemedialActionGroup(propertyBag.getId(NcConstants.REQUEST_REMEDIAL_ACTION_GROUP), propertyBag.get(NcConstants.REMEDIAL_ACTION_NAME));
    }
}
