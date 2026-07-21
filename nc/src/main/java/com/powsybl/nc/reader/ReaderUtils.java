/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.nc.reader;

import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Thomas Bouquet {@literal <thomas.bouquet at rte-france.com>}
 */
public final class ReaderUtils {
    public static final String MRID = "mRID";

    private ReaderUtils() {
    }

    public static String getElementIdFromResourceUri(String resourceUri) {
        // expected format is http://entsoe.eu/#_
        // +s need to be replaced by spaces to account for networks imported from UCTE
        return resourceUri.substring(resourceUri.lastIndexOf('#') + 2).replace('+', ' ');
    }

    public static Map<String, Set<PropertyBag>> groupOnAttribute(PropertyBags propertyBags, String propertyName, boolean isUri) {
        Map<String, Set<PropertyBag>> propertyBagsPerAttribute = new HashMap<>();
        propertyBags.forEach(
            propertyBag -> propertyBagsPerAttribute.computeIfAbsent(
                isUri ? ReaderUtils.getElementIdFromResourceUri(propertyBag.get(propertyName)) : propertyName,
                k -> new HashSet<>()
            ).add(propertyBag));
        return propertyBagsPerAttribute;
    }
}
