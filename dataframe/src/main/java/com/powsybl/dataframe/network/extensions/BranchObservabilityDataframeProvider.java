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
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.BranchObservability;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
@AutoService(NetworkExtensionDataframeProvider.class)
public class BranchObservabilityDataframeProvider extends AbstractSingleDataframeNetworkExtension {

    private Stream<BranchObservability> itemsStream(Network network) {
        return network.getBranchStream().filter(Objects::nonNull)
            .map(branch -> (BranchObservability) branch.getExtension(BranchObservability.class))
            .filter(Objects::nonNull);
    }

    private BranchObservability getOrThrow(Network network, String id) {
        Branch branch = network.getBranch(id);
        if (branch == null) {
            throw new PowsyblException("Invalid branch id : could not find " + id);
        }
        return (BranchObservability) branch.getExtension(BranchObservability.class);
    }

    @Override
    public String getExtensionName() {
        return BranchObservability.NAME;
    }

    @Override
    public ExtensionInformation getExtensionInformation() {
        return new ExtensionInformation(BranchObservability.NAME,
            "Provides information about the observability of a branch",
            "index : id (str), observable (bool), p1_standard_deviation (float), p1_redundant (bool), p2_standard_deviation (float), p2_redundant (bool), "
                + "q1_standard_deviation (float), q1_redundant (bool), q2_standard_deviation (float), q2_redundant (bool)");
    }

    @Override
    public NetworkDataframeMapper createMapper() {
        return NetworkDataframeMapperBuilder.ofStream(this::itemsStream, this::getOrThrow)
            .stringsIndex("id", branchObservability -> ((Branch) branchObservability.getExtendable()).getId())
            .booleans("observable", BranchObservability::isObservable)
            .doubles("p1_standard_deviation", branchObservability -> branchObservability.getQualityP1() != null ?
                    branchObservability.getQualityP1().getStandardDeviation() : Double.NaN,
                (branchObservability, standardDeviation) -> branchObservability.getQualityP1()
                    .setStandardDeviation(standardDeviation))
            .booleans("p1_redundant", branchObservability -> branchObservability.getQualityP1() != null &&
                    (boolean) branchObservability.getQualityP1().isRedundant().orElse(false),
                (branchObservability, redundant) -> branchObservability.getQualityP1().setRedundant(redundant))
            .booleans("p1_redundant_null",
                branchObservability -> branchObservability.getQualityP1() == null || branchObservability.getQualityP1()
                    .isRedundant()
                    .isEmpty())
            .doubles("p2_standard_deviation",
                branchObservability -> branchObservability.getQualityP2() != null ? branchObservability.getQualityP2()
                    .getStandardDeviation() : Double.NaN,
                (branchObservability, standardDeviation) -> branchObservability.getQualityP2()
                    .setStandardDeviation(standardDeviation))
            .booleans("p2_redundant", branchObservability -> branchObservability.getQualityP2() != null &&
                    (boolean) branchObservability.getQualityP2().isRedundant().orElse(false),
                (branchObservability, redundant) -> branchObservability.getQualityP2().setRedundant(redundant))
            .booleans("p2_redundant_null", branchObservability -> branchObservability.getQualityP2() == null ||
                branchObservability.getQualityP2().isRedundant().isEmpty())
            .doubles("q1_standard_deviation", branchObservability -> branchObservability.getQualityQ1() != null ?
                    branchObservability.getQualityQ1().getStandardDeviation() : Double.NaN,
                (branchObservability, standardDeviation) -> branchObservability.getQualityQ1()
                    .setStandardDeviation(standardDeviation))
            .booleans("q1_redundant", branchObservability -> branchObservability.getQualityQ1() != null &&
                    (boolean) branchObservability.getQualityQ1().isRedundant().orElse(false),
                (branchObservability, redundant) -> branchObservability.getQualityQ1().setRedundant(redundant))
            .booleans("q1_redundant_null", branchObservability -> branchObservability.getQualityQ1() == null ||
                branchObservability.getQualityQ1().isRedundant().isEmpty())
            .doubles("q2_standard_deviation", branchObservability -> branchObservability.getQualityQ2() != null ?
                    branchObservability.getQualityQ2().getStandardDeviation() : Double.NaN,
                (branchObservability, standardDeviation) -> branchObservability.getQualityQ2()
                    .setStandardDeviation(standardDeviation))
            .booleans("q2_redundant", branchObservability -> branchObservability.getQualityQ2() != null &&
                    (boolean) branchObservability.getQualityQ2().isRedundant().orElse(false),
                (branchObservability, redundant) -> branchObservability.getQualityQ2().setRedundant(redundant))
            .booleans("q2_redundant_null", branchObservability -> branchObservability.getQualityQ2() == null ||
                branchObservability.getQualityQ2().isRedundant().isEmpty())
            .build();
    }

    @Override
    public void removeExtensions(Network network, List<String> ids) {
        ids.stream().filter(Objects::nonNull)
            .map(network::getBranch)
            .filter(Objects::nonNull)
            .forEach(branch -> branch.removeExtension(BranchObservability.class));
    }

    @Override
    public NetworkElementAdder createAdder() {
        return new BranchObservabilityDataframeAdder();
    }
}
