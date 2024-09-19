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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
abstract class AbstractNetwork extends AbstractIdentifiable<Network> implements NetworkExt {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNetwork.class);

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
        new ArrayList<Extension<?>>(from.getExtensions()).forEach(e ->
            Arrays.stream(e.getClass().getInterfaces())
                    .filter(c -> Objects.nonNull(from.getExtension(c)))
                    .forEach(clazz -> {
                        if (ignoreAlreadyPresent && to.getExtension(clazz) != null) {
                            LOGGER.warn("Extension of class \"{}\" was not transferred from \"{}\" to \"{}\": " +
                                            "an extension of this same class already exists in the destination network.",
                                    clazz.getName(), from.getId(), to.getId());
                        } else {
                            from.removeExtension((Class<? extends Extension<Network>>) clazz);
                            to.addExtension((Class<? super Extension<Network>>) clazz, (Extension<Network>) e);
                        }
                    })
        );
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
        fromNetwork.getProperties().forEach((key, value) -> {
            if (ignoreAlreadyPresent && toNetwork.hasProperty(key.toString())) {
                LOGGER.warn("Property \"{}\" was not transferred from \"{}\" to \"{}\": it already exists in the destination network.",
                        key, fromNetwork.getId(), toNetwork.getId());
            } else {
                toNetwork.setProperty(key.toString(), value.toString());
                fromNetwork.removeProperty(key.toString());
            }
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
