/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ContingencyContext {

    private final String contingencyId;

    private final ContingencyContextType contextType;

    public ContingencyContext(ContingencyContextType contingencyContextType, String contingencyId) {
        this.contextType = Objects.requireNonNull(contingencyContextType);
        this.contingencyId = contingencyId;
    }

    public String getContingencyId() {
        return contingencyId;
    }

    public ContingencyContextType getContextType() {
        return contextType;
    }

    @Override
    public String toString() {
        return "ContingencyContext(" +
                "contingencyId='" + Objects.toString(contingencyId, "") + '\'' +
                ", contextType=" + contextType +
                ')';
    }

    public static ContingencyContext createAllContingencyContext() {
        return new ContingencyContext(ContingencyContextType.ALL, null);
    }

    public static ContingencyContext createNoneContingencyContext() {
        return new ContingencyContext(ContingencyContextType.NONE, null);
    }

    public static ContingencyContext createSpecificContingencyContext(String contingencyId) {
        return new ContingencyContext(ContingencyContextType.SPECIFIC, contingencyId);
    }
}
