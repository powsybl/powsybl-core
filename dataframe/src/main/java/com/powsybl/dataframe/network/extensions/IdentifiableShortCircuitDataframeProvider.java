/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.dataframe.network.ExtensionInformation;
import com.powsybl.dataframe.network.NetworkDataframeMapper;
import com.powsybl.dataframe.network.NetworkDataframeMapperBuilder;
import com.powsybl.dataframe.network.adders.NetworkElementAdder;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
@AutoService(NetworkExtensionDataframeProvider.class)
public class IdentifiableShortCircuitDataframeProvider extends AbstractSingleDataframeNetworkExtension {
    @Override
    public String getExtensionName() {
        return IdentifiableShortCircuit.NAME;
    }

    @Override
    public ExtensionInformation getExtensionInformation() {
        return new ExtensionInformation(IdentifiableShortCircuit.NAME, "it contains max and min values of current " +
            "allowed during short circuit on a network element",
            "index : id (str), equipment_type (str), ip_min (float), ip_max (float)");
    }

    private Stream<IdentifiableShortCircuit> itemsStream(Network network) {
        return network.getIdentifiables().stream()
            .map(g -> (IdentifiableShortCircuit) g.getExtension(IdentifiableShortCircuit.class))
            .filter(Objects::nonNull);
    }

    private IdentifiableShortCircuit getOrThrow(Network network, String id) {
        Identifiable identifiable = network.getIdentifiable(id);
        if (identifiable == null) {
            throw new PowsyblException("Identifiable '" + id + "' not found");
        }
        IdentifiableShortCircuit isc = (IdentifiableShortCircuit) identifiable.getExtension(
            IdentifiableShortCircuit.class);
        if (isc == null) {
            throw new PowsyblException("Identifiable '" + id + "' has no IdentifiableShortCircuit extension");
        }
        return isc;
    }

    @Override
    public NetworkDataframeMapper createMapper() {
        return NetworkDataframeMapperBuilder.ofStream(this::itemsStream, this::getOrThrow)
            .stringsIndex("id", ext -> ((Identifiable) ext.getExtendable()).getId())
            .strings("equipment_type", ext -> ((Identifiable) ext.getExtendable()).getType().toString())
            .doubles("ip_min", IdentifiableShortCircuit::getIpMin, IdentifiableShortCircuit::setIpMin)
            .doubles("ip_max", IdentifiableShortCircuit::getIpMax, IdentifiableShortCircuit::setIpMax)
            .build();
    }

    @Override
    public void removeExtensions(Network network, List<String> ids) {
        ids.stream().filter(Objects::nonNull)
            .map(network::getIdentifiable)
            .filter(Objects::nonNull)
            .forEach(id -> id.removeExtension(IdentifiableShortCircuit.class));
    }

    @Override
    public NetworkElementAdder createAdder() {
        return new IdentifiableShortCircuitDataframeAdder();
    }
}
