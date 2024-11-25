/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Country;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public final class CountryConversion {

    private CountryConversion() {
    }

    public static Optional<Country> fromRegionName(String gr) {
        if (gr == null) {
            return Optional.empty();
        }
        if (gr.equals("D1")
            || gr.equals("D2")
            || gr.equals("D4")
            || gr.equals("D7")
            || gr.equals("D8")) {
            return Optional.of(Country.DE);
        }
        try {
            return Optional.of(Country.valueOf(gr));
        } catch (IllegalArgumentException ignored) {
            // Ignore
            LOG.warn("{} does not match any Country enum", gr);
        }
        return Optional.empty();
    }

    public static Optional<Country> fromSubregionName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return switch (name.trim().toUpperCase()) {
            case "NO1", "NO2", "NO3", "NO4", "NO5" -> Optional.of(Country.NO);
            case "SE1", "SE2", "SE3", "SE4" -> Optional.of(Country.SE);
            case "FI1" -> Optional.of(Country.FI);
            case "DK1", "DK2" -> Optional.of(Country.DK);
            case "EE1" -> Optional.of(Country.EE);
            case "LV1" -> Optional.of(Country.LV);
            case "LT1" -> Optional.of(Country.LT);
            default -> Optional.empty();
        };
    }

    public static Optional<Country> fromIsoCode(String iso) {
        if (iso == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Country.valueOf(iso.trim().toUpperCase()));
        } catch (IllegalArgumentException ignored) {
            // Ignore
        }
        return Optional.empty();
    }

    private static final Logger LOG = LoggerFactory.getLogger(CountryConversion.class);
}
