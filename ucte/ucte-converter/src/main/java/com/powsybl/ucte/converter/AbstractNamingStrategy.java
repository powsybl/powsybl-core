/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.ucte.converter.util.UcteConverterConstants;
import com.powsybl.ucte.network.UcteElementId;
import com.powsybl.ucte.network.UcteNodeCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Clément LECLERC {@literal <clement.leclerc@rte-france.com>}
 */
public abstract class AbstractNamingStrategy implements NamingStrategy {

    @Override
    public Context initialize(Network network) {
        return new Context(network);
    }

    @Override
    public UcteNodeCode getUcteNodeCode(NamingStrategy.Context context, String id) {
        return ((Context) context).getUcteNodeIds().computeIfAbsent(id, k -> UcteNodeCode.parseUcteNodeCode(k)
                .orElseThrow(() -> new UcteException(UcteConverterConstants.NO_UCTE_CODE_ERROR + k)));
    }

    @Override
    public UcteNodeCode getUcteNodeCode(NamingStrategy.Context context, Bus bus) {
        if (bus == null) {
            throw new PowsyblException("the bus is null");
        }
        return getUcteNodeCode(context, bus.getId());
    }

    @Override
    public UcteNodeCode getUcteNodeCode(NamingStrategy.Context context, BoundaryLine boundaryLine) {
        if (boundaryLine.getPairingKey() == null) {
            return getUcteNodeCode(context, boundaryLine.getId());
        }
        return getUcteNodeCode(context, boundaryLine.getPairingKey());
    }

    @Override
    public UcteElementId getUcteElementId(NamingStrategy.Context context, String id) {
        return ((Context) context).getUcteElementIds().computeIfAbsent(id, k -> UcteElementId.parseUcteElementId(k)
                .orElseThrow(() -> new UcteException(UcteConverterConstants.NO_UCTE_CODE_ERROR + k)));
    }

    @Override
    public UcteElementId getUcteElementId(NamingStrategy.Context context, Switch sw) {
        if (sw == null) {
            throw new PowsyblException("the switch is null");
        }
        return getUcteElementId(context, sw.getId());
    }

    @Override
    public UcteElementId getUcteElementId(NamingStrategy.Context context, Branch branch) {
        if (branch == null) {
            throw new PowsyblException("the branch is null");
        }
        return getUcteElementId(context, branch.getId());
    }

    @Override
    public UcteElementId getUcteElementId(NamingStrategy.Context context, BoundaryLine boundaryLine) {
        if (boundaryLine == null) {
            throw new PowsyblException("the boundaryLine is null");
        }
        return getUcteElementId(context, boundaryLine.getId());
    }

    /**
     * Base {@link NamingStrategy.Context} implementation, holding the UCTE code assigned to each bus and
     * element id encountered so far.
     */
    public static class Context implements NamingStrategy.Context {

        private final Network network;

        private final Map<String, UcteNodeCode> ucteNodeIds = new HashMap<>();

        private final Map<String, UcteElementId> ucteElementIds = new HashMap<>();

        public Context(Network network) {
            this.network = Objects.requireNonNull(network);
        }

        @Override
        public Network getNetwork() {
            return network;
        }

        public Map<String, UcteNodeCode> getUcteNodeIds() {
            return ucteNodeIds;
        }

        public Map<String, UcteElementId> getUcteElementIds() {
            return ucteElementIds;
        }
    }
}
