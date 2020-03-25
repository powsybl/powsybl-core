/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.model;

import com.univocity.parsers.conversions.ObjectConversion;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class MDoubleConversion extends ObjectConversion<Double> {
    @Override
    protected Double fromString(String s) {
        if (s.equals("Inf")) {
            return Double.POSITIVE_INFINITY;
        } else if (s.equals("-Inf")) {
            return Double.NEGATIVE_INFINITY;
        } else {
            return Double.parseDouble(s);
        }
    }

    @Override
    public String revert(Double adouble) {
        if (adouble == Double.POSITIVE_INFINITY) {
            return "Inf";
        } else if (adouble == Double.NEGATIVE_INFINITY) {
            return "-Inf";
        } else {
            return Double.toString(adouble);
        }
    }
}
