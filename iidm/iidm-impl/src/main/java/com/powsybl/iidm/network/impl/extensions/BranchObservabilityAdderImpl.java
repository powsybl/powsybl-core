/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.extensions.BranchObservability;
import com.powsybl.iidm.network.extensions.BranchObservabilityAdder;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class BranchObservabilityAdderImpl<B extends Branch<B>>
        extends AbstractExtensionAdder<B, BranchObservability<B>>
        implements BranchObservabilityAdder<B> {

    private boolean observable;

    private double standardDeviationP1 = Double.NaN;

    private double standardDeviationP2 = Double.NaN;

    private double standardDeviationQ1 = Double.NaN;

    private double standardDeviationQ2 = Double.NaN;

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
        if (!Double.isNaN(standardDeviationP1)) {
            extension.setQualityP1(standardDeviationP1, redundantP1);
        }
        if (!Double.isNaN(standardDeviationP2)) {
            extension.setQualityP2(standardDeviationP2, redundantP2);
        }
        if (!Double.isNaN(standardDeviationQ1)) {
            extension.setQualityQ1(standardDeviationQ1, redundantQ1);
        }
        if (!Double.isNaN(standardDeviationQ2)) {
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
    public BranchObservabilityAdder<B> withRedundantP1(Boolean redundantP1) {
        this.redundantP1 = redundantP1;
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withRedundantP2(Boolean redundantP2) {
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
    public BranchObservabilityAdder<B> withRedundantQ1(Boolean redundantQ1) {
        this.redundantQ1 = redundantQ1;
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withRedundantQ2(Boolean redundantQ2) {
        this.redundantQ2 = redundantQ2;
        return this;
    }
}
