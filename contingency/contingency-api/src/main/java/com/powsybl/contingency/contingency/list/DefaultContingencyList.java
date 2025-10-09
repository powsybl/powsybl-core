/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.contingency.contingency.list;

import com.google.common.collect.ImmutableList;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public class DefaultContingencyList implements ContingencyList {

    public static final String TYPE = "default";
    private final String name;
    private final List<Contingency> contingencies;

    public DefaultContingencyList(Contingency... contingencies) {
        this("", contingencies);
    }

    public DefaultContingencyList(String name, Contingency... contingencies) {
        this(name, ImmutableList.copyOf(contingencies));
    }

    public DefaultContingencyList(String name, List<Contingency> contingencies) {
        this.name = Objects.requireNonNull(name);
        this.contingencies = ImmutableList.copyOf(contingencies);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public List<Contingency> getContingencies() {
        return contingencies;
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        return contingencies.stream()
                .filter(ctg -> ctg.isValid(network))
                .collect(Collectors.toList());
    }
}
