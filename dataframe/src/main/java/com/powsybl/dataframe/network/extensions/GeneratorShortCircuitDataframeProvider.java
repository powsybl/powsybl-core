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
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuit;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
@AutoService(NetworkExtensionDataframeProvider.class)
public class GeneratorShortCircuitDataframeProvider extends AbstractSingleDataframeNetworkExtension {
    @Override
    public String getExtensionName() {
        return GeneratorShortCircuit.NAME;
    }

    @Override
    public ExtensionInformation getExtensionInformation() {
        return new ExtensionInformation(GeneratorShortCircuit.NAME,
            "it contains the transitory reactance of a generator needed to compute short circuit. " +
                "A subtransitory reactance can also be contained",
            "index : id (str), direct_sub_trans_x (float), direct_trans_x (float), step_up_transformer_x (float)");
    }

    private Stream<GeneratorShortCircuit> itemsStream(Network network) {
        return network.getGeneratorStream()
            .map(g -> (GeneratorShortCircuit) g.getExtension(GeneratorShortCircuit.class))
            .filter(Objects::nonNull);
    }

    private GeneratorShortCircuit getOrThrow(Network network, String id) {
        Generator gen = network.getGenerator(id);
        if (gen == null) {
            throw new PowsyblException("Generator '" + id + "' not found");
        }
        GeneratorShortCircuit gsc = gen.getExtension(GeneratorShortCircuit.class);
        if (gsc == null) {
            throw new PowsyblException("Generator '" + id + "' has no GeneratorShortCircuit extension");
        }
        return gsc;
    }

    @Override
    public NetworkDataframeMapper createMapper() {
        return NetworkDataframeMapperBuilder.ofStream(this::itemsStream, this::getOrThrow)
            .stringsIndex("id", ext -> ((Identifiable) ext.getExtendable()).getId())
            .doubles("direct_sub_trans_x", GeneratorShortCircuit::getDirectSubtransX,
                GeneratorShortCircuit::setDirectSubtransX)
            .doubles("direct_trans_x", GeneratorShortCircuit::getDirectTransX, GeneratorShortCircuit::setDirectTransX)
            .doubles("step_up_transformer_x", GeneratorShortCircuit::getStepUpTransformerX,
                GeneratorShortCircuit::setStepUpTransformerX)
            .build();
    }

    @Override
    public void removeExtensions(Network network, List<String> ids) {
        ids.stream().filter(Objects::nonNull)
            .map(network::getGenerator)
            .filter(Objects::nonNull)
            .forEach(g -> g.removeExtension(GeneratorShortCircuit.class));
    }

    @Override
    public NetworkElementAdder createAdder() {
        return new GeneratorShortCircuitDataframeAdder();
    }
}
