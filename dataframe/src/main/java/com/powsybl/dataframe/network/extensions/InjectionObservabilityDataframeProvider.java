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
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.InjectionObservability;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
@AutoService(NetworkExtensionDataframeProvider.class)
public class InjectionObservabilityDataframeProvider extends AbstractSingleDataframeNetworkExtension {

    private Stream<InjectionObservability> itemsStream(Network network) {
        return network.getIdentifiables().stream().filter(Objects::nonNull)
            .filter(identifiable -> identifiable instanceof Injection)
            .map(inj -> (InjectionObservability) inj.getExtension(InjectionObservability.class))
            .filter(Objects::nonNull);
    }

    private InjectionObservability getOrThrow(Network network, String id) {
        Identifiable identifiable = network.getIdentifiable(id);
        if (identifiable == null) {
            throw new PowsyblException("Invalid injection id : could not find " + id);
        }
        if (!(identifiable instanceof Injection)) {
            throw new PowsyblException(id + " is not an injection");
        }
        Injection injection = (Injection) identifiable;
        return (InjectionObservability) injection.getExtension(InjectionObservability.class);
    }

    @Override
    public String getExtensionName() {
        return InjectionObservability.NAME;
    }

    @Override
    public ExtensionInformation getExtensionInformation() {
        return new ExtensionInformation(InjectionObservability.NAME,
            "Provides information about the observability of a injection",
            "index : id (str), observable (bool), p_standard_deviation (float), p_redundant (bool), " +
                "q_standard_deviation (float), q_redundant (bool), v_standard_deviation (float), v_redundant (bool)");
    }

    @Override
    public NetworkDataframeMapper createMapper() {
        return NetworkDataframeMapperBuilder.ofStream(this::itemsStream, this::getOrThrow)
            .stringsIndex("id", injectionObservability -> ((Injection) injectionObservability.getExtendable()).getId())
            .booleans("observable", InjectionObservability::isObservable)
            .doubles("p_standard_deviation", injectionObservability -> injectionObservability.getQualityP() != null ?
                    injectionObservability.getQualityP().getStandardDeviation() : Double.NaN,
                (injectionObservability, standardDeviation) -> {
                    if (injectionObservability.getQualityP() != null) {
                        injectionObservability.getQualityP().setStandardDeviation(standardDeviation);
                    } else {
                        injectionObservability.setQualityP(standardDeviation);
                    }
                })
            .booleans("p_redundant", injectionObservability -> injectionObservability.getQualityP() != null &&
                    (boolean) injectionObservability.getQualityP().isRedundant().orElse(false),
                (injectionObservability, redundant) -> injectionObservability.getQualityP().setRedundant(redundant))
            .booleans("p_redundant_null", injectionObservability -> injectionObservability.getQualityP() == null ||
                injectionObservability.getQualityP().isRedundant().isEmpty())
            .doubles("q_standard_deviation", injectionObservability -> injectionObservability.getQualityQ() != null ?
                    injectionObservability.getQualityQ().getStandardDeviation() : Double.NaN,
                (injectionObservability, standardDeviation) -> {
                    if (injectionObservability.getQualityQ() != null) {
                        injectionObservability.getQualityQ().setStandardDeviation(standardDeviation);
                    } else {
                        injectionObservability.setQualityQ(standardDeviation);
                    }
                })
            .booleans("q_redundant", injectionObservability -> injectionObservability.getQualityQ() != null &&
                    (boolean) injectionObservability.getQualityQ().isRedundant().orElse(false),
                (injectionObservability, redundant) -> injectionObservability.getQualityQ().setRedundant(redundant))
            .booleans("q_redundant_null", injectionObservability -> injectionObservability.getQualityQ() == null ||
                injectionObservability.getQualityQ().isRedundant().isEmpty())
            .doubles("v_standard_deviation", injectionObservability -> injectionObservability.getQualityV() != null ?
                    injectionObservability.getQualityV().getStandardDeviation() : Double.NaN,
                (injectionObservability, standardDeviation) -> {
                    if (injectionObservability.getQualityV() != null) {
                        injectionObservability.getQualityV().setStandardDeviation(standardDeviation);
                    } else {
                        injectionObservability.setQualityV(standardDeviation);
                    }
                })
            .booleans("v_redundant", injectionObservability -> injectionObservability.getQualityV() != null &&
                    (boolean) injectionObservability.getQualityV().isRedundant().orElse(false),
                (injectionObservability, redundant) -> injectionObservability.getQualityV().setRedundant(redundant))
            .booleans("v_redundant_null", injectionObservability -> injectionObservability.getQualityV() == null ||
                injectionObservability.getQualityV().isRedundant().isEmpty())
            .build();
    }

    @Override
    public void removeExtensions(Network network, List<String> ids) {
        ids.stream().filter(Objects::nonNull)
            .map(network::getIdentifiable)
            .filter(Objects::nonNull)
            .filter(identifiable -> identifiable instanceof Injection)
            .forEach(inj -> inj.removeExtension(InjectionObservability.class));
    }

    @Override
    public NetworkElementAdder createAdder() {
        return new InjectionObservabilityDataframeAdder();
    }
}
