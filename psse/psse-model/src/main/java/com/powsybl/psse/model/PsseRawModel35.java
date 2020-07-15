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
        // loads.forEach(load -> assertTrue(load instanceof PsseLoad35));
        loads.forEach(load -> {
            if (load instanceof PsseLoad35) {
                System.err.printf("es PsseLoad35 %n");
            } else {
                System.err.printf("es PsseLoad %n");
            }
        });
        getLoads().addAll(loads);
    }

    @Override
    public void addGenerators(List<PsseGenerator> generators) {
        generators.forEach(generator -> {
            if (generator instanceof PsseGenerator35) {
                System.err.printf("es PsseGenerator35 %n");
            } else {
                System.err.printf("es PsseGenerator %n");
            }
        });
        getGenerators().addAll(generators);
    }

    @Override
    public void addNonTransformerBranches(List<PsseNonTransformerBranch> nonTransformerBranches) {
        nonTransformerBranches.forEach(nonTransformerBranch -> {
            if (nonTransformerBranch instanceof PsseNonTransformerBranch35) {
                System.err.printf("es PsseGenerator35 %n");
            } else {
                System.err.printf("es PsseGenerator %n");
            }
        });
        getNonTransformerBranches().addAll(nonTransformerBranches);
    }

    @Override
    public void addTransformers(List<PsseTransformer> transformers) {
        transformers.forEach(transformer -> {
            if (transformer instanceof PsseTransformer35) {
                System.err.printf("es PsseTransformer35 %n");
            } else {
                System.err.printf("es PsseTransformer %n");
            }
        });
        getTransformers().addAll(transformers);
    }

    @Override
    public void addSwitchedShunts(List<PsseSwitchedShunt> switchedShunts) {
        switchedShunts.forEach(switchedShunt -> {
            if (switchedShunt instanceof PsseSwitchedShunt35) {
                System.err.printf("es PsseSwitchedShunt35 %n");
            } else {
                System.err.printf("es PsseSwitchedShunt %n");
            }
        });
        getSwitchedShunts().addAll(switchedShunts);
    }
}
