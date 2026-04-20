/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.LineCouplings;
import com.powsybl.iidm.network.extensions.MutualCoupling;
import com.powsybl.iidm.network.extensions.MutualCouplingAdder;

import java.util.*;

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
        Objects.requireNonNull(mutualCoupling);

        // Prevent duplicates and symmetrical mutual couplings
        boolean alreadyExists = mutualCouplings.stream()
            .anyMatch(mc -> matches(mc, mutualCoupling.getLine1(), mutualCoupling.getLine2()));

        if (alreadyExists) {
            throw new PowsyblException("Mutual coupling already exists between lines "
                + mutualCoupling.getLine1().getId() + " and "
                + mutualCoupling.getLine2().getId());
        }

        mutualCouplings.add(mutualCoupling);
        return this;
    }

    @Override
    public MutualCouplingAdder newMutualCoupling() {
        return new MutualCouplingAdderImpl(this);
    }

    @Override
    public Optional<MutualCoupling> findMutualCoupling(Line line1, Line line2) {
        return mutualCouplings.stream()
            .filter(mc -> matches(mc, line1, line2))
            .findFirst();
    }


    @Override
    public boolean removeMutualCoupling(MutualCoupling mutualCoupling) {
        return mutualCouplings.remove(mutualCoupling);
    }

    @Override
    public boolean removeMutualCoupling(Line line1, Line line2) {
        return mutualCouplings.removeIf(mc -> matches(mc, line1, line2));
    }

    private boolean matches(MutualCoupling mutualCoupling, Line line1, Line line2) {
        return mutualCoupling.getLine1() == line1 && mutualCoupling.getLine2() == line2
            || mutualCoupling.getLine1() == line2 && mutualCoupling.getLine2() == line1;
    }
}
