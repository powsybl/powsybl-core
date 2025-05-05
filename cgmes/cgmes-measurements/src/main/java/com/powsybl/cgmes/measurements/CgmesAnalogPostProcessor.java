/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.measurements;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

import static com.powsybl.iidm.network.extensions.Measurement.Type.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public final class CgmesAnalogPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesAnalogPostProcessor.class);

    /**
     * @deprecated use {{@link #process(Network, String, String, String, String, Map, Map)}} instead.
     */
    @Deprecated(since = "6.7.1")
    public static void process(Network network, String id, String terminalId, String powerSystemResourceId,
            String measurementType, PropertyBags bays, Map<String, String> typesMapping) {

        Map<String, PropertyBag> m = bays.stream().collect(Collectors.toMap(b -> b.getId("Bay"), b -> b));
        process(network, id, terminalId, powerSystemResourceId, measurementType, m, typesMapping);
    }

    public static void process(Network network, String id, String terminalId,
            String powerSystemResourceId, String measurementType,
            Map<String, PropertyBag> idToBayBag, Map<String, String> typesMapping) {
        if (terminalId != null) {
            Identifiable<?> identifiable = network.getIdentifiable(terminalId);
            if (identifiable != null) {
                createMeas(identifiable, id, terminalId, measurementType, typesMapping);
                return;
            }
            LOG.warn("Ignored terminal {} of {} {}: not found", terminalId, measurementType, id);
        }
        Identifiable<?> identifiable = network.getIdentifiable(powerSystemResourceId);
        if (identifiable != null) {
            createMeas(identifiable, id, terminalId, measurementType, typesMapping);
            return;
        }
        PropertyBag bay = idToBayBag.get(powerSystemResourceId);
        if (bay != null) {
            String voltageLevelId = bay.getId("VoltageLevel");
            LOG.info("Power resource system {} of Analog {} is a Bay: Analog is attached to the associated voltage level {}",
                    powerSystemResourceId, id, voltageLevelId);
            VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
            if (voltageLevel == null) {
                LOG.warn("Ignored {} {}: associated voltage level {} not found", measurementType, id, voltageLevelId);
                return;
            }
            voltageLevel.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Analog_" + measurementType, id);
        } else {
            LOG.warn("Ignored {} {}: attached power system resource {} not found", measurementType, id, powerSystemResourceId);
        }
    }

    private static void createMeas(Identifiable<?> identifiable, String id, String terminalId, String measurementType, Map<String, String> typesMapping) {
        if (identifiable instanceof Connectable) {
            Connectable<?> c = (Connectable<?>) identifiable;
            Measurements meas = c.getExtension(Measurements.class);
            if (meas == null) {
                c.newExtension(MeasurementsAdder.class).add();
                meas = c.getExtension(Measurements.class);
            }
            Measurement.Type type = getType(measurementType, typesMapping);
            ThreeSides side = null;
            MeasurementAdder adder = meas.newMeasurement()
                    .setValid(false)
                    .setId(id);
            if (!(c instanceof Injection)) {
                side = getSide(terminalId, c);
            }
            if (type != OTHER && side == null && !(c instanceof Injection)) {
                adder.setType(OTHER);
            } else {
                adder.setType(type);
            }
            adder.setSide(side);
            Measurement measurement = adder.add();
            measurement.putProperty("cgmesType", measurementType);
        } else {
            identifiable.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Analog_" + measurementType, id);
        }
        // TODO get value of measurement
    }

    private static Measurement.Type getType(String measurementType, Map<String, String> typesMapping) {
        switch (measurementType) {
            case "ThreePhaseActivePower":
            case "ActivePower":
                return ACTIVE_POWER;
            case "Angle":
                return ANGLE;
            case "ApparentPower":
                return APPARENT_POWER;
            case "Current":
                return CURRENT;
            case "Frequency":
                return FREQUENCY;
            case "ThreePhaseReactivePower":
            case "ReactivePower":
                return REACTIVE_POWER;
            case "PhaseVoltage":
            case "Voltage":
                return VOLTAGE;
            default:
                String iidmType = typesMapping.get(measurementType);
                if (iidmType != null) {
                    return Measurement.Type.valueOf(iidmType);
                }
                return OTHER;
        }
    }

    private static ThreeSides getSide(String terminalId, Connectable<?> c) {
        if (terminalId != null) {
            String terminalType = c.getAliasType(terminalId).orElse(null);
            if (terminalType != null) {
                if (terminalType.endsWith("1")) {
                    return ThreeSides.ONE;
                } else if (terminalType.endsWith("2")) {
                    return ThreeSides.TWO;
                } else if (terminalType.endsWith("3")) {
                    return ThreeSides.THREE;
                }
            }
        }
        return null;
    }

    private CgmesAnalogPostProcessor() {
    }
}
