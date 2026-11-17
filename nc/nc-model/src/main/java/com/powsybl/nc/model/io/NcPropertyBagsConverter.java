/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.model.io;

import com.powsybl.nc.model.NcObject;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public final class NcPropertyBagsConverter {

    private NcPropertyBagsConverter() {
    }

    public static <T extends NcObject> Set<T> convert(PropertyBags propertyBags, Function<PropertyBag, T> propertyBagConverter) {
        return propertyBags.stream().map(propertyBagConverter).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
