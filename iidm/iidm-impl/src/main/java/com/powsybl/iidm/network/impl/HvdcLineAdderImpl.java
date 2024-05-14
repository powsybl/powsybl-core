/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.commons.ref.Ref;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class HvdcLineAdderImpl extends AbstractIdentifiableAdder<HvdcLineAdderImpl> implements HvdcLineAdder {

    private final NetworkImpl network;
    private final String subnetwork;

    private double r = Double.NaN;

    private HvdcLine.ConvertersMode convertersMode;

    private double nominalV = Double.NaN;

    private double activePowerSetpoint = Double.NaN;

    private double maxP = Double.NaN;

    private String converterStationId1;

    private String converterStationId2;

    public HvdcLineAdderImpl(NetworkImpl network, String subnetwork) {
        this.network = Objects.requireNonNull(network);
        this.subnetwork = subnetwork;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return network;
    }

    @Override
    protected String getTypeDescription() {
        return HvdcLineImpl.TYPE_DESCRIPTION;
    }

    @Override
    public HvdcLineAdder setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public HvdcLineAdder setConvertersMode(HvdcLine.ConvertersMode convertersMode) {
        this.convertersMode = convertersMode;
        return this;
    }

    @Override
    public HvdcLineAdder setNominalV(double nominalV) {
        this.nominalV = nominalV;
        return this;
    }

    @Override
    public HvdcLineAdder setActivePowerSetpoint(double activePowerSetpoint) {
        this.activePowerSetpoint = activePowerSetpoint;
        return this;
    }

    @Override
    public HvdcLineAdder setMaxP(double maxP) {
        this.maxP = maxP;
        return this;
    }

    @Override
    public HvdcLineAdder setConverterStationId1(String converterStationId1) {
        this.converterStationId1 = converterStationId1;
        return this;
    }

    @Override
    public HvdcLineAdder setConverterStationId2(String converterStationId2) {
        this.converterStationId2 = converterStationId2;
        return this;
    }

    @Override
    public HvdcLine add() {
        String id = checkAndGetUniqueId();
        String name = getName();
        ValidationUtil.checkR(this, r);
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkConvertersMode(this, convertersMode, network.getMinValidationLevel().compareTo(ValidationLevel.STEADY_STATE_HYPOTHESIS) >= 0));
        ValidationUtil.checkNominalV(this, nominalV);
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkHvdcActivePowerSetpoint(this, activePowerSetpoint, network.getMinValidationLevel().compareTo(ValidationLevel.STEADY_STATE_HYPOTHESIS) >= 0));
        ValidationUtil.checkHvdcMaxP(this, maxP);
        AbstractHvdcConverterStation<?> converterStation1 = network.getHvdcConverterStation(converterStationId1);
        if (converterStation1 == null) {
            throw new PowsyblException("Side 1 converter station " + converterStationId1 + " not found");
        }
        AbstractHvdcConverterStation<?> converterStation2 = network.getHvdcConverterStation(converterStationId2);
        if (converterStation2 == null) {
            throw new PowsyblException("Side 2 converter station " + converterStationId2 + " not found");
        }
        VoltageLevelExt vl1 = converterStation1.getTerminal().getVoltageLevel();
        VoltageLevelExt vl2 = converterStation2.getTerminal().getVoltageLevel();
        if (subnetwork != null && (!subnetwork.equals(vl1.getSubnetworkId()) || !subnetwork.equals(vl2.getSubnetworkId()))) {
            throw new ValidationException(this, "The converter stations are not in the subnetwork '" +
                    subnetwork + "'. Create this Hvdc line from the parent network '" + getNetwork().getId() + "'");
        }
        Ref<NetworkImpl> networkRef = computeNetworkRef(network, vl1, vl2);
        HvdcLineImpl hvdcLine = new HvdcLineImpl(id, name, isFictitious(), r, nominalV, maxP, convertersMode, activePowerSetpoint,
                                                 converterStation1, converterStation2, networkRef);
        network.getIndex().checkAndAdd(hvdcLine);
        network.getListeners().notifyCreation(hvdcLine);
        return hvdcLine;
    }

}
