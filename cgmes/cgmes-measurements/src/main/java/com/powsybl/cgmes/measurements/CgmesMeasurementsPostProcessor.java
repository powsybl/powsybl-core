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
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.TripleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            process(network, analog.getId("Analog"), analog.getId("Terminal"), analog.getId("powerSystemResource"), "Analog", analog.getId("type"), bays);
        }
        for (PropertyBag discrete : model.discretes()) {
            process(network, discrete.getId("Discrete"), discrete.getId("Terminal"), discrete.getId("powerSystemResource"), "Discrete", discrete.getId("type"), bays);
        }
    }

    private static void process(Network network, String id, String terminalId, String powerSystemResourceId, String type, String measurementType, PropertyBags bays) {
        if (terminalId != null) {
            Identifiable identifiable = network.getIdentifiable(terminalId);
            if (identifiable == null) {
                LOG.warn("Ignored terminal {} of {} {}: not found", terminalId, measurementType, id);
            } else {
                addAliasOrProperty(identifiable, id, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + type + "_" + measurementType + getSide(terminalId, identifiable));
                return;
            }
        }
        Identifiable<?> identifiable = network.getIdentifiable(powerSystemResourceId);
        if (identifiable != null) {
            addAliasOrProperty(identifiable, id, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + type + "_" + measurementType);
            return;
        }
        PropertyBag bay = bays.stream().filter(b -> b.getId("Bay").equals(powerSystemResourceId)).findFirst().orElse(null);
        if (bay != null) {
            String voltageLevelId = bay.getId("VoltageLevel");
            LOG.info("Power resource system {} of {} {} is a Bay: {} is attached to the associated voltage level {}",
                    powerSystemResourceId, type, id, type, voltageLevelId);
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

    private static String getSide(String terminalId, Identifiable<?> identifiable) {
        String terminalType = identifiable.getAliasType(terminalId).orElse(null);
        if (terminalType != null) {
            if (terminalType.endsWith("1")) {
                return "_1";
            } else if (terminalType.endsWith("2")) {
                return "_2";
            }
        }
        return "";
    }

    private static void addAliasOrProperty(Identifiable identifiable, String id, String type) {
        if (identifiable.getAliasFromType(type).isEmpty()) {
            identifiable.addAlias(id, type);
        } else {
            identifiable.setProperty(type, id);
        }
    }
}
