/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list;

import com.google.common.collect.ImmutableList;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyElement;
import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class IdentifierContingencyList implements ContingencyList {

    private static final String VERSION = "1.1";
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
        return "identifier";
    }

    public List<NetworkElementIdentifier> getIdentifiants() {
        return ImmutableList.copyOf(networkElementIdentifiers);
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        return networkElementIdentifiers.stream()
                .map(identifiant -> identifiant.filterIdentifiable(network))
                .filter(Optional::isPresent)
                .map(identifiable -> new Contingency(identifiable.get().getId(),
                        ContingencyElement.of(identifiable.get())))
                .filter(contingency -> contingency.isValid(network))
                .collect(Collectors.toList());
    }
}
