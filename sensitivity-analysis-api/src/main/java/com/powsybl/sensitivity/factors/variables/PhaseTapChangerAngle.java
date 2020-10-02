/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.factors.variables;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.sensitivity.SensitivityVariable;

import java.util.Objects;

/**
 * Sensitivity variable on phase shift transformer angle
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 * @see SensitivityVariable
 */
public class PhaseTapChangerAngle extends SensitivityVariable {

    @JsonProperty("phaseTapChangerId")
    private final String phaseTapChangerHolderId;

    /**
     * Constructor
     *
     * @param id unique identifier of the variable
     * @param name readable name of the variable
     * @param phaseTapChangerHolderId id of the network phase tap changer holder which angle is used as sensitivity variable
     * @throws NullPointerException if phaseTapChangerHolderId is null or no PhaseTapChanger exists on holder
     */
    @JsonCreator
    public PhaseTapChangerAngle(@JsonProperty("id") String id,
                                @JsonProperty("name") String name,
                                @JsonProperty("phaseTapChangerId") String phaseTapChangerHolderId) {
        super(id, name);
        this.phaseTapChangerHolderId = Objects.requireNonNull(phaseTapChangerHolderId);
    }

    /**
     * Get the id of the phase tap changer holder composing the sensitivity variable
     *
     * @return the id of the phase tap changer holder
     */
    public String getPhaseTapChangerHolderId() {
        return phaseTapChangerHolderId;
    }
}
