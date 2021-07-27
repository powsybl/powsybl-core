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

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BranchObservabilityAdderImpl<B extends Branch<B>>
        extends AbstractExtensionAdder<B, BranchObservability<B>>
        implements BranchObservabilityAdder<B> {

    private boolean observable;

    private Double standardDeviationP1 = null;

    private Double standardDeviationP2 = null;

    private Double standardDeviationQ1 = null;

    private Double standardDeviationQ2 = null;

    private Boolean redundantP1 = null;

    private Boolean redundantP2 = null;

    private Boolean redundantQ1 = null;

    private Boolean redundantQ2 = null;

    public BranchObservabilityAdderImpl(B extendable) {
        super(extendable);
    }

    @Override
    protected BranchObservability<B> createExtension(B extendable) {
        BranchObservabilityImpl<B> extension = new BranchObservabilityImpl<>(extendable, observable);
        if (standardDeviationP1 != null) {
            extension.setQualityP1(standardDeviationP1, redundantP1);
        }
        if (standardDeviationP2 != null) {
            extension.setQualityP2(standardDeviationP2, redundantP2);
        }
        if (standardDeviationQ1 != null) {
            extension.setQualityQ1(standardDeviationQ1, redundantQ1);
        }
        if (standardDeviationQ2 != null) {
            extension.setQualityQ2(standardDeviationQ2, redundantQ2);
        }
        return extension;
    }

    @Override
    public BranchObservabilityAdder<B> withObservable(boolean observable) {
        this.observable = observable;
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withStandardDeviationP1(double standardDeviationP1) {
        this.standardDeviationP1 = standardDeviationP1;
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withStandardDeviationP2(double standardDeviationP2) {
        this.standardDeviationP2 = standardDeviationP2;
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withRedundantP1(boolean redundantP1) {
        this.redundantP1 = redundantP1;
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withRedundantP2(boolean redundantP2) {
        this.redundantP2 = redundantP2;
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withStandardDeviationQ1(double standardDeviationQ1) {
        this.standardDeviationQ1 = standardDeviationQ1;
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withStandardDeviationQ2(double standardDeviationQ2) {
        this.standardDeviationQ2 = standardDeviationQ2;
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withRedundantQ1(boolean redundantQ1) {
        this.redundantQ1 = redundantQ1;
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withRedundantQ2(boolean redundantQ2) {
        this.redundantQ2 = redundantQ2;
        return this;
    }
}
