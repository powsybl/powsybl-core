/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation;

import com.powsybl.simulation.securityindexes.SecurityIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImpactAnalysisResult {

    private final Map<String, String> metrics;

    private final List<SecurityIndex> securityIndexes;

    public ImpactAnalysisResult(Map<String, String> metrics) {
        this(metrics, new ArrayList<>());
    }

    public ImpactAnalysisResult(Map<String, String> metrics, List<SecurityIndex> securityIndexes) {
        this.metrics = metrics;
        this.securityIndexes = securityIndexes;
    }

    public Map<String, String> getMetrics() {
        return metrics;
    }

    public void addSecurityIndex(SecurityIndex index) {
        securityIndexes.add(index);
    }

    public List<SecurityIndex> getSecurityIndexes() {
        return securityIndexes;
    }

}
