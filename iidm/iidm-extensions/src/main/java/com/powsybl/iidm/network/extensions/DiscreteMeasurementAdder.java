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
public interface DiscreteMeasurementAdder {

    DiscreteMeasurementAdder setId(String id);

    DiscreteMeasurementAdder putProperty(String name, Object value);

    DiscreteMeasurementAdder setType(DiscreteMeasurement.Type type);

    DiscreteMeasurementAdder setStringValue(String value);

    DiscreteMeasurementAdder setIntValue(int value);

    DiscreteMeasurementAdder setValid(boolean valid);

    DiscreteMeasurements add();
}
