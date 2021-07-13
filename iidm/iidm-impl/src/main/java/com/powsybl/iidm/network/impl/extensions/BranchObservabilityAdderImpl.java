/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.extensions.BranchObservability;
import com.powsybl.iidm.network.extensions.BranchObservabilityAdder;
import org.jgrapht.alg.util.Pair;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BranchObservabilityAdderImpl<B extends Branch<B>>
        extends AbstractExtensionAdder<B, BranchObservability<B>>
        implements BranchObservabilityAdder<B> {

    private boolean observable;

    private final Map<Branch.Side, Pair<Float, Boolean>> standardDeviationP = new EnumMap<>(Branch.Side.class);

    private final Map<Branch.Side, Pair<Float, Boolean>> standardDeviationQ = new EnumMap<>(Branch.Side.class);

    private final Map<Branch.Side, Pair<Float, Boolean>> standardDeviationV = new EnumMap<>(Branch.Side.class);

    public BranchObservabilityAdderImpl(B extendable) {
        super(extendable);
        standardDeviationP.put(Branch.Side.ONE, new Pair<>(Float.NaN, false));
        standardDeviationP.put(Branch.Side.TWO, new Pair<>(Float.NaN, false));
        standardDeviationQ.put(Branch.Side.ONE, new Pair<>(Float.NaN, false));
        standardDeviationQ.put(Branch.Side.TWO, new Pair<>(Float.NaN, false));
        standardDeviationV.put(Branch.Side.ONE, new Pair<>(Float.NaN, false));
        standardDeviationV.put(Branch.Side.TWO, new Pair<>(Float.NaN, false));
    }

    @Override
    protected BranchObservability<B> createExtension(B extendable) {
        return new BranchObservabilityImpl<>(extendable, observable,
                standardDeviationP.get(Branch.Side.ONE).getFirst(), standardDeviationP.get(Branch.Side.ONE).getSecond(),
                standardDeviationP.get(Branch.Side.TWO).getFirst(), standardDeviationP.get(Branch.Side.TWO).getSecond(),
                standardDeviationQ.get(Branch.Side.ONE).getFirst(), standardDeviationQ.get(Branch.Side.ONE).getSecond(),
                standardDeviationQ.get(Branch.Side.TWO).getFirst(), standardDeviationQ.get(Branch.Side.TWO).getSecond(),
                standardDeviationV.get(Branch.Side.ONE).getFirst(), standardDeviationV.get(Branch.Side.ONE).getSecond(),
                standardDeviationV.get(Branch.Side.TWO).getFirst(), standardDeviationV.get(Branch.Side.TWO).getSecond());
    }

    @Override
    public BranchObservabilityAdder<B> withObservable(boolean observable) {
        this.observable = observable;
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withStandardDeviationP(float standardDeviationP, Branch.Side side) {
        this.standardDeviationP.get(side).setFirst(standardDeviationP);
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withRedundantP(boolean redundant, Branch.Side side) {
        this.standardDeviationP.get(side).setSecond(redundant);
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withStandardDeviationQ(float standardDeviationQ, Branch.Side side) {
        this.standardDeviationQ.get(side).setFirst(standardDeviationQ);
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withRedundantQ(boolean redundant, Branch.Side side) {
        this.standardDeviationQ.get(side).setSecond(redundant);
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withStandardDeviationV(float standardDeviationV, Branch.Side side) {
        this.standardDeviationV.get(side).setFirst(standardDeviationV);
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withRedundantV(boolean redundant, Branch.Side side) {
        this.standardDeviationV.get(side).setSecond(redundant);
        return this;
    }
}
