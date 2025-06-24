/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.contingency.list;

import com.google.common.collect.ImmutableList;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class ListOfContingencyLists implements ContingencyList {
    private static final String VERSION = "1.1";
    public static final String TYPE = "list";
    private final String name;
    private final List<ContingencyList> contingencyLists;

    public ListOfContingencyLists(String name, List<ContingencyList> contingencyLists) {
        this.name = Objects.requireNonNull(name);
        this.contingencyLists = ImmutableList.copyOf(contingencyLists);
    }

    public static String getVersion() {
        return VERSION;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        return contingencyLists.stream()
                .flatMap(contingencyList -> contingencyList.getContingencies(network).stream())
                .collect(Collectors.toList());
    }

    public List<ContingencyList> getContingencyLists() {
        return ImmutableList.copyOf(contingencyLists);
    }
}
