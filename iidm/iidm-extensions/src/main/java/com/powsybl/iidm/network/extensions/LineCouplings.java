/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Optional;

/**
 * This extension stores the mutual couplings on the network.
 *
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public interface LineCouplings extends Extension<Network> {

    String NAME = "lineCouplings";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * Gets all mutual couplings in the network.
     * @return a list of mutual couplings
     */
    List<MutualCoupling> getMutualCouplings();

    /**
     * Adds a mutual coupling.
     * @param mutualCoupling the mutual coupling to add
     * @return the current line couplings extension
     * @throws PowsyblException if the coupling is invalid or already exists
     */
    LineCouplings add(MutualCoupling mutualCoupling);

    /**
     * Creates a new mutual coupling adder.
     * @return a mutual coupling adder
     */
    MutualCouplingAdder newMutualCoupling();

    /**
     * Finds a mutual coupling between two lines.
     * The order of the lines does not matter.
     * @param line1 the first line
     * @param line2 the second line
     * @return the mutual coupling if found, or empty if not
     */
    Optional<MutualCoupling> findMutualCoupling(Line line1, Line line2);

    /**
     * Removes a mutual coupling.
     * @param mutualCoupling the mutual coupling to remove
     * @return true if the mutual coupling was removed, false otherwise
     */
    boolean removeMutualCoupling(MutualCoupling mutualCoupling);

    /**
     * Removes a mutual coupling between two lines.
     * @param line1 the first line
     * @param line2 the second line
     * @return true if the mutual coupling was removed, false otherwise
     */
    boolean removeMutualCoupling(Line line1, Line line2);
}
