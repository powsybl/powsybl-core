/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.extensions.BranchObservability;
import org.jgrapht.alg.util.Pair;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BranchObservabilityImpl<T extends Branch<T>> extends AbstractExtension<T>
        implements BranchObservability<T> {

    private boolean observable;

    private final Map<Branch.Side, Pair<Float, Boolean>> standardDeviationP = new EnumMap<>(Branch.Side.class);

    private final Map<Branch.Side, Pair<Float, Boolean>> standardDeviationQ = new EnumMap<>(Branch.Side.class);

    private final Map<Branch.Side, Pair<Float, Boolean>> standardDeviationV = new EnumMap<>(Branch.Side.class);

    public BranchObservabilityImpl(T component, boolean observable,
                                   float oneStandardDeviationP, boolean oneRedundantP,
                                   float twoStandardDeviationP, boolean twoRedundantP,
                                   float oneStandardDeviationQ, boolean oneRedundantQ,
                                   float twoStandardDeviationQ, boolean twoRedundantQ,
                                   float oneStandardDeviationV, boolean oneRedundantV,
                                   float twoStandardDeviationV, boolean twoRedundantV) {
        super(component);
        this.observable = observable;
        this.standardDeviationP.put(Branch.Side.ONE, new Pair<>(oneStandardDeviationP, oneRedundantP));
        this.standardDeviationP.put(Branch.Side.TWO, new Pair<>(twoStandardDeviationP, twoRedundantP));

        this.standardDeviationQ.put(Branch.Side.ONE, new Pair<>(oneStandardDeviationQ, oneRedundantQ));
        this.standardDeviationQ.put(Branch.Side.TWO, new Pair<>(twoStandardDeviationQ, twoRedundantQ));

        this.standardDeviationV.put(Branch.Side.ONE, new Pair<>(oneStandardDeviationV, oneRedundantV));
        this.standardDeviationV.put(Branch.Side.TWO, new Pair<>(twoStandardDeviationV, twoRedundantV));
    }

    public boolean isObservable() {
        return observable;
    }

    @Override
    public BranchObservability<T> setObservable(boolean observable) {
        this.observable = observable;
        return this;
    }

    @Override
    public float getStandardDeviationP(Branch.Side side) {
        return standardDeviationP.get(side).getFirst();
    }

    @Override
    public BranchObservabilityImpl<T> setStandardDeviationP(float standardDeviationP, Branch.Side side) {
        this.standardDeviationP.get(side).setFirst(standardDeviationP);
        return this;
    }

    @Override
    public boolean isRedundantP(Branch.Side side) {
        return standardDeviationP.get(side).getSecond();
    }

    @Override
    public BranchObservability<T> setRedundantP(boolean redundant, Branch.Side side) {
        this.standardDeviationP.get(side).setSecond(redundant);
        return this;
    }

    @Override
    public float getStandardDeviationQ(Branch.Side side) {
        return standardDeviationQ.get(side).getFirst();
    }

    @Override
    public BranchObservabilityImpl<T> setStandardDeviationQ(float standardDeviationQ, Branch.Side side) {
        this.standardDeviationQ.get(side).setFirst(standardDeviationQ);
        return this;
    }

    @Override
    public boolean isRedundantQ(Branch.Side side) {
        return standardDeviationQ.get(side).getSecond();
    }

    @Override
    public BranchObservability<T> setRedundantQ(boolean redundant, Branch.Side side) {
        this.standardDeviationQ.get(side).setSecond(redundant);
        return this;
    }

    @Override
    public float getStandardDeviationV(Branch.Side side) {
        return standardDeviationV.get(side).getFirst();
    }

    @Override
    public BranchObservabilityImpl<T> setStandardDeviationV(float standardDeviationV, Branch.Side side) {
        this.standardDeviationV.get(side).setFirst(standardDeviationV);
        return this;
    }

    @Override
    public boolean isRedundantV(Branch.Side side) {
        return this.standardDeviationV.get(side).getSecond();
    }

    @Override
    public BranchObservability<T> setRedundantV(boolean redundant, Branch.Side side) {
        this.standardDeviationV.get(side).setSecond(redundant);
        return this;
    }
}
