/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.tools;

import com.google.common.collect.ImmutableList;
import com.powsybl.shortcircuit.Fault;
import com.powsybl.shortcircuit.FaultParameters;
import com.powsybl.shortcircuit.ShortCircuitParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * Input data/configuration for a {@link com.powsybl.shortcircuit.ShortCircuitAnalysis} computation.
 * However, all fields must always be non {@literal null}.
 *
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class ShortCircuitInput {

    private final List<Fault> faults = new ArrayList<>();

    private ShortCircuitParameters parameters;

    private final List<FaultParameters> faultParameters = new ArrayList<>();

    public List<Fault> getFaults() {
        return faults;
    }

    public ShortCircuitParameters getParameters() {
        return parameters;
    }

    public List<FaultParameters> getFaultParameters() {
        return ImmutableList.copyOf(faultParameters);
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

    public ShortCircuitInput setFaultParameters(List<FaultParameters> faultParameters) {
        this.faultParameters.clear();
        this.faultParameters.addAll(Objects.requireNonNull(faultParameters));
        return this;
    }
}
