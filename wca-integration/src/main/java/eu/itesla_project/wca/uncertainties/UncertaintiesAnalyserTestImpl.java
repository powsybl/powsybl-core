/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.wca.uncertainties;

import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.wca.StochasticInjection;
import eu.itesla_project.modules.wca.Uncertainties;
import eu.itesla_project.modules.wca.UncertaintiesAnalyser;
import org.joda.time.Interval;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UncertaintiesAnalyserTestImpl implements UncertaintiesAnalyser {

    private static final float DEFAULT_INTERVAL_PERCENT = 0.1f; // 10%

    private final Network network;

    public UncertaintiesAnalyserTestImpl(Network network) {
        this.network = network;
    }

    @Override
    public CompletableFuture<Uncertainties> analyse(Interval interval) throws Exception {
        List<StochasticInjection> injections = StochasticInjection.create(network, true, false, false, null);
        Uncertainties uncertainties = new Uncertainties(injections, injections.size());

        // as many reduced variable as injections
        for (int i = 0; i < injections.size(); i++) {
            for (int varNum = 0; varNum < injections.size(); varNum++) {
                uncertainties.reductionMatrix[i][varNum] = (i == varNum ? 1d : 0d);
            }
        }

        for (int i = 0; i < injections.size(); i++) {
            StochasticInjection inj = injections.get(i);
            uncertainties.means[i] = inj.getP();
            uncertainties.min[i] = -Math.abs(inj.getP()) * DEFAULT_INTERVAL_PERCENT;
            uncertainties.max[i] = Math.abs(inj.getP()) * DEFAULT_INTERVAL_PERCENT;
        }

        return CompletableFuture.completedFuture(uncertainties);
    }

}
