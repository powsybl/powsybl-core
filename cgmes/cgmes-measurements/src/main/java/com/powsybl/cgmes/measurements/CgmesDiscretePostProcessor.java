/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.measurements;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement;
import com.powsybl.iidm.network.extensions.DiscreteMeasurements;
import com.powsybl.iidm.network.extensions.DiscreteMeasurementsAdder;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.powsybl.iidm.network.extensions.DiscreteMeasurement.Type.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class CgmesDiscretePostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesDiscretePostProcessor.class);

    public static void process(Network network, String id, String terminalId, String powerSystemResourceId, String measurementType, PropertyBags bays) {
        if (terminalId != null) {
            Identifiable<?> identifiable = network.getIdentifiable(terminalId);
            if (identifiable != null) {
                createDisMeas(identifiable, id, measurementType);
                return;
            }
            LOG.warn("Ignored terminal {} of {} {}: not found", terminalId, measurementType, id);
        }
        Identifiable<?> identifiable = network.getIdentifiable(powerSystemResourceId);
        if (identifiable != null) {
            createDisMeas(identifiable, id, measurementType);
            return;
        }
        PropertyBag bay = bays.stream().filter(b -> b.getId("Bay").equals(powerSystemResourceId)).findFirst().orElse(null);
        if (bay != null) {
            String voltageLevelId = bay.getId("VoltageLevel");
            LOG.info("Power resource system {} of Discrete {} is a Bay: Discrete is attached to the associated voltage level {}",
                    powerSystemResourceId, id, voltageLevelId);
            VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
            if (voltageLevel == null) {
                LOG.warn("Ignored {} {}: associated voltage level {} not found", measurementType, id, voltageLevelId);
                return;
            }
            createDisMeas(voltageLevel, id, measurementType);
        } else {
            LOG.warn("Ignored {} {}: attached power system resource {} not found", measurementType, id, powerSystemResourceId);
        }
    }

    private static void createDisMeas(Identifiable<?> identifiable, String id, String measurementType) {
        DiscreteMeasurements meas = identifiable.getExtension(DiscreteMeasurements.class);
        if (meas == null) {
            identifiable.newExtension(DiscreteMeasurementsAdder.class).add();
            meas = identifiable.getExtension(DiscreteMeasurements.class);
        }
        DiscreteMeasurement measurement = meas.newDiscreteMeasurement()
                .setValid(false)
                .setId(id)
                .setType(getType(measurementType))
                .add();
        if (measurement.getType() == OTHER) {
            measurement.putProperty("type", measurementType);
        }
    }

    private static DiscreteMeasurement.Type getType(String measurementType) {
        switch (measurementType) {
            case "SwitchPosition":
                return SWITCH_POSITION;
            case "TapPosition":
                return TAP_POSITION;
            default:
                return OTHER;
        }
    }

    private CgmesDiscretePostProcessor() {
    }
}
