/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.dgs;

import com.powsybl.powerfactory.model.PowerFactoryException;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DgsParsingContext {

    boolean general = false;

    DgsVersion version;

    List<DgsValueParser> valueParsers;

    private boolean decimalSeparatorIsComma = false;

    public double parseDouble(String value) {
        return parseFloat(value);
    }

    public float parseFloat(String value) {
        if (decimalSeparatorIsComma) {
            return Float.parseFloat(value.replace(',', '.'));
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ex) {
            try {
                float v = Float.parseFloat(value.replace(',', '.'));
                decimalSeparatorIsComma = true;
                return v;
            } catch (NumberFormatException ex2) {
                throw new PowerFactoryException("Invalid real value [" + value + "]");
            }
        }
    }
}
