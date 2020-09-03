/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import java.util.List;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseRawModel35 extends PsseRawModel {

    public PsseRawModel35(PsseCaseIdentification caseIdentification) {
        super(caseIdentification);
    }

    @Override
    public void addLoads(List<PsseLoad> loads) {
        if (loads.stream().anyMatch(load -> !(load instanceof PsseLoad35))) {
            throw new PsseException("PsseRawModel35. Unexpected instanceof load");
        }
        getLoads().addAll(loads);
    }

    @Override
    public void addGenerators(List<PsseGenerator> generators) {
        if (generators.stream().anyMatch(generator -> !(generator instanceof PsseGenerator35))) {
            throw new PsseException("PsseRawModel35. Unexpected instanceof generator");
        }
        getGenerators().addAll(generators);
    }

    @Override
    public void addNonTransformerBranches(List<PsseNonTransformerBranch> nonTransformerBranches) {
        if (nonTransformerBranches.stream().anyMatch(nonTransformerBranch -> !(nonTransformerBranch instanceof PsseNonTransformerBranch35))) {
            throw new PsseException("PsseRawModel35. Unexpected instanceof nonTransformerBranch");
        }
        getNonTransformerBranches().addAll(nonTransformerBranches);
    }

    @Override
    public void addTransformers(List<PsseTransformer> transformers) {
        if (transformers.stream().anyMatch(transformer -> !(transformer instanceof PsseTransformer35))) {
            throw new PsseException("PsseRawModel35. Unexpected instanceof transformer");
        }
        getTransformers().addAll(transformers);
    }

    @Override
    public void addSwitchedShunts(List<PsseSwitchedShunt> switchedShunts) {
        if (switchedShunts.stream().anyMatch(switchedShunt -> !(switchedShunt instanceof PsseSwitchedShunt35))) {
            throw new PsseException("PsseRawModel35. Unexpected instanceof switchedShunt");
        }
        getSwitchedShunts().addAll(switchedShunts);
    }
}
