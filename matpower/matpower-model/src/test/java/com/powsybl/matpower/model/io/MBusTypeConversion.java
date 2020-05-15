/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.model.io;

import com.powsybl.matpower.model.MBus;
import com.univocity.parsers.conversions.ObjectConversion;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class MBusTypeConversion extends ObjectConversion<MBus.Type> {
    @Override
    protected MBus.Type fromString(String s) {
        return MBus.Type.fromInt(Integer.parseInt(s.trim()));
    }

    @Override
    public String revert(MBus.Type type) {
        return Integer.toString(type.getValue());
    }
}
