/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.action;

import com.powsybl.contingency.contingency.list.identifier.IdBasedNetworkElementIdentifier;
import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;

import java.util.Collections;
import java.util.List;

/**
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class GeneratorActionBuilder {

    private String id;
    private List<NetworkElementIdentifier> generatorIdentifiers;
    private Boolean activePowerRelativeValue;
    private Double activePowerValue;
    private Boolean voltageRegulatorOn;
    private Double targetV;
    private Double targetQ;

    public GeneratorAction build() {
        if (activePowerRelativeValue != null ^ activePowerValue != null) {
            throw new IllegalArgumentException("For a generator action, both or none of these two attributes must be provided: activePowerValue and activePowerRelativeValue");
        }
        return new GeneratorAction(id, generatorIdentifiers, activePowerRelativeValue, activePowerValue, voltageRegulatorOn, targetV, targetQ);
    }

    public GeneratorActionBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public GeneratorActionBuilder withGeneratorId(String generatorId) {
        this.generatorIdentifiers = Collections.singletonList(new IdBasedNetworkElementIdentifier(generatorId));
        return this;
    }

    public GeneratorActionBuilder withGeneratorIdentifiers(List<NetworkElementIdentifier> networkElementIdentifiers) {
        this.generatorIdentifiers = networkElementIdentifiers;
        return this;
    }

    public GeneratorActionBuilder withActivePowerRelativeValue(boolean activePowerRelativeValue) {
        this.activePowerRelativeValue = activePowerRelativeValue;
        return this;
    }

    public GeneratorActionBuilder withActivePowerValue(double activePowerValue) {
        this.activePowerValue = activePowerValue;
        return this;
    }

    public GeneratorActionBuilder withVoltageRegulatorOn(boolean voltageRegulatorOn) {
        this.voltageRegulatorOn = voltageRegulatorOn;
        return this;
    }

    public GeneratorActionBuilder withTargetV(double targetV) {
        this.targetV = targetV;
        return this;
    }

    public GeneratorActionBuilder withTargetQ(double targetQ) {
        this.targetQ = targetQ;
        return this;
    }
}
