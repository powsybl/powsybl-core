/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.contingency;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 * <p>
 * provide the context to get information of the network after a security analysis
 * it contains a contingency's id and a context type. Context type defines
 * if we want the information in a pre-contingency state, a post-contingency state or both.
 * contingency's id is defined if informations are needed after
 * a specific contingency computation
 */
@JsonPropertyOrder({"contextType", "contingencyId"})
public class ContingencyContext {

    private static final ContingencyContext ALL = new ContingencyContext(null, ContingencyContextType.ALL);

    private static final ContingencyContext NONE = new ContingencyContext(null, ContingencyContextType.NONE);

    private static final ContingencyContext ONLY_CONTINGENCIES = new ContingencyContext(null, ContingencyContextType.ONLY_CONTINGENCIES);

    private final String contingencyId;

    /**
     * Define if information is asked for pre-contingency state, post-contingency state or both
     * For pre-contingency state only, contingency id is null
     */
    private final ContingencyContextType contextType;

    public ContingencyContext(@JsonProperty("contingencyId") @JsonInclude(JsonInclude.Include.NON_NULL) String contingencyId,
                              @JsonProperty("contextType") ContingencyContextType contingencyContextType) {
        this.contextType = Objects.requireNonNull(contingencyContextType);
        if (contingencyContextType == ContingencyContextType.SPECIFIC && contingencyId == null) {
            throw new IllegalArgumentException("Contingency ID should not be null in case of specific contingency context");
        }
        this.contingencyId = contingencyId;
    }

    public String getContingencyId() {
        return contingencyId;
    }

    public ContingencyContextType getContextType() {
        return contextType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ContingencyContext that = (ContingencyContext) o;
        return Objects.equals(contingencyId, that.contingencyId) &&
            contextType == that.contextType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(contingencyId, contextType);
    }

    @Override
    public String toString() {
        return "ContingencyContext(" +
            "contingencyId='" + Objects.toString(contingencyId, "") + '\'' +
            ", contextType=" + contextType +
            ')';
    }

    public static ContingencyContext create(String contingencyId, ContingencyContextType contingencyContextType) {
        Objects.requireNonNull(contingencyContextType);
        switch (contingencyContextType) {
            case ALL:
                return ALL;
            case NONE:
                return NONE;
            case SPECIFIC:
                return specificContingency(contingencyId);
            case ONLY_CONTINGENCIES:
                return ONLY_CONTINGENCIES;
            default:
                throw new IllegalStateException("Unknown contingency context type: " + contingencyContextType);
        }
    }

    public static ContingencyContext all() {
        return ALL;
    }

    public static ContingencyContext none() {
        return NONE;
    }

    public static ContingencyContext onlyContingencies() {
        return ONLY_CONTINGENCIES;
    }

    public static ContingencyContext specificContingency(String contingencyId) {
        return new ContingencyContext(contingencyId, ContingencyContextType.SPECIFIC);
    }
}
