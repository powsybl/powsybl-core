/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction;

import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.criteria.NetworkElementCriterion;
import com.powsybl.iidm.criteria.NetworkElementVisitor;
import com.powsybl.iidm.criteria.duration.AbstractTemporaryDurationCriterion;
import com.powsybl.iidm.criteria.duration.LimitDurationCriterion;
import com.powsybl.iidm.criteria.translation.NetworkElement;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.security.limitreduction.criteria.translation.DefaultNetworkElementWithLimitsAdapter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.powsybl.contingency.ContingencyContextType.*;

/**
 * Class responsible for computing reduced limits using a {@link LimitReductionDefinitionList}.
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class ContingencyWiseReducedLimitsComputer extends AbstractReducedLimitsComputer<ContingencyWiseReducedLimitsComputer.FilterableNetworkElement> {
    private final LimitReductionDefinitionList limitReductionDefinitionList;
    private List<LimitReductionDefinition> definitionsForCurrentContingencyId = Collections.emptyList();
    private boolean sameDefinitionsAsForPreviousContingencyId = false;

    public ContingencyWiseReducedLimitsComputer(LimitReductionDefinitionList limitReductionDefinitionList) {
        super();
        this.limitReductionDefinitionList = limitReductionDefinitionList;
        computeDefinitionsForCurrentContingencyId(null);
    }

    @Override
    protected ContingencyWiseReducedLimitsComputer.FilterableNetworkElement getAdapter(Identifiable<?> identifiable) {
        return new DefaultNetworkElementWithLimitsAdapter(identifiable);
    }

    @Override
    protected OriginalLimitsGetter<ContingencyWiseReducedLimitsComputer.FilterableNetworkElement, LoadingLimits> getOriginalLimitsGetterForIdentifiables() {
        return DefaultNetworkElementWithLimitsAdapter.getOriginalLimitsGetterForIdentifiables();
    }

    @Override
    protected <T> Optional<LimitsContainer<T>> computeLimitsWithAppliedReduction(ContingencyWiseReducedLimitsComputer.FilterableNetworkElement filterable,
                                                                                 LimitType limitType, ThreeSides side,
                                                                                 OriginalLimitsGetter<ContingencyWiseReducedLimitsComputer.FilterableNetworkElement, T> originalLimitsGetter,
                                                                                 AbstractLimitsReducerCreator<T, ? extends AbstractLimitsReducer<T>> limitsReducerCreator) {
        Optional<T> originalLimits = originalLimitsGetter.getLimits(filterable, limitType, side);
        if (definitionsForCurrentContingencyId.isEmpty() || originalLimits.isEmpty()) {
            // No reductions to apply or no limits on which to apply them
            return originalLimits.map(l -> new LimitsContainer<>(l, l));
        }
        AbstractLimitsReducer<T> limitsReducer = limitsReducerCreator.create(filterable.getId(), originalLimits.get());
        updateLimitReducer(limitsReducer, filterable, limitType);

        T reducedLimits = limitsReducer.getReducedLimits();
        // Cache the value to avoid recomputing it
        LimitsContainer<T> limitsContainer = new LimitsContainer<>(reducedLimits, originalLimits.get());
        putInCache(filterable, limitType, side, limitsContainer);
        return Optional.of(limitsContainer);
    }

    private void updateLimitReducer(AbstractLimitsReducer<?> limitsReducer, NetworkElement networkElement, LimitType limitType) {
        for (LimitReductionDefinition limitReductionDefinition : definitionsForCurrentContingencyId) {
            if (limitReductionDefinition.getLimitType() == limitType &&
                    isEquipmentAffectedByLimitReduction(networkElement, limitReductionDefinition)) {
                setLimitReductionsToLimitReducer(limitsReducer, limitReductionDefinition);
            }
        }
    }

    public boolean changeContingencyId(String contingencyId) {
        var definitionsForPreviousContingencyId = definitionsForCurrentContingencyId;
        computeDefinitionsForCurrentContingencyId(contingencyId);
        sameDefinitionsAsForPreviousContingencyId = definitionsForCurrentContingencyId.equals(definitionsForPreviousContingencyId);
        if (!isSameDefinitionsAsForPreviousContingencyId()) {
            // The limit reductions are not the same as for the previous contingencyId, we clear the cache.
            clearCache();
        }
        return isSameDefinitionsAsForPreviousContingencyId();
    }

    private void computeDefinitionsForCurrentContingencyId(String contingencyId) {
        definitionsForCurrentContingencyId = limitReductionDefinitionList.getLimitReductionDefinitions().stream()
                .filter(l -> isContingencyContextListApplicable(l.getContingencyContexts(), contingencyId))
                .toList();
    }

    public boolean isSameDefinitionsAsForPreviousContingencyId() {
        return sameDefinitionsAsForPreviousContingencyId;
    }

    private void setLimitReductionsToLimitReducer(AbstractLimitsReducer<?> limitsReducer, LimitReductionDefinition limitReductionDefinition) {
        if (isPermanentLimitAffectedByLimitReduction(limitReductionDefinition)) {
            limitsReducer.setPermanentLimitReduction(limitReductionDefinition.getLimitReduction());
        }
        limitsReducer.getTemporaryLimitsAcceptableDurationStream()
                .filter(acceptableDuration -> isTemporaryLimitAffectedByLimitReduction(acceptableDuration, limitReductionDefinition))
                .forEach(acceptableDuration -> limitsReducer.setTemporaryLimitReduction(acceptableDuration,
                        limitReductionDefinition.getLimitReduction()));
    }

    protected static boolean isContingencyContextListApplicable(List<ContingencyContext> contingencyContextList, String contingencyId) {
        return contingencyContextList.isEmpty()
                || contingencyContextList.stream().anyMatch(contingencyContext ->
                    contingencyContext.getContextType() == ALL
                        || contingencyContext.getContextType() == NONE && contingencyId == null
                        || contingencyContext.getContextType() == ONLY_CONTINGENCIES && contingencyId != null
                        || contingencyContext.getContextType() == SPECIFIC && contingencyContext.getContingencyId().equals(contingencyId)
        );
    }

    protected static boolean isEquipmentAffectedByLimitReduction(NetworkElement networkElement, LimitReductionDefinition limitReductionDefinition) {
        NetworkElementVisitor networkElementVisitor = new NetworkElementVisitor(networkElement);
        List<NetworkElementCriterion> networkElementCriteria = limitReductionDefinition.getNetworkElementCriteria();
        return networkElementCriteria.isEmpty()
                || networkElementCriteria.stream().anyMatch(networkElementCriterion -> networkElementCriterion.accept(networkElementVisitor));
    }

    protected static boolean isPermanentLimitAffectedByLimitReduction(LimitReductionDefinition limitReductionDefinition) {
        return limitReductionDefinition.getDurationCriteria().isEmpty()
                || limitReductionDefinition.getDurationCriteria().stream()
                    .anyMatch(c -> c.getType().equals(LimitDurationCriterion.LimitDurationType.PERMANENT));
    }

    protected static boolean isTemporaryLimitAffectedByLimitReduction(int temporaryLimitAcceptableDuration, LimitReductionDefinition limitReductionDefinition) {
        return limitReductionDefinition.getDurationCriteria().isEmpty()
                || limitReductionDefinition.getDurationCriteria().stream()
                    .filter(limitDurationCriterion -> limitDurationCriterion.getType().equals(LimitDurationCriterion.LimitDurationType.TEMPORARY))
                    .map(AbstractTemporaryDurationCriterion.class::cast)
                    .anyMatch(c -> c.filter(temporaryLimitAcceptableDuration));
    }

    public interface FilterableNetworkElement extends Filterable, NetworkElement {
    }
}
