/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model;

import com.univocity.parsers.conversions.ObjectConversion;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SeasonConversion extends ObjectConversion<IeeeCdfTitle.Season> {

    @Override
    protected IeeeCdfTitle.Season fromString(String str) {
        char season = str.charAt(0);
        switch (season) {
            case 'S':
                return IeeeCdfTitle.Season.SUMMER;
            case 'W':
                return IeeeCdfTitle.Season.WINTER;
            default:
                throw new IllegalStateException("Unknown season: " + season);
        }
    }

    @Override
    public String revert(IeeeCdfTitle.Season season) {
        switch (season) {
            case SUMMER:
                return "S";
            case WINTER:
                return "W";
            default:
                throw new IllegalStateException("Unknown season: " + season);
        }
    }
}
