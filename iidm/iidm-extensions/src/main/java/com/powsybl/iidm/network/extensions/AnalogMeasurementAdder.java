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
public interface AnalogMeasurementAdder {

    AnalogMeasurementAdder setId(String id);

    AnalogMeasurementAdder putProperty(String name, Object property);

    AnalogMeasurementAdder setType(AnalogMeasurement.Type type);

    AnalogMeasurementAdder setValue(double value);

    AnalogMeasurementAdder setSide(AnalogMeasurement.Side side);

    AnalogMeasurementAdder setValid(boolean valid);

    AnalogMeasurements add();
}
