/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.Measurement;
import com.powsybl.iidm.network.extensions.Measurements;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.util.Identifiables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.powsybl.iidm.network.extensions.Measurement.Type.OTHER;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public final class MeasurementValidationUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementValidationUtil.class);

    public static <C extends Connectable<C>> void checkId(String id, Measurements<C> measurements) {
        checkId(id, false, measurements);
    }

    public static <C extends Connectable<C>> String checkId(String id, boolean idUnicity, Measurements<C> measurements) {
        String finalId = id;
        if (id != null && measurements.getMeasurement(id) != null) {
            if (idUnicity) {
                finalId = Identifiables.getUniqueId(id, s -> measurements.getMeasurement(s) != null);
                LOGGER.warn("Ensure ID {} unicity: {}", id, finalId);
            } else {
                throw new PowsyblException(String.format("There is already a measurement with ID %s", id));
            }
        }
        return finalId;
    }

    public static void checkValue(double value, boolean valid) {
        if (Double.isNaN(value) && valid) {
            throw new PowsyblException("Valid measurement can not have an undefined value");
        }
    }

    public static <C extends Connectable<C>> void checkSide(Measurement.Type type, ThreeSides side, Connectable<C> c) {
        if (side != null && c instanceof Injection) {
            throw new PowsyblException("Inconsistent side for measurement of injection");
        } else if (side == null && type != OTHER && !(c instanceof Injection)) {
            throw new PowsyblException("Inconsistent null side for measurement of branch or three windings transformer");
        }
    }

    private MeasurementValidationUtil() {
    }
}
