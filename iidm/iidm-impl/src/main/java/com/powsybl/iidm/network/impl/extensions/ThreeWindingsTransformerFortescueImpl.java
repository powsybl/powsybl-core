/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerFortescue;

import java.util.Objects;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ThreeWindingsTransformerFortescueImpl extends AbstractExtension<ThreeWindingsTransformer> implements ThreeWindingsTransformerFortescue {

    private final LegFortescue leg1;
    private final LegFortescue leg2;
    private final LegFortescue leg3;

    public ThreeWindingsTransformerFortescueImpl(ThreeWindingsTransformer extendable,
                                                 LegFortescue leg1, LegFortescue leg2, LegFortescue leg3) {
        super(extendable);
        this.leg1 = Objects.requireNonNull(leg1);
        this.leg2 = Objects.requireNonNull(leg2);
        this.leg3 = Objects.requireNonNull(leg3);
    }

    @Override
    public LegFortescue getLeg1() {
        return leg1;
    }

    @Override
    public LegFortescue getLeg2() {
        return leg2;
    }

    @Override
    public LegFortescue getLeg3() {
        return leg3;
    }
}
