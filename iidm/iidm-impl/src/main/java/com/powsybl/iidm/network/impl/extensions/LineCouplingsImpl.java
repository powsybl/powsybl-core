/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.LineCouplings;
import com.powsybl.iidm.network.extensions.MutualCoupling;
import com.powsybl.iidm.network.extensions.MutualCouplingAdder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class LineCouplingsImpl extends AbstractExtension<Network> implements LineCouplings {

    private final List<MutualCoupling> mutualCouplings = new ArrayList<>();

    @Override
    public List<MutualCoupling> getMutualCouplings() {
        return Collections.unmodifiableList(mutualCouplings);
    }

    @Override
    public LineCouplings add(MutualCoupling mutualCoupling) {
        mutualCouplings.add(mutualCoupling);
        return this;
    }

    @Override
    public MutualCouplingAdder newMutualCoupling() {
        return new MutualCouplingAdderImpl(this);
    }
}
