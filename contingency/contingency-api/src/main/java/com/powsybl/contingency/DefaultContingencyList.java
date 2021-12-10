/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.contingency;

import com.google.common.collect.ImmutableList;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class DefaultContingencyList implements ContingencyList {

    // VERSION = 1.0 : first version
    public static final String VERSION = "1.0";

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
