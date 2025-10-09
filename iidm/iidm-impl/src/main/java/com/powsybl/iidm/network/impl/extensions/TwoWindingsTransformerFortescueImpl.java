/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.WindingConnectionType;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescue;

import java.util.Objects;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class TwoWindingsTransformerFortescueImpl extends AbstractExtension<TwoWindingsTransformer> implements TwoWindingsTransformerFortescue {

    private double rz;
    private double xz;
    private boolean freeFluxes;
    private WindingConnectionType connectionType1;
    private WindingConnectionType connectionType2;
    private double groundingR1;
    private double groundingX1;
    private double groundingR2;
    private double groundingX2;

    public TwoWindingsTransformerFortescueImpl(TwoWindingsTransformer twt, double rz, double xz, boolean freeFluxes,
                                               WindingConnectionType connectionType1, WindingConnectionType connectionType2,
                                               double groundingR1, double groundingX1, double groundingR2, double groundingX2) {
        super(twt);
        this.rz = rz;
        this.xz = xz;
        this.freeFluxes = freeFluxes;
        this.connectionType1 = Objects.requireNonNull(connectionType1);
        this.connectionType2 = Objects.requireNonNull(connectionType2);
        this.groundingR1 = groundingR1;
        this.groundingX1 = groundingX1;
        this.groundingR2 = groundingR2;
        this.groundingX2 = groundingX2;
    }

    @Override
    public double getRz() {
        return rz;
    }

    @Override
    public void setRz(double rz) {
        this.rz = rz;
    }

    @Override
    public double getXz() {
        return xz;
    }

    @Override
    public void setXz(double xz) {
        this.xz = xz;
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
    public WindingConnectionType getConnectionType1() {
        return connectionType1;
    }

    @Override
    public void setConnectionType1(WindingConnectionType connectionType1) {
        this.connectionType1 = Objects.requireNonNull(connectionType1);
    }

    @Override
    public WindingConnectionType getConnectionType2() {
        return connectionType2;
    }

    @Override
    public void setConnectionType2(WindingConnectionType connectionType2) {
        this.connectionType2 = Objects.requireNonNull(connectionType2);
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
