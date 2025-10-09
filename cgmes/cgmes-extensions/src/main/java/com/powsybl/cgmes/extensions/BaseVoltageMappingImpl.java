/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class BaseVoltageMappingImpl extends AbstractExtension<Network> implements BaseVoltageMapping {

    static class BaseVoltageSourceImpl implements BaseVoltageSource {
        private final String id;
        private final double nominalV;
        private final Source source;

        BaseVoltageSourceImpl(String id, double nominalV, Source source) {
            this.id = Objects.requireNonNull(id);
            this.nominalV = nominalV;
            this.source = Objects.requireNonNull(source);
        }

        public String getId() {
            return id;
        }

        public double getNominalV() {
            return nominalV;
        }

        public Source getSource() {
            return source;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseVoltageMappingImpl.class);

    private final Map<Double, BaseVoltageSource> nominalVoltageBaseVoltageMap = new HashMap<>();

    BaseVoltageMappingImpl(Set<BaseVoltageSource> baseVoltages) {
        baseVoltages.forEach(bvs -> {
            Objects.requireNonNull(bvs);
            addBaseVoltage(bvs.getNominalV(), bvs.getId(), bvs.getSource());
        });
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
    public BaseVoltageMapping addBaseVoltage(double nominalVoltage, String baseVoltageId, Source source) {
        if (nominalVoltageBaseVoltageMap.containsKey(nominalVoltage)) {
            if (nominalVoltageBaseVoltageMap.get(nominalVoltage).getSource().equals(Source.IGM) && source.equals(Source.BOUNDARY)) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Nominal voltage {} is already mapped with an {} base voltage. Replaced by a {} base voltage", nominalVoltage, Source.IGM.name(), Source.BOUNDARY.name());
                }
                nominalVoltageBaseVoltageMap.put(nominalVoltage, new BaseVoltageSourceImpl(baseVoltageId, nominalVoltage, source));
            } else {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Nominal voltage {} is already mapped and not to the given base voltage {} from {}", nominalVoltage, baseVoltageId, source.name());
                }
            }
        } else {
            nominalVoltageBaseVoltageMap.put(nominalVoltage, new BaseVoltageSourceImpl(baseVoltageId, nominalVoltage, source));
        }
        return this;
    }

    @Override
    public Map<Double, BaseVoltageSource> baseVoltagesByNominalVoltageMap() {
        return new HashMap<>(nominalVoltageBaseVoltageMap);
    }
}
