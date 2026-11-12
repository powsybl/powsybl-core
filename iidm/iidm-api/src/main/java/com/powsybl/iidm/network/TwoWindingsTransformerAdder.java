/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface TwoWindingsTransformerAdder extends BranchAdder<TwoWindingsTransformer, TwoWindingsTransformerAdder> {

    static TwoWindingsTransformerAdder fillTwoWindingsTransformerAdder(TwoWindingsTransformerAdder adder, TwoWindingsTransformer twoWindingsTransformer) {
        return adder.setR(twoWindingsTransformer.getR())
                .setX(twoWindingsTransformer.getX())
                .setB(twoWindingsTransformer.getB())
                .setG(twoWindingsTransformer.getG())
                .setRatedU1(twoWindingsTransformer.getRatedU1())
                .setRatedU2(twoWindingsTransformer.getRatedU2());
    }

    /**
     * Set the resistance of the transformer in &#937. Since this can be an equivalent model, calculations can lead to the value being negative.
     * Therefore, a negative value is allowed.
     * @param r the value of the resistance of the transformer in &#937
     * @return this adder, for chaining
     */
    TwoWindingsTransformerAdder setR(double r);

    TwoWindingsTransformerAdder setX(double x);

    TwoWindingsTransformerAdder setB(double b);

    TwoWindingsTransformerAdder setG(double g);

    TwoWindingsTransformerAdder setRatedU1(double ratedU1);

    TwoWindingsTransformerAdder setRatedU2(double ratedU2);

    default TwoWindingsTransformerAdder setRatedS(double ratedS) {
        throw new UnsupportedOperationException();
    }

    @Override
    TwoWindingsTransformer add();

}
