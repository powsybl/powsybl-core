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
public class SeasonConversion extends ObjectConversion<IeeeCdfTitle.Season> {

    @Override
    protected IeeeCdfTitle.Season fromString(String input) {
        switch (input.charAt(0)) {
            case 'S':
                return IeeeCdfTitle.Season.SUMMER;
            case 'W':
                return IeeeCdfTitle.Season.WINTER;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public String revert(IeeeCdfTitle.Season input) {
        switch (input) {
            case SUMMER:
                return "S";
            case WINTER:
                return "W";
            default:
                throw new AssertionError();
        }
    }
}
