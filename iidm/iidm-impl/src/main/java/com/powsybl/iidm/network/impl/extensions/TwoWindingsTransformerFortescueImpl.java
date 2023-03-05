/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.LegConnectionType;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescue;

import java.util.Objects;

/**
 * @author Jean-Baptiste Heyberger <jbheyberger at gmail.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TwoWindingsTransformerFortescueImpl extends AbstractExtension<TwoWindingsTransformer> implements TwoWindingsTransformerFortescue {

    private double ro;
    private double xo;
    private boolean freeFluxes; // free fluxes mean that magnetizing impedance Zm is infinite, by default, fluxes are forced and Zm exists
    private LegConnectionType leg1ConnectionType;
    private LegConnectionType leg2ConnectionType;
    private boolean partOfGeneratingUnit;
    private double groundingR1;
    private double groundingX1;
    private double groundingR2;
    private double groundingX2;

    public TwoWindingsTransformerFortescueImpl(TwoWindingsTransformer twt, boolean partOfGeneratingUnit, double ro, double xo, boolean freeFluxes,
                                               LegConnectionType leg1ConnectionType, LegConnectionType leg2ConnectionType, double groundingR1, double groundingX1, double groundingR2, double groundingX2) {
        super(twt);
        this.partOfGeneratingUnit = partOfGeneratingUnit;
        this.ro = ro;
        this.xo = xo;
        this.freeFluxes = freeFluxes;
        this.leg1ConnectionType = Objects.requireNonNull(leg1ConnectionType);
        this.leg2ConnectionType = Objects.requireNonNull(leg2ConnectionType);
        this.groundingR1 = groundingR1;
        this.groundingX1 = groundingX1;
        this.groundingR2 = groundingR2;
        this.groundingX2 = groundingX2;
    }

    @Override
    public double getRo() {
        return ro;
    }

    @Override
    public void setRo(double ro) {
        this.ro = ro;
    }

    @Override
    public double getXo() {
        return xo;
    }

    @Override
    public void setXo(double xo) {
        this.xo = xo;
    }

    @Override
    public boolean isFreeFluxes() {
        return freeFluxes;
    }

    @Override
    public void setFreeFluxes(boolean freeFluxes) {
        this.freeFluxes = freeFluxes;
    }

    @Override
    public LegConnectionType getLeg1ConnectionType() {
        return leg1ConnectionType;
    }

    @Override
    public void setLeg1ConnectionType(LegConnectionType leg1ConnectionType) {
        this.leg1ConnectionType = Objects.requireNonNull(leg1ConnectionType);
    }

    @Override
    public LegConnectionType getLeg2ConnectionType() {
        return leg2ConnectionType;
    }

    @Override
    public void setLeg2ConnectionType(LegConnectionType leg2ConnectionType) {
        this.leg2ConnectionType = Objects.requireNonNull(leg2ConnectionType);
    }

    @Override
    public boolean isPartOfGeneratingUnit() {
        return partOfGeneratingUnit;
    }

    @Override
    public void setPartOfGeneratingUnit(boolean partOfGeneratingUnit) {
        this.partOfGeneratingUnit = partOfGeneratingUnit;
    }

    @Override
    public double getGroundingR1() {
        return groundingR1;
    }

    @Override
    public void setGroundingR1(double groundingR1) {
        this.groundingR1 = groundingR1;
    }

    @Override
    public double getGroundingR2() {
        return groundingR2;
    }

    @Override
    public void setGroundingR2(double groundingR2) {
        this.groundingR2 = groundingR2;
    }

    @Override
    public double getGroundingX1() {
        return groundingX1;
    }

    @Override
    public void setGroundingX1(double groundingX1) {
        this.groundingX1 = groundingX1;
    }

    @Override
    public double getGroundingX2() {
        return groundingX2;
    }

    @Override
    public void setGroundingX2(double groundingX2) {
        this.groundingX2 = groundingX2;
    }
}
