/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.LegConnectionType;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescue;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescueAdder;

import java.util.Objects;

import static com.powsybl.iidm.network.extensions.FortescueConstants.*;

/**
 * @author Jean-Baptiste Heyberger <jbheyberger at gmail.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TwoWindingsTransformerFortescueAdderImpl extends AbstractExtensionAdder<TwoWindingsTransformer, TwoWindingsTransformerFortescue> implements TwoWindingsTransformerFortescueAdder {

    private boolean partOfGeneratingUnit = false;
    private double ro = Double.NaN;
    private double xo = Double.NaN;
    private boolean freeFluxes = DEFAULT_FREE_FLUXES;
    private LegConnectionType leg1ConnectionType = DEFAULT_LEG1_CONNECTION_TYPE;
    private LegConnectionType leg2ConnectionType = DEFAULT_LEG2_CONNECTION_TYPE;
    private double groundingR1 = DEFAULT_GROUNDING_R;
    private double groundingX1 = DEFAULT_GROUNDING_X;
    private double groundingR2 = DEFAULT_GROUNDING_R;
    private double groundingX2 = DEFAULT_GROUNDING_X;

    public TwoWindingsTransformerFortescueAdderImpl(TwoWindingsTransformer twt) {
        super(twt);
    }

    @Override
    public Class<? super TwoWindingsTransformerFortescue> getExtensionClass() {
        return TwoWindingsTransformerFortescue.class;
    }

    @Override
    protected TwoWindingsTransformerFortescueImpl createExtension(TwoWindingsTransformer twt) {
        return new TwoWindingsTransformerFortescueImpl(twt, partOfGeneratingUnit, ro, xo, freeFluxes, leg1ConnectionType, leg2ConnectionType, groundingR1, groundingX1, groundingR2, groundingX2);
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withPartOfGeneratingUnit(boolean partOfGeneratingUnit) {
        this.partOfGeneratingUnit = partOfGeneratingUnit;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withRo(double ro) {
        this.ro = ro;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withXo(double xo) {
        this.xo = xo;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withFreeFluxes(boolean freeFluxes) {
        this.freeFluxes = freeFluxes;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withLeg1ConnectionType(LegConnectionType leg1ConnectionType) {
        this.leg1ConnectionType = Objects.requireNonNull(leg1ConnectionType);
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withLeg2ConnectionType(LegConnectionType leg2ConnectionType) {
        this.leg2ConnectionType = Objects.requireNonNull(leg2ConnectionType);
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withGroundingR1(double groundingR1) {
        this.groundingR1 = groundingR1;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withGroundingX1(double groundingX1) {
        this.groundingX1 = groundingX1;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withGroundingR2(double groundingR2) {
        this.groundingR2 = groundingR2;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withGroundingX2(double groundingX2) {
        this.groundingX2 = groundingX2;
        return this;
    }
}
