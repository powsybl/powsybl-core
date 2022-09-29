/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.contingency.contingency.list.identifiant.Identifier;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class IdentifierContingencyList implements ContingencyList {

    // VERSION = 1.0 : first version
    public static final String VERSION = "1.0";

    private final String name;
    private final IdentifiableType identifiableType;
    private final List<Identifier> identifiers;

    public IdentifierContingencyList(String name, String identifiableType, List<Identifier> identifiers) {
        this(name, IdentifiableType.valueOf(identifiableType), identifiers);
    }

    public IdentifierContingencyList(String name, IdentifiableType identifiableType, List<Identifier> identifiers) {
        this.name = name;
        this.identifiableType = identifiableType;
        this.identifiers = identifiers;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return "identifiant";
    }

    public IdentifiableType getIdentifiableType() {
        return identifiableType;
    }

    public List<Identifier> getIdentifiants() {
        return identifiers;
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        return identifiers.stream()
                .map(identifiant -> identifiant.filterIdentifiable(network))
                .filter(Objects::nonNull)
                .filter(contingency -> contingency.isValid(network))
                .collect(Collectors.toList());
    }

    public String getVersion() {
        return VERSION;
    }
}
