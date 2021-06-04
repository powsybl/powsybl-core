/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.extensions.ExtensionSeriesBuilder;
import com.powsybl.commons.extensions.ExtensionSeriesSerializer;
import com.powsybl.iidm.network.Generator;

import java.util.Map;
import java.util.Optional;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
@AutoService(ExtensionSeriesSerializer.class)
public class ApcExtensionSeriesSerializer implements ExtensionSeriesSerializer<Generator, ActivePowerControlImpl<Generator>> {

    private static final String DROOP = "apc_droop";
    private static final String PARTICIPATE = "apc_participate";

    private static final Map<String, Integer> TYPE_MAP = ImmutableMap.<String, Integer>builder()
            .put(DROOP, ExtensionSeriesSerializer.DOUBLE_SERIES_TYPE)
            .put(PARTICIPATE, ExtensionSeriesSerializer.BOOLEAN_SERIES_TYPE)
            .build();

    @Override
    public void serialize(ExtensionSeriesBuilder<?, Generator> builder) {
        builder.addBooleanSeries(PARTICIPATE, g -> {
            Optional<Object> extension = Optional.ofNullable(g.getExtension(ActivePowerControlImpl.class));
            if (extension.isPresent()) {
                ActivePowerControlImpl apc = (ActivePowerControlImpl) extension.get();
                return apc.isParticipate();
            } else {
                return false;
            }
        });
        builder.addDoubleSeries(DROOP, g -> {
            Optional<Object> extension = Optional.ofNullable(g.getExtension(ActivePowerControlImpl.class));
            if (extension.isPresent()) {
                ActivePowerControlImpl apc = (ActivePowerControlImpl) extension.get();
                return apc.getDroop();
            } else {
                return Float.NaN;
            }
        });
    }

    @Override
    public void deserialize(Generator generator, String name, double value) {
        Optional.ofNullable(generator.getExtension(ActivePowerControlImpl.class))
                .ifPresent(ext -> {
                    switch (name) {
                        case DROOP:
                            ((ActivePowerControlImpl) ext).setDroop((float) value);
                            break;
                        default:
                            throw new UnsupportedOperationException("Series name not supported for ActivePowerControlImpl: " + name);
                    }
                });
    }

    @Override
    public void deserialize(Generator element, String name, int value) {
        Optional.ofNullable(element.getExtension(ActivePowerControlImpl.class))
                .ifPresent(ext -> {
                    switch (name) {
                        case PARTICIPATE:
                            ((ActivePowerControlImpl) ext).setParticipate(value == 1);
                            break;
                        default:
                            throw new UnsupportedOperationException("Series name not supported for ActivePowerControlImpl: " + name);
                    }
                });
    }

    @Override
    public void deserialize(Generator element, String name, String value) {
    }

    @Override
    public Map<String, Integer> getTypeMap() {
        return TYPE_MAP;
    }

    @Override
    public String getExtensionName() {
        return "activePowerControl";
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<ActivePowerControlImpl> getExtensionClass() {
        return ActivePowerControlImpl.class;
    }

}
