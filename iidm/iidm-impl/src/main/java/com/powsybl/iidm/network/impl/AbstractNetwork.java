/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Override
    public void splitLinksBetween(String networkId1, String networkId2) {
        splitEquipments(getLinksBetween(networkId1, networkId2));
    }

    @Override
    public void splitLinksBetween(Network network1, Network network2) {
        splitEquipments(getLinksBetween(network1, network2));
    }

    private void splitEquipments(Map<Class<? extends Identifiable<?>>, Set<Identifiable<?>>> equipmentsByClass) {
        splitEquipments(equipmentsByClass.values().stream().flatMap(Set::stream).collect(Collectors.toSet()));
    }

    @Override
    public void splitEquipments(Collection<Identifiable<?>> equipments) {
        for (Identifiable<?> equipment : equipments) {
            if (equipment.getType() == IdentifiableType.TIE_LINE) {
                //TODO subnetworks API
                throw new UnsupportedOperationException("Not yet implemented");
            } else {
                throw new PowsyblException("This equipment cannot be split.");
            }
        }
    }
}
