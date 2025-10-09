/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface DiscreteMeasurementAdder {

    DiscreteMeasurementAdder setId(String id);

    DiscreteMeasurementAdder putProperty(String name, String value);

    DiscreteMeasurementAdder setType(DiscreteMeasurement.Type type);

    DiscreteMeasurementAdder setTapChanger(DiscreteMeasurement.TapChanger tapChanger);

    DiscreteMeasurementAdder setValue(String value);

    DiscreteMeasurementAdder setValue(boolean value);

    DiscreteMeasurementAdder setValue(int value);

    DiscreteMeasurementAdder setValid(boolean valid);

    DiscreteMeasurementAdder setEnsureIdUnicity(boolean idUnicity);

    DiscreteMeasurement add();
}
