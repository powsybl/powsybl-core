/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.measurements;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.conversion.CgmesImportPostProcessor;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.TripleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.powsybl.iidm.network.extensions.Measurement.Side.ONE;
import static com.powsybl.iidm.network.extensions.Measurement.Side.THREE;
import static com.powsybl.iidm.network.extensions.Measurement.Side.TWO;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(CgmesImportPostProcessor.class)
public class CgmesMeasurementsPostProcessor implements CgmesImportPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesMeasurementsPostProcessor.class);

    @Override
    public String getName() {
        return "measurements";
    }

    @Override
    public void process(Network network, TripleStore tripleStore) {
        CgmesMeasurementsModel model = new CgmesMeasurementsModel(tripleStore);
        PropertyBags bays = model.bays();
        for (PropertyBag analog : model.analogs()) {
            processMeas(network, analog.getId("Analog"), analog.getId("Terminal"), analog.getId("powerSystemResource"), analog.getId("type"), bays);
        }
        for (PropertyBag discrete : model.discretes()) {
            processDisMeas(network, discrete.getId("Discrete"), discrete.getId("Terminal"), discrete.getId("powerSystemResource"), discrete.getId("type"), bays);
        }
    }

    private static void processMeas(Network network, String id, String terminalId, String powerSystemResourceId, String measurementType, PropertyBags bays) {
        if (terminalId != null) {
            Identifiable identifiable = network.getIdentifiable(terminalId);
            if (identifiable != null) {
                if (identifiable instanceof Connectable) {
                    Connectable<?> c = (Connectable<?>) identifiable;
                    Measurements meas = c.getExtension(Measurements.class);
                    if (meas == null) {
                        c.newExtension(MeasurementsAdder.class).add();
                        meas = c.getExtension(Measurements.class);
                    }
                    MeasurementAdder adder = meas.newMeasurement()
                            .setValid(false)
                            .setId(id)
                            .setType(Measurement.Type.OTHER)
                            .putProperty("type", measurementType);
                    if (!(c instanceof Injection)) {
                        adder.setSide(getSide(terminalId, c));
                    }
                    adder.add();
                } else {
                    addProperty(identifiable, id, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Analog_" + measurementType);
                }
                return;
            }
            LOG.warn("Ignored terminal {} of {} {}: not found", terminalId, measurementType, id);
        }
        Identifiable<?> identifiable = network.getIdentifiable(powerSystemResourceId);
        if (identifiable != null) {
            if (identifiable instanceof Injection) {
                Injection<?> inj = (Injection<?>) identifiable;
                Measurements meas = inj.getExtension(Measurements.class);
                if (meas == null) {
                    inj.newExtension(MeasurementsAdder.class).add();
                    meas = inj.getExtension(Measurements.class);
                }
                meas.newMeasurement()
                        .setValid(false)
                        .setId(id)
                        .setType(Measurement.Type.OTHER)
                        .putProperty("type", measurementType)
                        .add();
            } else {
                addProperty(identifiable, id, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Analog_" + measurementType);
            }
            return;
        }
        PropertyBag bay = bays.stream().filter(b -> b.getId("Bay").equals(powerSystemResourceId)).findFirst().orElse(null);
        if (bay != null) {
            String voltageLevelId = bay.getId("VoltageLevel");
            LOG.info("Power resource system {} of Analog {} is a Bay: Analog is attached to the associated voltage level {}",
                    powerSystemResourceId, id, voltageLevelId);
            VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
            if (voltageLevel == null) {
                LOG.warn("Ignored {} {}: associated voltage level {} not found", measurementType, id, voltageLevelId);
                return;
            }
            voltageLevel.addAlias(id, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + measurementType);
        } else {
            LOG.warn("Ignored {} {}: attached power system resource {} not found", measurementType, id, powerSystemResourceId);
        }
    }

    private static void processDisMeas(Network network, String id, String terminalId, String powerSystemResourceId, String measurementType, PropertyBags bays) {
        if (terminalId != null) {
            Identifiable<?> identifiable = network.getIdentifiable(terminalId);
            if (identifiable != null) {
                DiscreteMeasurements meas = identifiable.getExtension(DiscreteMeasurements.class);
                if (meas == null) {
                    identifiable.newExtension(DiscreteMeasurementsAdder.class).add();
                    meas = identifiable.getExtension(DiscreteMeasurements.class);
                }
                meas.newDiscreteMeasurement()
                        .setValid(false)
                        .setId(id)
                        .setType(DiscreteMeasurement.Type.OTHER)
                        .putProperty("type", measurementType)
                        .add();
                return;
            }
            LOG.warn("Ignored terminal {} of {} {}: not found", terminalId, measurementType, id);
        }
        Identifiable<?> identifiable = network.getIdentifiable(powerSystemResourceId);
        if (identifiable != null) {
            DiscreteMeasurements meas = identifiable.getExtension(DiscreteMeasurements.class);
            if (meas == null) {
                identifiable.newExtension(DiscreteMeasurementsAdder.class).add();
                meas = identifiable.getExtension(DiscreteMeasurements.class);
            }
            meas.newDiscreteMeasurement()
                    .setValid(false)
                    .setId(id)
                    .setType(DiscreteMeasurement.Type.OTHER)
                    .putProperty("type", measurementType)
                    .add();
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
            voltageLevel.addAlias(id, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + measurementType);
        } else {
            LOG.warn("Ignored {} {}: attached power system resource {} not found", measurementType, id, powerSystemResourceId);
        }
    }

    private static Measurement.Side getSide(String terminalId, Connectable<?> c) {
        String terminalType = c.getAliasType(terminalId).orElse(null);
        if (terminalType != null) {
            if (terminalType.endsWith("1")) {
                return Measurement.Side.ONE;
            } else if (terminalType.endsWith("2")) {
                return TWO;
            } else if (terminalType.endsWith("3")) {
                return THREE;
            }
        }
        return null;
    }

    private static void addProperty(Identifiable identifiable, String id, String type) {
        identifiable.setProperty(type, id);
    }
}
