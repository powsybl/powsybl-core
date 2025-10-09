/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.execution;

import com.powsybl.iidm.network.Network;

import static java.util.Objects.requireNonNull;

/**
 * A network variant, simply embeds a {@link Network} and the ID of one of its variants.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public class NetworkVariant {

    private final Network network;
    private final String variantId;

    public NetworkVariant(Network network, String variantId) {
        this.network = requireNonNull(network);
        this.variantId = requireNonNull(variantId);
    }

    /**
     * The underlying {@link Network}
     * @return the underlying {@link Network}
     */
    public Network getNetwork() {
        return network;
    }

    /**
     * The underlying {@link Network} with variant {@link #variantId} properly set.
     * @return the underlying {@link Network} with variant {@link #variantId} properly set.
     */
    public Network getVariant() {
        network.getVariantManager().setWorkingVariant(variantId);
        return network;
    }

    /**
     * The variant identifier.
     * @return the variant identifier.
     */
    public String getVariantId() {
        return variantId;
    }
}
