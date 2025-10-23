/*
 * Copyright (c) 2019-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.conversion;

import com.powsybl.ieeecdf.model.IeeeCdfTitle;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class SeasonConversion {

    private SeasonConversion() {
        // Utility class
    }

    public static IeeeCdfTitle.Season fromString(String str) {
        char season = str.charAt(0);
        return switch (season) {
            case 'S' -> IeeeCdfTitle.Season.SUMMER;
            case 'W' -> IeeeCdfTitle.Season.WINTER;
            default -> throw new IllegalStateException("Unknown season: " + season);
        };
    }

    public static String revert(IeeeCdfTitle.Season season) {
        return switch (season) {
            case SUMMER -> "S";
            case WINTER -> "W";
            default -> throw new IllegalStateException("Unknown season: " + season);
        };
    }
}
