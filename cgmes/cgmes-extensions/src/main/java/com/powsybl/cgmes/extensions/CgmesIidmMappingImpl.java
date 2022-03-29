/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class CgmesIidmMappingImpl extends AbstractExtension<Network> implements CgmesIidmMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(CgmesIidmMappingImpl.class);

    // Ideally, each nominal voltage is represented by a single base voltage,
    // for this reason the mapping has been considered 1: 1

    private final Map<Double, BaseVoltageSource> nominalVoltageBaseVoltageMap;

    CgmesIidmMappingImpl(Set<BaseVoltageSource> baseVoltages) {
        nominalVoltageBaseVoltageMap = new HashMap<>();
        baseVoltages.forEach(bvs -> addBaseVoltage(bvs.getNominalV(), bvs.getCgmesId(), bvs.getSource()));
    }

    @Override
    public Map<Double, BaseVoltageSource> getBaseVoltages() {
        return Collections.unmodifiableMap(nominalVoltageBaseVoltageMap);
    }

    @Override
    public BaseVoltageSource getBaseVoltage(double nominalVoltage) {
        return nominalVoltageBaseVoltageMap.get(nominalVoltage);
    }

    @Override
    public boolean isBaseVoltageMapped(double nominalVoltage) {
        return nominalVoltageBaseVoltageMap.containsKey(nominalVoltage);
    }

    @Override
    public boolean isBaseVoltageEmpty() {
        return nominalVoltageBaseVoltageMap.isEmpty();
    }

    @Override
    public CgmesIidmMapping addBaseVoltage(double nominalVoltage, String baseVoltageId, Source source) {
        if (nominalVoltageBaseVoltageMap.containsKey(nominalVoltage)) {
            if (nominalVoltageBaseVoltageMap.get(nominalVoltage).getSource().equals(Source.IGM) && source.equals(Source.BOUNDARY)) {
                LOGGER.info("Nominal voltage {} is already mapped with an {} base voltage. Replaced by a {} base voltage", nominalVoltage, Source.IGM.name(), Source.BOUNDARY.name());
                nominalVoltageBaseVoltageMap.put(nominalVoltage, new BaseVoltageSource(baseVoltageId, nominalVoltage, source));
            } else {
                LOGGER.info("Nominal voltage {} is already mapped and not to the given base voltage {} from {}", nominalVoltage, baseVoltageId, source.name());
            }
        } else {
            nominalVoltageBaseVoltageMap.put(nominalVoltage, new BaseVoltageSource(baseVoltageId, nominalVoltage, source));
        }
        return this;
    }

    public Map<Double, BaseVoltageSource> baseVoltagesByNominalVoltageMap() {
        return new HashMap<>(nominalVoltageBaseVoltageMap);
    }
}
