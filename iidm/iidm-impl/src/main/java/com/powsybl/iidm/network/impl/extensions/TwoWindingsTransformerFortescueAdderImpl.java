/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.WindingConnectionType;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescue;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescueAdder;

import java.util.Objects;

import static com.powsybl.iidm.network.extensions.FortescueConstants.*;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class TwoWindingsTransformerFortescueAdderImpl extends AbstractExtensionAdder<TwoWindingsTransformer, TwoWindingsTransformerFortescue> implements TwoWindingsTransformerFortescueAdder {

    private double rz = Double.NaN;
    private double xz = Double.NaN;
    private double rz1 = Double.NaN;
    private double xz1 = Double.NaN;
    private double rz2 = Double.NaN;
    private double xz2 = Double.NaN;
    private boolean freeFluxes = DEFAULT_FREE_FLUXES;
    private double xmz = Double.NaN;
    private WindingConnectionType connectionType1 = DEFAULT_LEG1_CONNECTION_TYPE;
    private WindingConnectionType connectionType2 = DEFAULT_LEG2_CONNECTION_TYPE;
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
        return new TwoWindingsTransformerFortescueImpl(twt, rz, xz, rz1, xz1, rz2, xz2, freeFluxes, xmz, connectionType1, connectionType2, groundingR1, groundingX1, groundingR2, groundingX2);
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withRz(double rz) {
        this.rz = rz;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withXz(double xz) {
        this.xz = xz;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withRz1(double rz1) {
        this.rz1 = rz1;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withXz1(double xz1) {
        this.xz1 = xz1;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withRz2(double rz2) {
        this.rz2 = rz2;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withXz2(double xz2) {
        this.xz2 = xz2;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withFreeFluxes(boolean freeFluxes) {
        this.freeFluxes = freeFluxes;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withXmz(double xmz) {
        this.xmz = xmz;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withConnectionType1(WindingConnectionType connectionType1) {
        this.connectionType1 = Objects.requireNonNull(connectionType1);
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withConnectionType2(WindingConnectionType connectionType2) {
        this.connectionType2 = Objects.requireNonNull(connectionType2);
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
