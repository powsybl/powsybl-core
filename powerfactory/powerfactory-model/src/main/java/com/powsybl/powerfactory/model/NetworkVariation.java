/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkVariation {

    private final DataObject intScheme;

    private final StudyCase studyCase;

    public NetworkVariation(DataObject intScheme, StudyCase studyCase) {
        this.intScheme = Objects.requireNonNull(intScheme);
        this.studyCase = Objects.requireNonNull(studyCase);
    }

    public String getName() {
        return intScheme.getLocName();
    }

    public List<NetworkExpansionStage> getExpansionStages() {
        return intScheme.getChildrenByClass("IntSstage").stream()
                .map(NetworkExpansionStage::new)
                .collect(Collectors.toList());
    }

    public List<NetworkExpansionStage> getActiveExpansionStages() {
        return getExpansionStages().stream()
                .filter(expansionStage -> !expansionStage.getActivationTime().isAfter(studyCase.getTime()))
                .collect(Collectors.toList());
    }
}
