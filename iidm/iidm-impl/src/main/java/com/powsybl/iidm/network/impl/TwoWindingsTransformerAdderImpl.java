/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
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

import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TwoWindingsTransformerAdderImpl extends AbstractBranchAdder<TwoWindingsTransformerAdderImpl> implements TwoWindingsTransformerAdder {

    private static final Logger LOG = LoggerFactory.getLogger(TwoWindingsTransformerAdderImpl.class);

    private final NetworkImpl network;
    private final SubstationImpl substation;
    private final String subnetwork;

    private double r = Double.NaN;

    private double x = Double.NaN;

    private double g = 0.0;

    private double b = 0.0;

    private double ratedU1 = Double.NaN;

    private double ratedU2 = Double.NaN;

    private double ratedS = Double.NaN;

    TwoWindingsTransformerAdderImpl(SubstationImpl substation) {
        network = null;
        this.substation = substation;
        this.subnetwork = substation.getSubnetwork();
    }

    TwoWindingsTransformerAdderImpl(NetworkImpl network, String subnetwork) {
        this.network = network;
        substation = null;
        this.subnetwork = subnetwork;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return Optional.ofNullable(network)
                .orElseGet(() -> Optional.ofNullable(substation)
                        .map(SubstationImpl::getNetwork)
                        .orElseThrow(() -> new PowsyblException("Two windings transformer has no container")));
    }

    @Override
    protected String getTypeDescription() {
        return "2 windings transformer";
    }

    @Override
    public TwoWindingsTransformerAdder setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setB(double b) {
        this.b = b;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setG(double g) {
        this.g = g;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setRatedU1(double ratedU1) {
        this.ratedU1 = ratedU1;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setRatedU2(double ratedU2) {
        this.ratedU2 = ratedU2;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setRatedS(double ratedS) {
        this.ratedS = ratedS;
        return this;
    }

    @Override
    public TwoWindingsTransformer add() {
        String id = checkAndGetUniqueId();
        checkConnectableBuses();
        VoltageLevelExt voltageLevel1 = checkAndGetVoltageLevel1();
        VoltageLevelExt voltageLevel2 = checkAndGetVoltageLevel2();
        if (voltageLevel1.getParentNetwork() != voltageLevel2.getParentNetwork()) {
            throw new ValidationException(this,
                    "The 2 windings of the transformer shall belong to the same subnetwork ('"
                            + voltageLevel1.getParentNetwork().getId() + "', '" + voltageLevel2.getParentNetwork().getId() + "')");
        }
        if (subnetwork != null && (!subnetwork.equals(voltageLevel1.getSubnetwork()) || !subnetwork.equals(voltageLevel2.getSubnetwork()))) {
            throw new ValidationException(this, "Transformer '" + id + "' is not contained in sub-network '" +
                    subnetwork + "'. Create this line from the parent network '" + getNetwork().getId() + "'");
        }
        if (substation != null) {
            if (voltageLevel1.getSubstation().map(s -> s != substation).orElse(true) || voltageLevel2.getSubstation().map(s -> s != substation).orElse(true)) {
                throw new ValidationException(this,
                        "the 2 windings of the transformer shall belong to the substation '"
                                + substation.getId() + "' ('" + voltageLevel1.getSubstation().map(Substation::getId).orElse("null") + "', '"
                                + voltageLevel2.getSubstation().map(Substation::getId).orElse("null") + "')");
            }
        } else if (voltageLevel1.getSubstation().isPresent() || voltageLevel2.getSubstation().isPresent()) {
            throw new ValidationException(this,
                    "the 2 windings of the transformer shall belong to a substation since there are located in voltage levels with substations ('"
                            + voltageLevel1.getId() + "', '" + voltageLevel2.getId() + "')");
        }
        if (Double.isNaN(ratedU1)) {
            ratedU1 = voltageLevel1.getNominalV();
        }
        if (Double.isNaN(ratedU2)) {
            ratedU2 = voltageLevel2.getNominalV();
        }
        TerminalExt terminal1 = checkAndGetTerminal1();
        TerminalExt terminal2 = checkAndGetTerminal2();

        ValidationUtil.checkR(this, r);
        ValidationUtil.checkX(this, x);
        ValidationUtil.checkG(this, g);
        ValidationUtil.checkB(this, b);
        ValidationUtil.checkRatedU1(this, ratedU1);
        ValidationUtil.checkRatedU2(this, ratedU2);
        ValidationUtil.checkRatedS(this, ratedS);

        NetworkImpl n = substation != null ? substation.getNetwork() : network;
        Ref<NetworkImpl> networkRef = computeNetworkRef(n, voltageLevel1, voltageLevel2);
        TwoWindingsTransformerImpl transformer
                = new TwoWindingsTransformerImpl(networkRef, id, getName(), isFictitious(),
                (SubstationImpl) voltageLevel1.getSubstation().orElse(null),
                r, x, g, b,
                ratedU1, ratedU2, ratedS);
        terminal1.setNum(1);
        terminal2.setNum(2);
        transformer.addTerminal(terminal1);
        transformer.addTerminal(terminal2);

        // check that the two windings transformer is attachable on both side
        voltageLevel1.attach(terminal1, true);
        voltageLevel2.attach(terminal2, true);

        voltageLevel1.attach(terminal1, false);
        voltageLevel2.attach(terminal2, false);
        getNetwork().getIndex().checkAndAdd(transformer);
        getNetwork().getListeners().notifyCreation(transformer);
        return transformer;

    }

}
