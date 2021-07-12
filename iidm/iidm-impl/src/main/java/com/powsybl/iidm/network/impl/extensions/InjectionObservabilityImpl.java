/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.InjectionObservability;
import org.jgrapht.alg.util.Pair;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class InjectionObservabilityImpl<T extends Injection<T>> extends AbstractExtension<T>
        implements InjectionObservability<T> {

    private boolean observable;

    private final Pair<Float, Boolean> standardDeviationP;

    private final Pair<Float, Boolean> standardDeviationQ;

    private final Pair<Float, Boolean> standardDeviationV;

    public InjectionObservabilityImpl(T component, boolean observable,
                                      float standardDeviationP, boolean redundantP,
                                      float standardDeviationQ, boolean redundantQ,
                                      float standardDeviationV, boolean redundantV) {
        super(component);
        this.observable = observable;
        this.standardDeviationP = new Pair<>(standardDeviationP, redundantP);
        this.standardDeviationQ = new Pair<>(standardDeviationQ, redundantQ);
        this.standardDeviationV = new Pair<>(standardDeviationV, redundantV);
    }

    public boolean isObservable() {
        return observable;
    }

    @Override
    public InjectionObservability<T> setObservable(boolean observable) {
        this.observable = observable;
        return this;
    }

    @Override
    public float getStandardDeviationP() {
        return standardDeviationP.getFirst();
    }

    @Override
    public InjectionObservabilityImpl<T> setStandardDeviationP(float standardDeviationP) {
        this.standardDeviationP.setFirst(standardDeviationP);
        return this;
    }

    @Override
    public boolean isRedundantP() {
        return standardDeviationP.getSecond();
    }

    @Override
    public InjectionObservability<T> setRedundantP(boolean redundant) {
        this.standardDeviationP.setSecond(redundant);
        return this;
    }

    @Override
    public float getStandardDeviationQ() {
        return standardDeviationQ.getFirst();
    }

    @Override
    public InjectionObservabilityImpl<T> setStandardDeviationQ(float standardDeviationQ) {
        this.standardDeviationQ.setFirst(standardDeviationQ);
        return this;
    }

    @Override
    public boolean isRedundantQ() {
        return standardDeviationQ.getSecond();
    }

    @Override
    public InjectionObservability<T> setRedundantQ(boolean redundant) {
        this.standardDeviationQ.setSecond(redundant);
        return this;
    }

    @Override
    public float getStandardDeviationV() {
        return standardDeviationV.getFirst();
    }

    @Override
    public InjectionObservabilityImpl<T> setStandardDeviationV(float standardDeviationV) {
        this.standardDeviationV.setFirst(standardDeviationV);
        return this;
    }

    @Override
    public boolean isRedundantV() {
        return this.standardDeviationV.getSecond();
    }

    @Override
    public InjectionObservability<T> setRedundantV(boolean redundant) {
        this.standardDeviationV.setSecond(redundant);
        return this;
    }
}
