/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.nc.model.triplestore;

import com.powsybl.nc.model.NcKeyword;
import com.powsybl.nc.model.NcModel;
import com.powsybl.nc.model.NcModelExtension;
import com.powsybl.nc.model.NcProfileMetadata;
import com.powsybl.nc.model.assessedelement.NcAssessedElement;
import com.powsybl.nc.model.assessedelement.NcAssessedElementWithContingency;
import com.powsybl.nc.model.assessedelement.NcAssessedElementWithRemedialAction;
import com.powsybl.nc.model.contingency.NcContingency;
import com.powsybl.nc.model.contingency.NcContingencyEquipment;
import com.powsybl.nc.model.contingency.NcContingencyWithRemedialAction;
import com.powsybl.nc.model.io.NcConstants;
import com.powsybl.nc.model.io.NcOverrideKey;
import com.powsybl.nc.model.io.NcOverridingObjectsFields;
import com.powsybl.nc.model.io.NcPropertyBagsConverter;
import com.powsybl.nc.model.io.NcQueryContext;
import com.powsybl.nc.model.io.NcUtils;
import com.powsybl.nc.model.remedialaction.NcGridStateAlterationRemedialAction;
import com.powsybl.nc.model.remedialaction.NcRemedialActionDependency;
import com.powsybl.nc.model.remedialaction.NcRemedialActionGroup;
import com.powsybl.nc.model.remedialaction.NcRotatingMachineAction;
import com.powsybl.nc.model.remedialaction.NcShuntCompensatorModification;
import com.powsybl.nc.model.remedialaction.NcStaticPropertyRange;
import com.powsybl.nc.model.remedialaction.NcTapPositionAction;
import com.powsybl.nc.model.remedialaction.NcTopologyAction;
import com.powsybl.triplestore.api.PropertyBags;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Jean-Pierre Arnould {@literal <jean-pierre.arnould at rte-france.com>}
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
public class NcModelTripleStore implements NcModel {

    private final NcDatasetTripleStore dataset;
    private final NcProfileSelector.Selection profileSelection;
    private final Map<NcOverrideKey, String> overridingData;
    private final Map<Class<? extends NcModelExtension>, NcModelExtension> extensions = new HashMap<>();

    NcModelTripleStore(NcDatasetTripleStore dataset, OffsetDateTime timestamp) {
        this.dataset = dataset;
        if (timestamp == null) {
            this.profileSelection = NcProfileSelector.baseline(dataset.getProfileMetadata());
            this.overridingData = Map.of();
        } else {
            this.profileSelection = NcProfileSelector.forTimestamp(dataset.getProfileMetadata(), timestamp,
                dataset.getReportNode());
            this.overridingData = NcOverrideResolver.resolve(timestamp, profileSelection, dataset.getQueryExecutor());
        }
        NcQueryContext queryContext = new NcQueryContextTripleStore(this);
        dataset.getPostProcessors().forEach(postProcessor -> postProcessor.process(this, queryContext));
    }

    @Override
    public NcModel forTimestamp(OffsetDateTime timestamp) {
        return dataset.forTimestamp(timestamp);
    }

    private Set<String> getContextNamesToRequest(NcKeyword keyword) {
        return profileSelection.contexts(keyword);
    }

    PropertyBags queryExtension(NcKeyword keyword, String contextQueryTemplate) {
        return dataset.getQueryExecutor().queryExtension(getContextNamesToRequest(keyword), contextQueryTemplate);
    }

    @Override
    public Map<String, NcProfileMetadata> getProfileMetadata() {
        dataset.checkOpen();
        return profileSelection.profileMetadata();
    }

    @Override
    public <E extends NcModelExtension> Optional<E> getExtension(Class<E> type) {
        dataset.checkOpen();
        return Optional.ofNullable(type.cast(extensions.get(type)));
    }

    <E extends NcModelExtension> void addExtension(Class<E> type, E extension) {
        extensions.put(type, extension);
    }

    /**
     * Queries the contexts selected for this view. Results are cached on the owning dataset by
     * query and context set, so views that select the same contexts share a single query execution.
     */
    private PropertyBags getPropertyBags(NcKeyword keyword, String... queries) {
        dataset.checkOpen();
        Set<String> namesToRequest = getContextNamesToRequest(keyword);
        if (namesToRequest.isEmpty()) {
            return new PropertyBags();
        }
        List<String> queryKeys = List.of(queries);
        return dataset.cachedQuery(keyword, queryKeys, namesToRequest,
            () -> dataset.getQueryExecutor().query(queryKeys, namesToRequest));
    }

    private PropertyBags getPropertyBags(NcKeyword keyword, NcOverridingObjectsFields withOverride,
                                         String... queries) {
        PropertyBags baselineData = getPropertyBags(keyword, queries);
        return withOverride == null ? baselineData : NcUtils.overrideData(baselineData, overridingData, withOverride);
    }

    @Override
    public Set<NcContingency> getContingencies() {
        return NcPropertyBagsConverter.convert(
            getPropertyBags(NcKeyword.CONTINGENCY, NcOverridingObjectsFields.CONTINGENCY,
                NcConstants.REQUEST_CONTINGENCY),
            NcContingency::fromPropertyBag);
    }

    @Override
    public Set<NcContingencyEquipment> getContingencyEquipments() {
        return NcPropertyBagsConverter.convert(getPropertyBags(NcKeyword.CONTINGENCY, NcConstants.REQUEST_CONTINGENCY_EQUIPMENT), NcContingencyEquipment::fromPropertyBag);
    }

