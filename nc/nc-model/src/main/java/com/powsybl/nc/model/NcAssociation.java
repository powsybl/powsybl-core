/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.model;

import com.powsybl.nc.model.io.NcConstants;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public interface NcAssociation extends NcObject {
    String INCLUDED = NcConstants.ENTSOE_NS_NC_URL + "#ElementCombinationConstraintKind.included";
    String EXCLUDED = NcConstants.ENTSOE_NS_NC_URL + "#ElementCombinationConstraintKind.excluded";

    String combinationConstraintKind();

    default boolean isIncluded() {
        return INCLUDED.equals(combinationConstraintKind());
    }

    default boolean isExcluded() {
        return EXCLUDED.equals(combinationConstraintKind());
    }
}
