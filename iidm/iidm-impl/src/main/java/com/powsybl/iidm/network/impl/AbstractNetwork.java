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
import com.powsybl.iidm.network.util.Networks;
import org.joda.time.DateTime;

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
     * {@inheritDoc}
     * <p>For now, only tie-lines can be split (HVDC lines may be supported later).</p>
     */
    @Override
    public abstract Network detach();

    /**
     * {@inheritDoc}
     * For now, only tie-lines can be split (HVDC lines may be supported later).
     * @see Networks#getDetachPreventingEquipments(Network)
     */
    @Override
    public boolean isSplittableEquipment(Identifiable<?> identifiable) {
        return identifiable.getType() == IdentifiableType.TIE_LINE;
    }

    protected void splitEquipment(Identifiable<?> equipment) {
        if (!isSplittableEquipment(equipment)) {
            throw new PowsyblException("This equipment cannot be split.");
        }
        if (equipment.getType() == IdentifiableType.TIE_LINE) {
            //TODO subnetworks API
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

}
