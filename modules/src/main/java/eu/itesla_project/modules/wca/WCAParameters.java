/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.wca;

import eu.itesla_project.simulation.securityindexes.SecurityIndexType;
import org.joda.time.Interval;

import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class WCAParameters {

    private final Interval histoInterval;

    private final String offlineWorkflowId; // null means, do not use offline security rules

    private final Set<SecurityIndexType> securityIndexTypes; // all security index types if null

    private final double purityThreshold;

    private final boolean stopWcaOnViolations;

    public WCAParameters(Interval histoInterval, String offlineWorkflowId, Set<SecurityIndexType> securityIndexTypes, double purityThreshold, 
            boolean stopWcaOnViolations) {
        this.histoInterval = Objects.requireNonNull(histoInterval);
        this.offlineWorkflowId = offlineWorkflowId;
        this.securityIndexTypes = securityIndexTypes;
        this.purityThreshold = purityThreshold;
        this.stopWcaOnViolations = stopWcaOnViolations;
    }

    public Interval getHistoInterval() {
        return histoInterval;
    }

    public String getOfflineWorkflowId() {
        return offlineWorkflowId;
    }

    public Set<SecurityIndexType> getSecurityIndexTypes() {
        return securityIndexTypes;
    }

    public double getPurityThreshold() {
        return purityThreshold;
    }

    public boolean stopWcaOnViolations() {
        return stopWcaOnViolations;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [histoInterval=" + histoInterval +
                ", offlineWorkflowId=" + offlineWorkflowId +
                ", securityIndexTypes=" + securityIndexTypes +
                ", purityThreshold=" + purityThreshold +
                ", stopWcaOnViolations=" + stopWcaOnViolations +
                "]";
    }
}
