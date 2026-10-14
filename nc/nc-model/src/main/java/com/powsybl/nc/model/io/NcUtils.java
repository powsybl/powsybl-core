/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.model.io;

import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import java.util.Map;
import java.util.Optional;

/**
 * @author Jean-Pierre Arnould {@literal <jean-pierre.arnould at rte-france.com>}
 */
public final class NcUtils {

    private NcUtils() {
    }

    public static Optional<String> createElementName(String nativeElementName, String operator) {
        if (nativeElementName == null) {
            return Optional.empty();
        }
        return Optional.of(operator == null ? nativeElementName : operator + "_" + nativeElementName);
    }

    public static PropertyBags overrideData(PropertyBags propertyBags, Map<NcOverrideKey, String> data,
                                     NcOverridingObjectsFields fields) {
        PropertyBags effectivePropertyBags = new PropertyBags();
        for (PropertyBag propertyBag : propertyBags) {
            String mrid = propertyBag.getId(fields.getObjectName());
            String value = mrid == null
                ? propertyBag.get(fields.getBaselineFieldName())
                : data.getOrDefault(NcOverrideKey.of(mrid, fields), propertyBag.get(fields.getBaselineFieldName()));
            effectivePropertyBags.add(value == null
                ? propertyBag
                : new EffectivePropertyBag(propertyBag, fields.getEffectiveFieldName(), value));
        }
        return effectivePropertyBags;
    }

    private static final class EffectivePropertyBag extends PropertyBag {
        private final PropertyBag baseline;
        private final String effectiveFieldName;
        private final String effectiveValue;

        private EffectivePropertyBag(PropertyBag baseline, String effectiveFieldName, String effectiveValue) {
            super(baseline.propertyNames(), true, true);
            this.baseline = baseline;
            this.effectiveFieldName = effectiveFieldName;
            this.effectiveValue = effectiveValue;
        }

        @Override
        public String get(Object key) {
            return effectiveFieldName.equals(key) ? effectiveValue : baseline.get(key);
        }

        @Override
        public String getOrDefault(Object key, String defaultValue) {
            String value = get(key);
            return value == null ? defaultValue : value;
        }

        @Override
        public boolean containsKey(Object key) {
            return effectiveFieldName.equals(key) || baseline.containsKey(key);
        }
    }
}