    @Override
    public Set<NcAssessedElement> getAssessedElements() {
        return NcPropertyBagsConverter.convert(
            getPropertyBags(NcKeyword.ASSESSED_ELEMENT, NcOverridingObjectsFields.ASSESSED_ELEMENT, NcConstants.REQUEST_ASSESSED_ELEMENT),
            NcAssessedElement::fromPropertyBag);
    }

    @Override
    public Set<NcAssessedElementWithContingency> getAssessedElementWithContingencies() {
        return NcPropertyBagsConverter.convert(
            getPropertyBags(NcKeyword.ASSESSED_ELEMENT, NcOverridingObjectsFields.ASSESSED_ELEMENT_WITH_CONTINGENCY, NcConstants.REQUEST_ASSESSED_ELEMENT_WITH_CONTINGENCY),
            NcAssessedElementWithContingency::fromPropertyBag);
    }

    @Override
    public Set<NcAssessedElementWithRemedialAction> getAssessedElementWithRemedialActions() {
        return NcPropertyBagsConverter.convert(
            getPropertyBags(NcKeyword.ASSESSED_ELEMENT, NcOverridingObjectsFields.ASSESSED_ELEMENT_WITH_REMEDIAL_ACTION, NcConstants.REQUEST_ASSESSED_ELEMENT_WITH_REMEDIAL_ACTION),
            NcAssessedElementWithRemedialAction::fromPropertyBag);
    }

    @Override
    public Set<NcGridStateAlterationRemedialAction> getGridStateAlterationRemedialActions() {
        return NcPropertyBagsConverter.convert(
            getPropertyBags(NcKeyword.REMEDIAL_ACTION, NcOverridingObjectsFields.GRID_STATE_ALTERATION_REMEDIAL_ACTION, NcConstants.GRID_STATE_ALTERATION_REMEDIAL_ACTION),
            NcGridStateAlterationRemedialAction::fromPropertyBag);
    }

    @Override
    public Set<NcTopologyAction> getTopologyActions() {
        return NcPropertyBagsConverter.convert(getPropertyBags(NcKeyword.REMEDIAL_ACTION, NcOverridingObjectsFields.TOPOLOGY_ACTION, NcConstants.TOPOLOGY_ACTION), NcTopologyAction::fromPropertyBag);
    }

    @Override
    public Set<NcRotatingMachineAction> getRotatingMachineActions() {
        return NcPropertyBagsConverter.convert(
            getPropertyBags(NcKeyword.REMEDIAL_ACTION, NcOverridingObjectsFields.ROTATING_MACHINE_ACTION, NcConstants.ROTATING_MACHINE_ACTION),
            NcRotatingMachineAction::fromPropertyBag);
    }

    @Override
    public Set<NcShuntCompensatorModification> getShuntCompensatorModifications() {
        return NcPropertyBagsConverter.convert(
            getPropertyBags(NcKeyword.REMEDIAL_ACTION, NcOverridingObjectsFields.SHUNT_COMPENSATOR_MODIFICATION, NcConstants.SHUNT_COMPENSATOR_MODIFICATION),
            NcShuntCompensatorModification::fromPropertyBag);
    }

    @Override
    public Set<NcTapPositionAction> getTapPositionActions() {
        return NcPropertyBagsConverter.convert(
            getPropertyBags(NcKeyword.REMEDIAL_ACTION, NcOverridingObjectsFields.TAP_POSITION_ACTION, NcConstants.TAP_POSITION_ACTION),
            NcTapPositionAction::fromPropertyBag);
    }

    @Override
    public Set<NcStaticPropertyRange> getStaticPropertyRanges() {
        return NcPropertyBagsConverter.convert(
            getPropertyBags(NcKeyword.REMEDIAL_ACTION, NcOverridingObjectsFields.STATIC_PROPERTY_RANGE, NcConstants.STATIC_PROPERTY_RANGE),
            NcStaticPropertyRange::fromPropertyBag);
    }

    @Override
    public Set<NcContingencyWithRemedialAction> getContingencyWithRemedialActions() {
        return NcPropertyBagsConverter.convert(
            getPropertyBags(NcKeyword.REMEDIAL_ACTION, NcOverridingObjectsFields.CONTINGENCY_WITH_REMEDIAL_ACTION, NcConstants.REQUEST_CONTINGENCY_WITH_REMEDIAL_ACTION),
            NcContingencyWithRemedialAction::fromPropertyBag);
    }

    @Override
    public Set<NcRemedialActionGroup> getRemedialActionGroups() {
        return NcPropertyBagsConverter.convert(getPropertyBags(NcKeyword.REMEDIAL_ACTION, NcConstants.REQUEST_REMEDIAL_ACTION_GROUP), NcRemedialActionGroup::fromPropertyBag);

    }

    @Override
    public Set<NcRemedialActionDependency> getRemedialActionDependencies() {
        return NcPropertyBagsConverter.convert(
            getPropertyBags(NcKeyword.REMEDIAL_ACTION, NcOverridingObjectsFields.SCHEME_REMEDIAL_ACTION_DEPENDENCY, NcConstants.REQUEST_REMEDIAL_ACTION_DEPENDENCY),
            NcRemedialActionDependency::fromPropertyBag);
    }

}
