/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.measurements;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.powsybl.iidm.network.extensions.DiscreteMeasurement.Type.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public final class CgmesDiscretePostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesDiscretePostProcessor.class);

    // To keep public interface compatible even though the public aspect of this method is arguably a "nice to have".
    // Migration path : as below, turn the PropertyBags into a map before calling the non deprecated method.
    @Deprecated
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
                createDisMeas(identifiable, id, measurementType, typesMapping);
                return;
            }
            LOG.warn("Ignored terminal {} of {} {}: not found", terminalId, measurementType, id);
        }
        Identifiable<?> identifiable = network.getIdentifiable(powerSystemResourceId);
        if (identifiable != null) {
            createDisMeas(identifiable, id, measurementType, typesMapping);
            return;
        }
        PropertyBag bay = idToBayBag.get(powerSystemResourceId);
        if (bay != null) {
            String voltageLevelId = bay.getId("VoltageLevel");
            LOG.info("Power resource system {} of Discrete {} is a Bay: Discrete is attached to the associated voltage level {}",
                    powerSystemResourceId, id, voltageLevelId);
            VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
            if (voltageLevel == null) {
                LOG.warn("Ignored {} {}: associated voltage level {} not found", measurementType, id, voltageLevelId);
                return;
            }
            createDisMeas(voltageLevel, id, measurementType, typesMapping);
        } else {
            LOG.warn("Ignored {} {}: attached power system resource {} not found", measurementType, id, powerSystemResourceId);
        }
    }

    private static void createDisMeas(Identifiable<?> identifiable, String id, String measurementType, Map<String, String> typesMapping) {
        DiscreteMeasurements meas = identifiable.getExtension(DiscreteMeasurements.class);
        if (meas == null) {
            identifiable.newExtension(DiscreteMeasurementsAdder.class).add();
            meas = identifiable.getExtension(DiscreteMeasurements.class);
        }
        DiscreteMeasurement.Type type = getType(measurementType, typesMapping);
        DiscreteMeasurementAdder adder = meas.newDiscreteMeasurement()
                .setValid(false)
                .setId(id)
                .setType(type);
        if (type == TAP_POSITION) {
            setTapChanger(adder, identifiable);
        }
        DiscreteMeasurement measurement = adder.add();
        measurement.putProperty("cgmesType", measurementType);
        // TODO get value of discrete measurements
    }

    private static void setTapChanger(DiscreteMeasurementAdder adder, Identifiable<?> identifiable) {
        if (identifiable instanceof TwoWindingsTransformer twt) {
            if (twt.hasRatioTapChanger() && !twt.hasPhaseTapChanger()) {
                adder.setTapChanger(DiscreteMeasurement.TapChanger.RATIO_TAP_CHANGER);
            } else if (!twt.hasRatioTapChanger() && twt.hasPhaseTapChanger()) {
                adder.setTapChanger(DiscreteMeasurement.TapChanger.PHASE_TAP_CHANGER);
            } else {
                adder.setType(OTHER);
            }
        } else if (identifiable instanceof ThreeWindingsTransformer twt) {
            List<DiscreteMeasurement.TapChanger> tapChangers = new ArrayList<>();
            twt.getLeg1().getOptionalRatioTapChanger().ifPresent(tc -> tapChangers.add(DiscreteMeasurement.TapChanger.RATIO_TAP_CHANGER_1));
            twt.getLeg2().getOptionalRatioTapChanger().ifPresent(tc -> tapChangers.add(DiscreteMeasurement.TapChanger.RATIO_TAP_CHANGER_2));
            twt.getLeg3().getOptionalRatioTapChanger().ifPresent(tc -> tapChangers.add(DiscreteMeasurement.TapChanger.RATIO_TAP_CHANGER_3));
            twt.getLeg1().getOptionalPhaseTapChanger().ifPresent(tc -> tapChangers.add(DiscreteMeasurement.TapChanger.PHASE_TAP_CHANGER_1));
            twt.getLeg2().getOptionalPhaseTapChanger().ifPresent(tc -> tapChangers.add(DiscreteMeasurement.TapChanger.PHASE_TAP_CHANGER_2));
            twt.getLeg3().getOptionalPhaseTapChanger().ifPresent(tc -> tapChangers.add(DiscreteMeasurement.TapChanger.PHASE_TAP_CHANGER_3));
            if (tapChangers.size() == 1) {
                adder.setTapChanger(tapChangers.get(0));
            } else {
                adder.setType(OTHER);
            }
        }
    }

    private static DiscreteMeasurement.Type getType(String measurementType, Map<String, String> typesMapping) {
        switch (measurementType) {
            case "SwitchPosition":
                return SWITCH_POSITION;
            case "TapPosition":
                return TAP_POSITION;
            default:
                String iidmType = typesMapping.get(measurementType);
                if (iidmType != null) {
                    return DiscreteMeasurement.Type.valueOf(iidmType);
                }
                return OTHER;
        }
    }

    private CgmesDiscretePostProcessor() {
    }
}
