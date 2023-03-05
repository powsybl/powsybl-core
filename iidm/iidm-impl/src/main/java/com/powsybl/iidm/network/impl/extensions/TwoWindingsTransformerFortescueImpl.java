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
    private double r1Ground;
    private double x1Ground;
    private double r2Ground;
    private double x2Ground;

    public TwoWindingsTransformerFortescueImpl(TwoWindingsTransformer twt, boolean partOfGeneratingUnit, double ro, double xo, boolean freeFluxes,
                                               LegConnectionType leg1ConnectionType, LegConnectionType leg2ConnectionType, double r1Ground, double x1Ground, double r2Ground, double x2Ground) {
        super(twt);
        this.partOfGeneratingUnit = partOfGeneratingUnit;
        this.ro = ro;
        this.xo = xo;
        this.freeFluxes = freeFluxes;
        this.leg1ConnectionType = Objects.requireNonNull(leg1ConnectionType);
        this.leg2ConnectionType = Objects.requireNonNull(leg2ConnectionType);
        this.r1Ground = r1Ground;
        this.x1Ground = x1Ground;
        this.r2Ground = r2Ground;
        this.x2Ground = x2Ground;
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
    public double getR1Ground() {
        return r1Ground;
    }

    @Override
    public void setR1Ground(double r1Ground) {
        this.r1Ground = r1Ground;
    }

    @Override
    public double getR2Ground() {
        return r2Ground;
    }

    @Override
    public void setR2Ground(double r2Ground) {
        this.r2Ground = r2Ground;
    }

    @Override
    public double getX1Ground() {
        return x1Ground;
    }

    @Override
    public void setX1Ground(double x1Ground) {
        this.x1Ground = x1Ground;
    }

    @Override
    public double getX2Ground() {
        return x2Ground;
    }

    @Override
    public void setX2Ground(double x2Ground) {
        this.x2Ground = x2Ground;
    }
}
