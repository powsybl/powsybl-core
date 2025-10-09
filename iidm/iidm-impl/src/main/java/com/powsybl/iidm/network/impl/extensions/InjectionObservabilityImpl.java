/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.InjectionObservability;
import com.powsybl.iidm.network.extensions.ObservabilityQuality;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class InjectionObservabilityImpl<T extends Injection<T>> extends AbstractExtension<T>
        implements InjectionObservability<T> {

    private boolean observable;

    private ObservabilityQuality<T> qualityP;

    private ObservabilityQuality<T> qualityQ;

    private ObservabilityQuality<T> qualityV;

    public InjectionObservabilityImpl(T component, boolean observable) {
        super(component);
        this.observable = observable;
    }

    public InjectionObservabilityImpl(T component, boolean observable,
                                      double standardDeviationP, Boolean redundantP,
                                      double standardDeviationQ, Boolean redundantQ,
                                      double standardDeviationV, Boolean redundantV) {
        this(component, observable);
        this.qualityP = new ObservabilityQualityImpl<>(standardDeviationP, redundantP);
        this.qualityQ = new ObservabilityQualityImpl<>(standardDeviationQ, redundantQ);
        this.qualityV = new ObservabilityQualityImpl<>(standardDeviationV, redundantV);
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
    public ObservabilityQuality<T> getQualityP() {
        return qualityP;
    }

    @Override
    public InjectionObservability<T> setQualityP(double standardDeviation, Boolean redundant) {
        if (qualityP == null) {
            qualityP = new ObservabilityQualityImpl<>(standardDeviation, redundant);
        } else {
            qualityP.setStandardDeviation(standardDeviation);
            qualityP.setRedundant(redundant);
        }
        return this;
    }

    @Override
    public InjectionObservability<T> setQualityP(double standardDeviation) {
        if (qualityP == null) {
            qualityP = new ObservabilityQualityImpl<>(standardDeviation);
        } else {
            qualityP.setStandardDeviation(standardDeviation);
        }
        return this;
    }

    @Override
    public ObservabilityQuality<T> getQualityQ() {
        return qualityQ;
    }

    @Override
    public InjectionObservability<T> setQualityQ(double standardDeviation, Boolean redundant) {
        if (qualityQ == null) {
            qualityQ = new ObservabilityQualityImpl<>(standardDeviation, redundant);
        } else {
            qualityQ.setStandardDeviation(standardDeviation);
            qualityQ.setRedundant(redundant);
        }
        return this;
    }

    @Override
    public InjectionObservability<T> setQualityQ(double standardDeviation) {
        if (qualityQ == null) {
            qualityQ = new ObservabilityQualityImpl<>(standardDeviation);
        } else {
            qualityQ.setStandardDeviation(standardDeviation);
        }
        return this;
    }

    @Override
    public ObservabilityQuality<T> getQualityV() {
        return qualityV;
    }

    @Override
    public InjectionObservability<T> setQualityV(double standardDeviation, Boolean redundant) {
        if (qualityV == null) {
            qualityV = new ObservabilityQualityImpl<>(standardDeviation, redundant);
        } else {
            qualityV.setStandardDeviation(standardDeviation);
            qualityV.setRedundant(redundant);
        }
        return this;
    }

    @Override
    public InjectionObservability<T> setQualityV(double standardDeviation) {
        if (qualityV == null) {
            qualityV = new ObservabilityQualityImpl<>(standardDeviation);
        } else {
            qualityV.setStandardDeviation(standardDeviation);
        }
        return this;
    }
}
