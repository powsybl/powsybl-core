/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
abstract class AbstractNetwork extends AbstractIdentifiable<Network> implements Network {

    private DateTime caseDate = new DateTime(); // default is the time at which the network has been created

    private int forecastDistance = 0;

    protected String sourceFormat;

    AbstractNetwork(String id, String name, String sourceFormat) {
        super(id, name);
        Objects.requireNonNull(sourceFormat, "source format is null");
        this.sourceFormat = sourceFormat;
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.NETWORK;
    }

    @Override
    public DateTime getCaseDate() {
        return caseDate;
    }

    @Override
    public Network setCaseDate(DateTime caseDate) {
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
        new ArrayList<>(from.getExtensions())
                .forEach(e -> Arrays.stream(e.getClass().getInterfaces())
                        .filter(c -> Objects.nonNull(from.getExtension(c)))
                        .forEach(clazz -> {
                            from.removeExtension((Class<? extends Extension<Network>>) clazz);
                            to.addExtension((Class<? super Extension<Network>>) clazz, (Extension<Network>) e);
                        }));
    }
}
