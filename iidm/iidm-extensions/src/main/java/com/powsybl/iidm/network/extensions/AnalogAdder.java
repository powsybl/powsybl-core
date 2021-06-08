/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface AnalogAdder {

    AnalogAdder setId(String id);

    AnalogAdder putProperty(String name, Object property);

    AnalogAdder setType(Analog.Type type);

    AnalogAdder setValue(double value);

    AnalogAdder setSide(Analog.Side side);

    Analogs add();
}
