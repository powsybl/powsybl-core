/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

/**
 * @author Maissa Souissi {@literal <maissa.souissi at rte-france.com>}
 */
public class RemoveSubstationBuilder {
    private String substationId = null;

    public RemoveSubstation build() {
        return new RemoveSubstation(substationId);
    }

    /**
     * @param substationId the non-null ID of the substation
     */
    public RemoveSubstationBuilder withSubstationId(String substationId) {
        this.substationId = substationId;
        return this;
    }
}
