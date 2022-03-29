/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.InjectionObservability;
import com.powsybl.iidm.network.extensions.InjectionObservabilityAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class InjectionObservabilityAdderImpl<I extends Injection<I>>
        extends AbstractExtensionAdder<I, InjectionObservability<I>>
        implements InjectionObservabilityAdder<I> {

    private boolean observable;

    private double standardDeviationP = Double.NaN;

    private double standardDeviationQ = Double.NaN;

    private double standardDeviationV = Double.NaN;

    private boolean redundantP = false;

    private boolean redundantQ = false;

    private boolean redundantV = false;

    public InjectionObservabilityAdderImpl(I extendable) {
        super(extendable);
    }

    @Override
    protected InjectionObservability<I> createExtension(I extendable) {
        InjectionObservabilityImpl<I> extension = new InjectionObservabilityImpl<>(extendable, observable);
        if (!Double.isNaN(standardDeviationP)) {
            extension.setQualityP(standardDeviationP, redundantP);
        }
        if (!Double.isNaN(standardDeviationQ)) {
            extension.setQualityQ(standardDeviationQ, redundantQ);
        }
        if (!Double.isNaN(standardDeviationV)) {
            extension.setQualityV(standardDeviationV, redundantV);
        }
        return extension;
    }

    @Override
    public InjectionObservabilityAdder<I> withObservable(boolean observable) {
        this.observable = observable;
        return this;
    }

    @Override
    public InjectionObservabilityAdder<I> withStandardDeviationP(double standardDeviationP) {
        this.standardDeviationP = standardDeviationP;
        return this;
    }

    @Override
    public InjectionObservabilityAdder<I> withRedundantP(boolean redundant) {
        this.redundantP = redundant;
        return this;
    }

    @Override
    public InjectionObservabilityAdder<I> withStandardDeviationQ(double standardDeviationQ) {
        this.standardDeviationQ = standardDeviationQ;
        return this;
    }

    @Override
    public InjectionObservabilityAdder<I> withRedundantQ(boolean redundant) {
        this.redundantQ = redundant;
        return this;
    }

    @Override
    public InjectionObservabilityAdder<I> withStandardDeviationV(double standardDeviationV) {
        this.standardDeviationV = standardDeviationV;
        return this;
    }

    @Override
    public InjectionObservabilityAdder<I> withRedundantV(boolean redundant) {
        this.redundantV = redundant;
        return this;
    }
}
