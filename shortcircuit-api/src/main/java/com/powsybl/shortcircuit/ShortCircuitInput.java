/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.google.common.collect.ImmutableList;
import com.powsybl.security.monitor.StateMonitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * Input data/configuration for a {@link com.powsybl.shortcircuit.ShortCircuitAnalysis} computation.
 * However, all fields must always be non {@literal null}.
 *
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ShortCircuitInput {

    private final List<Fault> faults = new ArrayList<>();

    private ShortCircuitParameters parameters;

    private final List<StateMonitor> monitors = new ArrayList<>();

    public List<Fault> getFaults() {
        return faults;
    }

    public ShortCircuitParameters getParameters() {
        return parameters;
    }

    public List<StateMonitor> getMonitors() {
        return ImmutableList.copyOf(monitors);
    }

    public ShortCircuitInput setFaults(List<Fault> faults) {
        this.faults.clear();
        this.faults.addAll(Objects.requireNonNull(faults));
        return this;
    }

    public ShortCircuitInput setParameters(ShortCircuitParameters parameters) {
        this.parameters = Objects.requireNonNull(parameters);
        return this;
    }

    public ShortCircuitInput setMonitors(List<StateMonitor> monitors) {
        this.monitors.clear();
        this.monitors.addAll(Objects.requireNonNull(monitors));
        return this;
    }
}
