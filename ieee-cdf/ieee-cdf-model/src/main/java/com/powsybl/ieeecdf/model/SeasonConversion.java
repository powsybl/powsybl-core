/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ieeecdf.model;

import com.univocity.parsers.conversions.ObjectConversion;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SeasonConversion extends ObjectConversion<String> {

    @Override
    protected String fromString(String str) {
        char season = str.charAt(0);
        switch (season) {
            case 'S':
                return IeeeCdfTitle.Season.SUMMER.name();
            case 'W':
                return IeeeCdfTitle.Season.WINTER.name();
            default:
                throw new AssertionError("Unknown season: " + season);
        }
    }

    @Override
    public String revert(String str) {
        return str.substring(0, 1);
    }
}
