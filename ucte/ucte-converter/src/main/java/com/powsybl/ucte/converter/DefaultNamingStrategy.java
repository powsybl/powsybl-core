/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.ucte.converter;

import com.google.auto.service.AutoService;
import com.powsybl.iidm.network.*;
import com.powsybl.ucte.network.UcteElementId;
import com.powsybl.ucte.network.UcteNodeCode;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link NamingStrategy} implementation that ensures the conformity of IDs with the UCTE-DEF format
 *
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
@AutoService(NamingStrategy.class)
public class DefaultNamingStrategy implements NamingStrategy {

    private final Map<String, UcteNodeCode> ucteNodeIds = new HashMap<>();

    private final Map<String, UcteElementId> ucteElementIds = new HashMap<>();

    @Override
    public String getName() {
        return "Default";
    }

    @Override
    public void initialiseNetwork(Network network) {

    }

    @Override
    public UcteNodeCode getUcteNodeCode(String id) {
        return ucteNodeIds.computeIfAbsent(id, k -> UcteNodeCode.parseUcteNodeCode(k).orElseThrow(() -> new UcteException("Invalid UCTE node identifier: " + k)));
    }

    @Override
    public UcteNodeCode getUcteNodeCode(Bus bus) {
        return getUcteNodeCode(bus.getId());
    }

    @Override
    public UcteNodeCode getUcteNodeCode(DanglingLine danglingLine) {
        return getUcteNodeCode(danglingLine.getPairingKey());
    }

    @Override
    public UcteElementId getUcteElementId(String id) {
        return ucteElementIds.computeIfAbsent(id, k -> UcteElementId.parseUcteElementId(k).orElseThrow(() -> new UcteException("Invalid UCTE node identifier: " + k)));
    }

    @Override
    public UcteElementId getUcteElementId(Switch sw) {
        return getUcteElementId(sw.getId());
    }

    @Override
    public UcteElementId getUcteElementId(Branch branch) {
        return getUcteElementId(branch.getId());
    }

    @Override
    public UcteElementId getUcteElementId(DanglingLine danglingLine) {
        return getUcteElementId(danglingLine.getId());
    }

}
