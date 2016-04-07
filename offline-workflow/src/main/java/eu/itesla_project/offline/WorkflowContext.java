/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline;

import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.loadflow.api.LoadFlow;
import eu.itesla_project.loadflow.api.LoadFlowParameters;
import eu.itesla_project.modules.Optimizer;
import eu.itesla_project.modules.sampling.Sampler;
import eu.itesla_project.modules.simulation.ImpactAnalysis;
import eu.itesla_project.modules.simulation.Stabilization;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class WorkflowContext {

    private final Network network;

    private final Sampler sampler;

    private final Optimizer optimizer;

    private final LoadFlow loadflow;

    private final Stabilization stabilization;

    private final ImpactAnalysis impactAnalysis;

    private final LoadFlowParameters loadFlowParameters;

    public WorkflowContext(Network network, Sampler sampler, Optimizer optimizer, LoadFlow loadflow, 
                           Stabilization stabilization, ImpactAnalysis impactAnalysis, 
                           LoadFlowParameters loadFlowParameters) {
        this.network = network;
        this.sampler = sampler;
        this.optimizer = optimizer;
        this.loadflow = loadflow;
        this.stabilization = stabilization;
        this.impactAnalysis = impactAnalysis;
        this.loadFlowParameters = loadFlowParameters;
    }

    public Network getNetwork() {
        return network;
    }

    public Sampler getSampler() {
        return sampler;
    }

    public Optimizer getOptimizer() {
        return optimizer;
    }

    public LoadFlow getLoadflow() {
        return loadflow;
    }

    public Stabilization getStabilization() {
        return stabilization;
    }

    public ImpactAnalysis getImpactAnalysis() {
        return impactAnalysis;
    }

    public LoadFlowParameters getLoadFlowParameters() {
        return loadFlowParameters;
    }

}
