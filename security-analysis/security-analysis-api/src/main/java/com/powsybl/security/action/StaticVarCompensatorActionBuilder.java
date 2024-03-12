/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.action;

import com.powsybl.contingency.contingency.list.identifier.IdBasedNetworkElementIdentifier;
import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;
import com.powsybl.iidm.network.StaticVarCompensator;

import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class StaticVarCompensatorActionBuilder {

    private String id;
    private List<NetworkElementIdentifier> svcIdentifiers;
    private StaticVarCompensator.RegulationMode regulationMode;
    private Double voltageSetpoint;
    private Double reactivePowerSetpoint;

    public StaticVarCompensatorAction build() {
        return new StaticVarCompensatorAction(id, svcIdentifiers, regulationMode, voltageSetpoint, reactivePowerSetpoint);
    }

    public StaticVarCompensatorActionBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public StaticVarCompensatorActionBuilder withStaticVarCompensatorId(String staticVarCompensatorId) {
        this.svcIdentifiers = Collections.singletonList(new IdBasedNetworkElementIdentifier(staticVarCompensatorId));
        return this;
    }

    public StaticVarCompensatorActionBuilder withStaticVarCompensatorIdentifiers(List<NetworkElementIdentifier> svcIdentifiers) {
        this.svcIdentifiers = svcIdentifiers;
        return this;
    }

    public StaticVarCompensatorActionBuilder withRegulationMode(StaticVarCompensator.RegulationMode regulationMode) {
        this.regulationMode = regulationMode;
        return this;
    }

    public StaticVarCompensatorActionBuilder withVoltageSetpoint(Double voltageSetpoint) {
        this.voltageSetpoint = voltageSetpoint;
        return this;
    }

    public StaticVarCompensatorActionBuilder withReactivePowerSetpoint(Double reactivePowerSetpoint) {
        this.reactivePowerSetpoint = reactivePowerSetpoint;
        return this;
    }
}
