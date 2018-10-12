/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import java.util.Optional;

import com.powsybl.iidm.network.Country;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
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
        }
        return Optional.empty();

    }

    public static Optional<Country> fromSubregionName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        switch (name) {
            case "NO1":
            case "NO2":
            case "NO3":
            case "NO4":
            case "NO5":
                return Optional.of(Country.NO);
            case "SE1":
            case "SE2":
            case "SE3":
            case "SE4":
                return Optional.of(Country.SE);
            case "FI1":
                return Optional.of(Country.FI);
            case "DK1":
            case "DK2":
                return Optional.of(Country.DK);
            case "EE1":
                return Optional.of(Country.EE);
            case "LV1":
                return Optional.of(Country.LV);
            case "LT1":
                return Optional.of(Country.LT);
            default:
                return Optional.empty();
        }
    }

    public static Country defaultCountry() {
        return Country.values()[0];
    }
}
