/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.extensions.BranchObservability;
import com.powsybl.iidm.network.extensions.ObservabilityQuality;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class BranchObservabilityImpl<B extends Branch<B>> extends AbstractExtension<B>
        implements BranchObservability<B> {

    private boolean observable;

    private ObservabilityQuality<B> qualityP1;

    private ObservabilityQuality<B> qualityP2;

    private ObservabilityQuality<B> qualityQ1;

    private ObservabilityQuality<B> qualityQ2;

    public BranchObservabilityImpl(B component, boolean observable) {
        super(component);
        this.observable = observable;
    }

    public BranchObservabilityImpl(B component, boolean observable,
                                   double standardDeviationP1, Boolean redundantP1,
                                   double standardDeviationP2, Boolean redundantP2,
                                   double standardDeviationQ1, Boolean redundantQ1,
                                   double standardDeviationQ2, Boolean redundantQ2) {
        this(component, observable);
        this.qualityP1 = new ObservabilityQualityImpl<>(standardDeviationP1, redundantP1);
        this.qualityP2 = new ObservabilityQualityImpl<>(standardDeviationP2, redundantP2);
        this.qualityQ1 = new ObservabilityQualityImpl<>(standardDeviationQ1, redundantQ1);
        this.qualityQ2 = new ObservabilityQualityImpl<>(standardDeviationQ2, redundantQ2);
    }

    public boolean isObservable() {
        return observable;
    }

    @Override
    public BranchObservability<B> setObservable(boolean observable) {
        this.observable = observable;
        return this;
    }

    @Override
    public ObservabilityQuality<B> getQualityP1() {
        return qualityP1;
    }

    @Override
    public BranchObservability<B> setQualityP1(double standardDeviation, Boolean redundant) {
        if (qualityP1 == null) {
            qualityP1 = new ObservabilityQualityImpl<>(standardDeviation, redundant);
        } else {
            qualityP1.setStandardDeviation(standardDeviation);
            qualityP1.setRedundant(redundant);
        }
        return this;
    }

    @Override
    public BranchObservability<B> setQualityP1(double standardDeviation) {
        if (qualityP1 == null) {
            qualityP1 = new ObservabilityQualityImpl<>(standardDeviation);
        } else {
            qualityP1.setStandardDeviation(standardDeviation);
        }
        return this;
    }

    @Override
    public ObservabilityQuality<B> getQualityP2() {
        return qualityP2;
    }

    @Override
    public BranchObservability<B> setQualityP2(double standardDeviation, Boolean redundant) {
        if (qualityP2 == null) {
            qualityP2 = new ObservabilityQualityImpl<>(standardDeviation, redundant);
        } else {
            qualityP2.setStandardDeviation(standardDeviation);
            qualityP2.setRedundant(redundant);
        }
        return this;
    }

    @Override
    public BranchObservability<B> setQualityP2(double standardDeviation) {
        if (qualityP2 == null) {
            qualityP2 = new ObservabilityQualityImpl<>(standardDeviation);
        } else {
            qualityP2.setStandardDeviation(standardDeviation);
        }
        return this;
    }

    @Override
    public ObservabilityQuality<B> getQualityQ1() {
        return qualityQ1;
    }

    @Override
    public BranchObservability<B> setQualityQ1(double standardDeviation, Boolean redundant) {
        if (qualityQ1 == null) {
            qualityQ1 = new ObservabilityQualityImpl<>(standardDeviation, redundant);
        } else {
            qualityQ1.setStandardDeviation(standardDeviation);
            qualityQ1.setRedundant(redundant);
        }
        return this;
    }

    @Override
    public BranchObservability<B> setQualityQ1(double standardDeviation) {
        if (qualityQ1 == null) {
            qualityQ1 = new ObservabilityQualityImpl<>(standardDeviation);
        } else {
            qualityQ1.setStandardDeviation(standardDeviation);
        }
        return this;
    }

    @Override
    public ObservabilityQuality<B> getQualityQ2() {
        return qualityQ2;
    }

    @Override
    public BranchObservability<B> setQualityQ2(double standardDeviation, Boolean redundant) {
        if (qualityQ2 == null) {
            qualityQ2 = new ObservabilityQualityImpl<>(standardDeviation, redundant);
        } else {
            qualityQ2.setStandardDeviation(standardDeviation);
            qualityQ2.setRedundant(redundant);
        }
        return this;
    }

    @Override
    public BranchObservability<B> setQualityQ2(double standardDeviation) {
        if (qualityQ2 == null) {
            qualityQ2 = new ObservabilityQualityImpl<>(standardDeviation);
        } else {
            qualityQ2.setStandardDeviation(standardDeviation);
        }
        return this;
    }
}
