/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.iidm.network.ThreeSides;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface MeasurementAdder {

    MeasurementAdder setId(String id);

    MeasurementAdder putProperty(String name, String property);

    MeasurementAdder setType(Measurement.Type type);

    MeasurementAdder setValue(double value);

    MeasurementAdder setStandardDeviation(double standardDeviation);

    MeasurementAdder setSide(ThreeSides side);

    MeasurementAdder setValid(boolean valid);

    MeasurementAdder setEnsureIdUnicity(boolean ensureIdUnicity);

    Measurement add();
}
