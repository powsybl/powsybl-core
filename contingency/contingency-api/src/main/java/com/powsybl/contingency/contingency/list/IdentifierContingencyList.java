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
import com.powsybl.contingency.ContingencyElement;
import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;
import com.powsybl.iidm.network.Network;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class IdentifierContingencyList implements ContingencyList {

    private static final String VERSION = "1.2";
    public static final String TYPE = "identifier";
    private final String name;
    private final List<NetworkElementIdentifier> networkElementIdentifiers;

    public IdentifierContingencyList(String name, List<NetworkElementIdentifier> networkElementIdentifiers) {
        this.name = Objects.requireNonNull(name);
        this.networkElementIdentifiers = ImmutableList.copyOf(networkElementIdentifiers);
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

    public List<NetworkElementIdentifier> getIdentifiants() {
        return ImmutableList.copyOf(networkElementIdentifiers);
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        return networkElementIdentifiers.stream()
                .filter(identifier -> !identifier.filterIdentifiable(network).isEmpty())
                .map(identifier -> {
                    List<ContingencyElement> contingencyElements = identifier.filterIdentifiable(network)
                            .stream()
                            .map(ContingencyElement::of)
                            .collect(Collectors.toList());
                    String contingencyId = identifier.getContingencyId().orElse(getGeneratedContingencyId(contingencyElements));
                    return new Contingency(contingencyId, contingencyElements);
                })
                .filter(contingency -> contingency.isValid(network))
                .collect(Collectors.toList());
    }

    public Map<String, Set<String>> getNotFoundElements(Network network) {
        Map<String, Set<String>> notFoundElementsMap = new HashMap<>();
        networkElementIdentifiers.forEach(identifier -> {
            Set<String> notFoundElements = identifier.getNotFoundElements(network);
            if (!notFoundElements.isEmpty()) {
                String contingencyId = identifier.getContingencyId().orElse(getGeneratedContingencyId(identifier.filterIdentifiable(network)
                        .stream()
                        .map(ContingencyElement::of).toList()));
                notFoundElementsMap.put(contingencyId, notFoundElements);
            }

        });
        return notFoundElementsMap;
    }

    private String getGeneratedContingencyId(List<ContingencyElement> contingencyElements) {
        return "Contingency : " + contingencyElements
                        .stream()
                        .map(ContingencyElement::getId)
                        .collect(Collectors.joining(" + "));
    }
}
