/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement;
import com.powsybl.iidm.network.extensions.DiscreteMeasurements;
import com.powsybl.iidm.network.util.Identifiables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public final class DiscreteMeasurementValidationUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscreteMeasurementValidationUtil.class);

    public static <I extends Identifiable<I>> void checkId(String id, DiscreteMeasurements<I> discreteMeasurements) {
        checkId(id, false, discreteMeasurements);
    }

    public static <I extends Identifiable<I>> String checkId(String id, boolean idUnicity, DiscreteMeasurements<I> discreteMeasurements) {
        String finalId = id;
        if (id != null && discreteMeasurements.getDiscreteMeasurement(id) != null) {
            if (idUnicity) {
                finalId = Identifiables.getUniqueId(id, s -> discreteMeasurements.getDiscreteMeasurement(s) != null);
                LOGGER.warn("Ensure ID {} unicity: {}", id, finalId);
            } else {
                throw new PowsyblException(String.format("There is already a discrete measurement with ID %s", id));
            }
        }
        return finalId;
    }

    public static <I extends Identifiable<I>> void checkType(DiscreteMeasurement.Type type, Identifiable<I> i) {
        Objects.requireNonNull(type);
        if (type == DiscreteMeasurement.Type.SWITCH_POSITION && !(i instanceof Switch)) {
            throw new PowsyblException("SWITCH_POSITION discrete not linked to a switch");
        }
        if (type == DiscreteMeasurement.Type.TAP_POSITION && !(i instanceof TwoWindingsTransformer || i instanceof ThreeWindingsTransformer)) {
            throw new PowsyblException("TAP_POSITION discrete not linked to a transformer");
        }
    }

    public static <I extends Identifiable<I>> void checkTapChanger(DiscreteMeasurement.TapChanger tapChanger,
                                                                   DiscreteMeasurement.Type type, Identifiable<I> i) {
        if (tapChanger == null && type == DiscreteMeasurement.Type.TAP_POSITION) {
            throw new PowsyblException("The measured tap changer must be specified");
        }
        if (tapChanger != null) {
            if (!(i instanceof TwoWindingsTransformer || i instanceof ThreeWindingsTransformer)) {
                throw new PowsyblException("A tap changer is specified when the measured equipment is not a tap changer");
            }
            if (i instanceof TwoWindingsTransformer
                    && tapChanger != DiscreteMeasurement.TapChanger.RATIO_TAP_CHANGER
                    && tapChanger != DiscreteMeasurement.TapChanger.PHASE_TAP_CHANGER) {
                throw new PowsyblException("A non existent tap changer place has been specified for a two windings transformer's tap changer");
            }
            if (i instanceof ThreeWindingsTransformer
                    && (tapChanger == DiscreteMeasurement.TapChanger.RATIO_TAP_CHANGER
                    || tapChanger == DiscreteMeasurement.TapChanger.PHASE_TAP_CHANGER)) {
                throw new PowsyblException("Side is not specified for the measured tap changer of a three windings transformer");
            }
        }
    }

    public static void checkValue(Object value, boolean valid) {
        if (value == null && valid) {
            throw new PowsyblException("A valid discrete measurement can not have an undefined value");
        }
    }

    public static DiscreteMeasurement.ValueType getValueType(Object value) {
        if (value == null || value instanceof String) {
            return DiscreteMeasurement.ValueType.STRING;
        }
        if (value instanceof Integer) {
            return DiscreteMeasurement.ValueType.INT;
        }
        if (value instanceof Boolean) {
            return DiscreteMeasurement.ValueType.BOOLEAN;
        }
        throw new PowsyblException("Unsupported value type for discrete measurement: " + value.getClass().getName());
    }

    private DiscreteMeasurementValidationUtil() {
    }
}
