/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.simple;

import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.loadflow.LoadFlowResultImpl;
import com.powsybl.loadflow.simple.equations.LoadFlowMatrix;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.SparseStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 *
 * A dummy DC load flow implementation for test purposes.
 *
 * It only updates active power flow on branches, and only takes into account lines, two winding transformers,
 * generators, and loads.
 * Lines open on one side are considered open.
 * The slack bus is the first bus in the network bus ordering.
 *
 *
 * Does not use {@link com.powsybl.computation.ComputationManager}, so that it can be used with a {@code null} one.

 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class SimpleLoadFlow implements LoadFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleLoadFlow.class);

    private final Network network;

    public SimpleLoadFlow(Network network) {
        this.network = Objects.requireNonNull(network);
    }

    @Override
    public CompletableFuture<LoadFlowResult> run(String state, LoadFlowParameters loadFlowParameters) {

        network.getVariantManager().setWorkingVariant(state);

        //initialize phase and voltage
        switch (loadFlowParameters.getVoltageInitMode()) {
            case DC_VALUES:
                LOGGER.warn("Voltage initialization with DC values is not supported, falling back to uniform values");
            case UNIFORM_VALUES:
                network.getBusView().getBusStream()
                        .forEach(b -> {
                            b.setAngle(0);
                            b.setV(b.getVoltageLevel().getNominalV());
                        });
                break;
            case PREVIOUS_VALUES:
                break;
        }

        SparseStore<Double> lfMatrix = LoadFlowMatrix.buildDc(network);
        PrimitiveDenseStore rhs = LoadFlowMatrix.buildDcRhs(network);

        boolean status;
        try {
            MatrixStore<Double> lhs = LU.PRIMITIVE.make().solve(lfMatrix, rhs);

            LoadFlowMatrix.updateNetwork(network, lhs);

            status = true;
        } catch (RecoverableCondition recoverableCondition) {
            status = false;
            LOGGER.error("Failed to solve linear system for simple DC load flow.", recoverableCondition);
        }

        return CompletableFuture.completedFuture(new LoadFlowResultImpl(status, Collections.emptyMap(), null));
    }

    @Override
    public String getName() {
        return "simple-dc-loadflow";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }
}
