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
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
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
@JsonPropertyOrder({"contextType", "contingencyIds"})
public class ContingencyContext {

    private static final ContingencyContext ALL = new ContingencyContext(Collections.emptyList(), ContingencyContextType.ALL);

    private static final ContingencyContext NONE = new ContingencyContext(Collections.emptyList(), ContingencyContextType.NONE);

    private static final ContingencyContext ONLY_CONTINGENCIES = new ContingencyContext(Collections.emptyList(), ContingencyContextType.ONLY_CONTINGENCIES);

    private final List<String> contingencyIds;

    /**
     * Define if information is asked for pre-contingency state, post-contingency state or both
     * For pre-contingency state only, contingency id is null
     */
    private final ContingencyContextType contextType;

    public ContingencyContext(String contingencyId,
                              ContingencyContextType contingencyContextType) {
        this(Collections.singletonList(contingencyId), contingencyContextType);
    }

    public ContingencyContext(@JsonProperty("contingencyIds") @JsonInclude(JsonInclude.Include.NON_EMPTY) List<String> contingencyIds,
                              @JsonProperty("contextType") ContingencyContextType contingencyContextType) {
        this.contingencyIds = contingencyIds == null ? Collections.emptyList() : contingencyIds;
        this.contextType = Objects.requireNonNull(contingencyContextType);
        if (contingencyContextType == ContingencyContextType.SPECIFIC && this.contingencyIds.isEmpty()) {
            throw new IllegalArgumentException("Contingency IDs should not be empty in case of specific contingency context");
        } else if (contingencyContextType != ContingencyContextType.SPECIFIC && !this.contingencyIds.isEmpty()) {
            throw new IllegalArgumentException("Contingency IDs should be empty in case of not a specific contingency context");
        }
    }

    public static ContingencyContext specificContingency(String contingencyId) {
        return new ContingencyContext(contingencyId, ContingencyContextType.SPECIFIC);
    }

    public static ContingencyContext specificContingency(List<String> contingencyIds) {
        return new ContingencyContext(contingencyIds, ContingencyContextType.SPECIFIC);
    }

    public List<String> getContingencyIds() {
        return ImmutableList.copyOf(contingencyIds);
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
        return Objects.equals(contingencyIds, that.contingencyIds) &&
            contextType == that.contextType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(contingencyIds, contextType);
    }

    @Override
    public String toString() {
        return "ContingencyContext(" +
            "contingencyIds=" + contingencyIds +
            ", contextType=" + contextType +
            ')';
    }

    public static ContingencyContext create(String contingencyId, ContingencyContextType contingencyContextType) {
        return create(Collections.singletonList(contingencyId), contingencyContextType);
    }

    public static ContingencyContext create(List<String> contingencyIds, ContingencyContextType contingencyContextType) {
        Objects.requireNonNull(contingencyContextType);
        return switch (contingencyContextType) {
            case ALL -> ALL;
            case NONE -> NONE;
            case SPECIFIC -> specificContingency(contingencyIds);
            case ONLY_CONTINGENCIES -> ONLY_CONTINGENCIES;
        };
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
}
