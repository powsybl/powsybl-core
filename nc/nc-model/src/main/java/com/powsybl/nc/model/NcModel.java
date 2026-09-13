/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.model;

import com.powsybl.nc.model.assessedelement.NcAssessedElement;
import com.powsybl.nc.model.assessedelement.NcAssessedElementWithContingency;
import com.powsybl.nc.model.assessedelement.NcAssessedElementWithRemedialAction;
import com.powsybl.nc.model.contingency.NcContingency;
import com.powsybl.nc.model.contingency.NcContingencyEquipment;
import com.powsybl.nc.model.contingency.NcContingencyWithRemedialAction;
import com.powsybl.nc.model.remedialaction.NcGridStateAlterationRemedialAction;
import com.powsybl.nc.model.remedialaction.NcRemedialActionDependency;
import com.powsybl.nc.model.remedialaction.NcRemedialActionGroup;
import com.powsybl.nc.model.remedialaction.NcRotatingMachineAction;
import com.powsybl.nc.model.remedialaction.NcShuntCompensatorModification;
import com.powsybl.nc.model.remedialaction.NcStaticPropertyRange;
import com.powsybl.nc.model.remedialaction.NcTapPositionAction;
import com.powsybl.nc.model.remedialaction.NcTopologyAction;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Non-owning baseline or timestamp-specific effective view of an NC dataset.
 *
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
public interface NcModel {
    /**
     * Creates an effective view of the same loaded NC dataset for another timestamp.
     * The returned model resolves unmodified fields through the baseline model and
     * has its own selected contexts, overrides and extensions.
     */
    NcModel forTimestamp(OffsetDateTime timestamp);

    Map<String, NcProfileMetadata> getProfileMetadata();

    Set<NcContingency> getContingencies();

    Set<NcContingencyEquipment> getContingencyEquipments();

    Set<NcAssessedElement> getAssessedElements();

    Set<NcAssessedElementWithContingency> getAssessedElementWithContingencies();

    Set<NcAssessedElementWithRemedialAction> getAssessedElementWithRemedialActions();

    Set<NcGridStateAlterationRemedialAction> getGridStateAlterationRemedialActions();

    Set<NcTopologyAction> getTopologyActions();

    Set<NcRotatingMachineAction> getRotatingMachineActions();

    Set<NcShuntCompensatorModification> getShuntCompensatorModifications();

    Set<NcTapPositionAction> getTapPositionActions();

    Set<NcStaticPropertyRange> getStaticPropertyRanges();

    Set<NcContingencyWithRemedialAction> getContingencyWithRemedialActions();

    Set<NcRemedialActionGroup> getRemedialActionGroups();

    Set<NcRemedialActionDependency> getRemedialActionDependencies();

    <E extends NcModelExtension> Optional<E> getExtension(Class<E> type);

}
