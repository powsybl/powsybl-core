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
            process(network, analog.getId("Analog"), analog.getId("Terminal"), analog.getId("powerSystemResource"), "Analog", bays);
        }
        for (PropertyBag discrete : model.discretes()) {
            process(network, discrete.getId("Discrete"), discrete.getId("Terminal"), discrete.getId("powerSystemResource"), "Discrete", bays);
        }
    }

    private static void process(Network network, String id, String terminalId, String powerSystemResourceId, String type, PropertyBags bays) {
        if (terminalId != null) {
            Identifiable identifiable = network.getIdentifiable(terminalId);
            if (identifiable == null) {
                LOG.warn("Ignored terminal {} of {} {}: not found", terminalId, type, id);
            } else {
                identifiable.addAlias(id, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + type + getSide(terminalId, identifiable));
                return;
            }
        }
        Identifiable<?> identifiable = network.getIdentifiable(powerSystemResourceId);
        if (identifiable != null) {
            if (identifiable.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + type).isEmpty()) {
                identifiable.addAlias(id, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + type);
            } else {
                identifiable.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + type, id);
            }
            return;
        }
        PropertyBag bay = bays.stream().filter(b -> b.getId("Bay").equals(powerSystemResourceId)).findFirst().orElse(null);
        if (bay != null) {
            String voltageLevelId = bay.getId("VoltageLevel");
            VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
            if (voltageLevel == null) {
                LOG.warn("Ignored {} {}: attached voltage level {} not found", type, id, voltageLevelId);
                return;
            }
            voltageLevel.addAlias(id, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + type);
        } else {
            LOG.warn("Ignored {} {}: attached power system resource {} not found", type, id, powerSystemResourceId);
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
}
