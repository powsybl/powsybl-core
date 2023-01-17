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

    private final Ref<NetworkImpl> networkRef;
    private final SubstationImpl substation;
    private final String subNetwork;

    private double r = Double.NaN;

    private double x = Double.NaN;

    private double g = 0.0;

    private double b = 0.0;

    private double ratedU1 = Double.NaN;

    private double ratedU2 = Double.NaN;

    private double ratedS = Double.NaN;

    TwoWindingsTransformerAdderImpl(SubstationImpl substation) {
        networkRef = null;
        this.substation = substation;
        this.subNetwork = substation.getSubNetwork();
    }

    TwoWindingsTransformerAdderImpl(Ref<NetworkImpl> networkRef, String subNetwork) {
        this.networkRef = networkRef;
        substation = null;
        this.subNetwork = subNetwork;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return Optional.ofNullable(networkRef)
                .map(Ref::get)
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
        if (voltageLevel1.getClosestNetwork() != voltageLevel2.getClosestNetwork()) {
            LOG.warn("Transformer '{}' is between two different sub-networks: splitting back the network will not be possible." +
                    " If you want to be able to split the network, delete this transformer and create a tie-line instead", id);
        }
        if (subNetwork != null && (!subNetwork.equals(voltageLevel1.getSubNetwork()) || !subNetwork.equals(voltageLevel2.getSubNetwork()))) {
            throw new ValidationException(this, "Transformer '" + id + "' is not contained in sub-network '" +
                    subNetwork + "'. Create this line from the parent network '" + getNetwork().getId() + "'");
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

        TwoWindingsTransformerImpl transformer
                = new TwoWindingsTransformerImpl(substation != null ? substation.getNetwork().getRef() : networkRef, id, getName(), isFictitious(),
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
