/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class HvdcLineAdderImpl extends AbstractIdentifiableAdder<HvdcLineAdderImpl> implements HvdcLineAdder {

    private static final Logger LOG = LoggerFactory.getLogger(HvdcLineAdderImpl.class);

    private final Ref<NetworkImpl> networkRef;
    private final String subNetwork;

    private double r = Double.NaN;

    private HvdcLine.ConvertersMode convertersMode;

    private double nominalV = Double.NaN;

    private double activePowerSetpoint = Double.NaN;

    private double maxP = Double.NaN;

    private String converterStationId1;

    private String converterStationId2;

    public HvdcLineAdderImpl(Ref<NetworkImpl> networkRef, String subNetwork) {
        this.networkRef = Objects.requireNonNull(networkRef);
        this.subNetwork = subNetwork;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return networkRef.get();
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
        NetworkImpl network = getNetwork();
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
        if (vl1.getClosestNetwork() != vl2.getClosestNetwork()) {
            LOG.warn("HVDC Line '{}' is between two different sub-networks: splitting back the network will not be possible.", id);
        }
        if (subNetwork != null && (!subNetwork.equals(vl1.getSubNetwork()) || !subNetwork.equals(vl2.getSubNetwork()))) {
            throw new ValidationException(this, "HVDC Line '" + id + "' is not contained in sub-network '" +
                    subNetwork + "'. Create this HVDC line from the parent network '" + getNetwork().getId() + "'");
        }
        HvdcLineImpl hvdcLine = new HvdcLineImpl(id, name, isFictitious(), r, nominalV, maxP, convertersMode, activePowerSetpoint,
                                                 converterStation1, converterStation2, networkRef);
        network.getIndex().checkAndAdd(hvdcLine);
        network.getListeners().notifyCreation(hvdcLine);
        return hvdcLine;
    }

}
