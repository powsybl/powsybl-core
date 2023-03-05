/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.LegConnectionType;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerFortescue;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerFortescueAdder;

import java.util.Objects;

import static com.powsybl.iidm.network.extensions.FortescueConstants.*;

/**
 * @author Jean-Baptiste Heyberger <jbheyberger at gmail.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ThreeWindingsTransformerFortescueAdderImpl extends AbstractExtensionAdder<ThreeWindingsTransformer, ThreeWindingsTransformerFortescue> implements ThreeWindingsTransformerFortescueAdder {

    private double leg1Ro = Double.NaN;
    private double leg2Ro = Double.NaN;
    private double leg3Ro = Double.NaN;
    private double leg1Xo = Double.NaN;
    private double leg2Xo = Double.NaN;
    private double leg3Xo = Double.NaN;
    private boolean leg1FreeFluxes = DEFAULT_FREE_FLUXES;
    private boolean leg2FreeFluxes = DEFAULT_FREE_FLUXES;
    private boolean leg3FreeFluxes = DEFAULT_FREE_FLUXES;
    private LegConnectionType leg1ConnectionType = DEFAULT_LEG1_CONNECTION_TYPE;
    private LegConnectionType leg2ConnectionType = DEFAULT_LEG2_CONNECTION_TYPE;
    private LegConnectionType leg3ConnectionType = DEFAULT_LEG3_CONNECTION_TYPE;
    private double leg1GroundingR = DEFAULT_GROUNDING_R;
    private double leg1GroundingX = DEFAULT_GROUNDING_X;
    private double leg2GroundingR = DEFAULT_GROUNDING_R;
    private double leg2GroundingX = DEFAULT_GROUNDING_X;
    private double leg3GroundingR = DEFAULT_GROUNDING_R;
    private double leg3GroundingX = DEFAULT_GROUNDING_X;

    public ThreeWindingsTransformerFortescueAdderImpl(ThreeWindingsTransformer twt) {
        super(twt);
    }

    @Override
    public Class<? super ThreeWindingsTransformerFortescue> getExtensionClass() {
        return ThreeWindingsTransformerFortescue.class;
    }

    @Override
    protected ThreeWindingsTransformerFortescueImpl createExtension(ThreeWindingsTransformer twt) {
        var leg1 = new ThreeWindingsTransformerFortescue.LegFortescue(leg1Ro, leg1Xo, leg1FreeFluxes, leg1ConnectionType, leg1GroundingR, leg1GroundingX);
        var leg2 = new ThreeWindingsTransformerFortescue.LegFortescue(leg2Ro, leg2Xo, leg2FreeFluxes, leg2ConnectionType, leg2GroundingR, leg2GroundingX);
        var leg3 = new ThreeWindingsTransformerFortescue.LegFortescue(leg3Ro, leg3Xo, leg3FreeFluxes, leg3ConnectionType, leg3GroundingR, leg3GroundingX);
        return new ThreeWindingsTransformerFortescueImpl(twt, leg1, leg2, leg3);
    }

    @Override
    public ThreeWindingsTransformerFortescueAdderImpl withLeg1Ro(double leg1Ro) {
        this.leg1Ro = leg1Ro;
        return this;
    }

    @Override
    public ThreeWindingsTransformerFortescueAdderImpl withLeg2Ro(double leg2Ro) {
        this.leg2Ro = leg2Ro;
        return this;
    }

    @Override
    public ThreeWindingsTransformerFortescueAdderImpl withLeg3Ro(double leg3Ro) {
        this.leg3Ro = leg3Ro;
        return this;
    }

    @Override
    public ThreeWindingsTransformerFortescueAdderImpl withLeg1Xo(double leg1Xo) {
        this.leg1Xo = leg1Xo;
        return this;
    }

    @Override
    public ThreeWindingsTransformerFortescueAdderImpl withLeg2Xo(double leg2Xo) {
        this.leg2Xo = leg2Xo;
        return this;
    }

    @Override
    public ThreeWindingsTransformerFortescueAdderImpl withLeg3Xo(double leg3Xo) {
        this.leg3Xo = leg3Xo;
        return this;
    }

    @Override
    public ThreeWindingsTransformerFortescueAdderImpl withLeg1FreeFluxes(boolean leg1FreeFluxes) {
        this.leg1FreeFluxes = leg1FreeFluxes;
        return this;
    }

    @Override
    public ThreeWindingsTransformerFortescueAdderImpl withLeg2FreeFluxes(boolean leg2FreeFluxes) {
        this.leg2FreeFluxes = leg2FreeFluxes;
        return this;
    }

    @Override
    public ThreeWindingsTransformerFortescueAdderImpl withLeg3FreeFluxes(boolean leg3FreeFluxes) {
        this.leg3FreeFluxes = leg3FreeFluxes;
        return this;
    }

    @Override
    public ThreeWindingsTransformerFortescueAdderImpl withLeg1ConnectionType(LegConnectionType leg1ConnectionType) {
        this.leg1ConnectionType = Objects.requireNonNull(leg1ConnectionType);
        return this;
    }

    @Override
    public ThreeWindingsTransformerFortescueAdderImpl withLeg2ConnectionType(LegConnectionType leg2ConnectionType) {
        this.leg2ConnectionType = Objects.requireNonNull(leg2ConnectionType);
        return this;
    }

    @Override
    public ThreeWindingsTransformerFortescueAdderImpl withLeg3ConnectionType(LegConnectionType leg3ConnectionType) {
        this.leg3ConnectionType = Objects.requireNonNull(leg3ConnectionType);
        return this;
    }

    @Override
    public ThreeWindingsTransformerFortescueAdderImpl withLeg1GroundingR(double leg1GroundingR) {
        this.leg1GroundingR = leg1GroundingR;
        return this;
    }

    @Override
    public ThreeWindingsTransformerFortescueAdderImpl withLeg1GroundingX(double leg1GroundingX) {
        this.leg1GroundingX = leg1GroundingX;
        return this;
    }

    @Override
    public ThreeWindingsTransformerFortescueAdderImpl withLeg2GroundingR(double leg2GroundingR) {
        this.leg2GroundingR = leg2GroundingR;
        return this;
    }

    @Override
    public ThreeWindingsTransformerFortescueAdderImpl withLeg2GroundingX(double leg2GroundingX) {
        this.leg2GroundingX = leg2GroundingX;
        return this;
    }

    @Override
    public ThreeWindingsTransformerFortescueAdderImpl withLeg3GroundingR(double leg3GroundingR) {
        this.leg3GroundingR = leg3GroundingR;
        return this;
    }

    @Override
    public ThreeWindingsTransformerFortescueAdderImpl withLeg3GroundingX(double leg3GroundingX) {
        this.leg3GroundingX = leg3GroundingX;
        return this;
    }
}
