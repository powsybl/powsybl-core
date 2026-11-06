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
public record NcStaticPropertyRange(String mrid, double value, NcValueOffsetKind valueKind, NcRelativeDirectionKind direction,
                                    String gridStateAlteration, NcPropertyReference propertyReference) implements NcObject {
    public static NcStaticPropertyRange fromPropertyBag(PropertyBag propertyBag) {
        return new NcStaticPropertyRange(
            propertyBag.getId(NcConstants.STATIC_PROPERTY_RANGE),
            Double.parseDouble(propertyBag.get(NcConstants.VALUE)),
            NcValueOffsetKind.fromUri(propertyBag.get(NcConstants.STATIC_PROPERTY_RANGE_VALUE_KIND)),
            NcRelativeDirectionKind.fromUri(propertyBag.get(NcConstants.STATIC_PROPERTY_RANGE_DIRECTION)),
            propertyBag.getId(NcConstants.GRID_STATE_ALTERATION),
            NcPropertyReference.fromUri(propertyBag.get(NcConstants.GRID_ALTERATION_PROPERTY_REFERENCE))
        );
    }
}
