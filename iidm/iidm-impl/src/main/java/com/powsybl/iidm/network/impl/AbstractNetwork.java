/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.FluentIterable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
abstract class AbstractNetwork extends AbstractIdentifiable<Network> implements NetworkExt {

    private ZonedDateTime caseDate = ZonedDateTime.now(ZoneOffset.UTC); // default is the time at which the network has been created

    private int forecastDistance = 0;

    protected String sourceFormat;

    AbstractNetwork(String id, String name, String sourceFormat) {
        super(id, name);
        this.sourceFormat = Objects.requireNonNull(sourceFormat, "source format is null");
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.NETWORK;
    }

    @Override
    public ZonedDateTime getCaseDate() {
        return caseDate;
    }

    @Override
    public Network setCaseDate(ZonedDateTime caseDate) {
        ValidationUtil.checkCaseDate(this, caseDate);
        this.caseDate = caseDate;
        return this;
    }

    @Override
    public int getForecastDistance() {
        return forecastDistance;
    }

    @Override
    public Network setForecastDistance(int forecastDistance) {
        ValidationUtil.checkForecastDistance(this, forecastDistance);
        this.forecastDistance = forecastDistance;
        return this;
    }

    @Override
    public String getSourceFormat() {
        return sourceFormat;
    }

    @Override
    protected String getTypeDescription() {
        return "Network";
    }

    /**
     * Transfer the extensions of a network to another one.
     * @param from the network whose extensions must be transferred
     * @param to the destination network
     */
    protected static void transferExtensions(Network from, Network to) {
        transferExtensions(from, to, false);
    }

    /**
     * Transfer the extensions of a network to another one.
     * @param from the network whose extensions must be transferred
     * @param to the destination network
     * @param ignoreAlreadyPresent should an extension to transfer be ignored if already present in {@code to}?
     */
    protected static void transferExtensions(Network from, Network to, boolean ignoreAlreadyPresent) {
        // Only well-defined extensions (with an interface) are transferred
        new ArrayList<Extension<?>>(from.getExtensions()).forEach(e -> {
            Stream<Class<?>> stream = Arrays.stream(e.getClass().getInterfaces())
                    .filter(c -> Objects.nonNull(from.getExtension(c)));
            if (ignoreAlreadyPresent) {
                stream = stream.filter(c -> to.getExtension(c) == null);
            }
            stream.forEach(clazz -> {
                from.removeExtension((Class<? extends Extension<Network>>) clazz);
                to.addExtension((Class<? super Extension<Network>>) clazz, (Extension<Network>) e);
            });
        });
    }

    /**
     * Transfer the properties of a network to another one.
     * @param fromNetwork the network whose properties must be transferred
     * @param toNetwork the destination network
     */
    protected static void transferProperties(AbstractNetwork fromNetwork, AbstractNetwork toNetwork) {
        transferProperties(fromNetwork, toNetwork, false);
    }

    /**
     * Transfer the properties of a network to another one.
     * @param fromNetwork the network whose properties must be transferred
     * @param toNetwork the destination network
     * @param ignoreAlreadyPresent should a property to transfer be ignored if already present in {@code toNetwork}?
     */
    protected static void transferProperties(AbstractNetwork fromNetwork, AbstractNetwork toNetwork, boolean ignoreAlreadyPresent) {
        Stream<Map.Entry<Object, Object>> stream = fromNetwork.getProperties().entrySet().stream();
        if (ignoreAlreadyPresent) {
            stream = stream.filter(e -> !toNetwork.hasProperty(e.getKey().toString()));
        }
        stream.forEach(e -> {
            toNetwork.setProperty(e.getKey().toString(), e.getValue().toString());
            fromNetwork.removeProperty(e.getKey().toString());
        });
    }

    abstract class AbstractBusBreakerViewImpl implements BusBreakerView {
        @Override
        public Iterable<Bus> getBuses() {
            return FluentIterable.from(getVoltageLevels())
                    .transformAndConcat(vl -> vl.getBusBreakerView().getBuses());
        }

        @Override
        public Stream<Bus> getBusStream() {
            return getVoltageLevelStream().flatMap(vl -> vl.getBusBreakerView().getBusStream());
        }

        @Override
        public int getBusCount() {
            return getVoltageLevelStream().mapToInt(vl -> vl.getBusBreakerView().getBusCount()).sum();
        }

        @Override
        public Iterable<Switch> getSwitches() {
            return FluentIterable.from(getVoltageLevels())
                    .transformAndConcat(vl -> vl.getBusBreakerView().getSwitches());
        }

        @Override
        public Stream<Switch> getSwitchStream() {
            return getVoltageLevelStream().flatMap(vl -> vl.getBusBreakerView().getSwitchStream());
        }

        @Override
        public int getSwitchCount() {
            return getVoltageLevelStream().mapToInt(vl -> vl.getBusBreakerView().getSwitchCount()).sum();
        }
    }

    abstract class AbstractBusViewImpl implements BusView {
        @Override
        public Iterable<Bus> getBuses() {
            return FluentIterable.from(getVoltageLevels())
                    .transformAndConcat(vl -> vl.getBusView().getBuses());
        }

        @Override
        public Stream<Bus> getBusStream() {
            return getVoltageLevelStream().flatMap(vl -> vl.getBusView().getBusStream());
        }
    }
}
