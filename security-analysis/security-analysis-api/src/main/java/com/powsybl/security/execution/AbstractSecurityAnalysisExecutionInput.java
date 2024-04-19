/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.execution;

import com.google.common.io.ByteSource;
import com.powsybl.action.Action;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.AbstractSecurityAnalysisParameters;
import com.powsybl.security.LimitViolationType;
import com.powsybl.security.limitreduction.LimitReduction;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.security.strategy.OperatorStrategy;

import java.util.*;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public abstract class AbstractSecurityAnalysisExecutionInput<T extends AbstractSecurityAnalysisExecutionInput<T, S>,
        S extends AbstractSecurityAnalysisParameters<S>> {

    private NetworkVariant networkVariant;
    private ByteSource contingenciesSource;
    private final List<String> resultExtensions = new ArrayList<>();
    private final Set<LimitViolationType> violationTypes = EnumSet.noneOf(LimitViolationType.class);
    private boolean withLogs = false;
    private final List<OperatorStrategy> operatorStrategies = new ArrayList<>();
    private final List<Action> actions = new ArrayList<>();
    private final List<StateMonitor> monitors = new ArrayList<>();
    private final List<LimitReduction> limitReductions = new ArrayList<>();

    public Optional<ByteSource> getContingenciesSource() {
        return Optional.ofNullable(contingenciesSource);
    }

    public List<String> getResultExtensions() {
        return Collections.unmodifiableList(resultExtensions);
    }

    public Set<LimitViolationType> getViolationTypes() {
        return Collections.unmodifiableSet(violationTypes);
    }

    public NetworkVariant getNetworkVariant() {
        return networkVariant;
    }

    public List<OperatorStrategy> getOperatorStrategies() {
        return Collections.unmodifiableList(operatorStrategies);
    }

    public List<Action> getActions() {
        return Collections.unmodifiableList(actions);
    }

    public List<StateMonitor> getMonitors() {
        return Collections.unmodifiableList(monitors);
    }

    public List<LimitReduction> getLimitReductions() {
        return Collections.unmodifiableList(limitReductions);
    }

    public boolean isWithLogs() {
        return withLogs;
    }

    public T setContingenciesSource(ByteSource contingenciesSource) {
        this.contingenciesSource = contingenciesSource;
        return self();
    }

    public T addResultExtension(String resultExtension) {
        resultExtensions.add(Objects.requireNonNull(resultExtension));
        return self();
    }

    public T addResultExtensions(Collection<String> resultExtensions) {
        this.resultExtensions.addAll(Objects.requireNonNull(resultExtensions));
        return self();
    }

    public T addViolationType(LimitViolationType violationType) {
        violationTypes.add(Objects.requireNonNull(violationType));
        return self();
    }

    public T addViolationTypes(Collection<LimitViolationType> violationTypes) {
        this.violationTypes.addAll(Objects.requireNonNull(violationTypes));
        return self();
    }

    public T addOperatorStrategies(List<OperatorStrategy> operatorStrategies) {
        this.operatorStrategies.addAll(Objects.requireNonNull(operatorStrategies));
        return self();
    }

    public T addActions(List<Action> actions) {
        this.actions.addAll(Objects.requireNonNull(actions));
        return self();
    }

    public T setNetworkVariant(Network network, String variantId) {
        networkVariant = new NetworkVariant(network, variantId);
        return self();
    }

    public T setOperatorStrategies(List<OperatorStrategy> operatorStrategies) {
        Objects.requireNonNull(operatorStrategies);
        this.operatorStrategies.clear();
        this.operatorStrategies.addAll(operatorStrategies);
        return self();
    }

    public T setActions(List<Action> actions) {
        Objects.requireNonNull(actions);
        this.actions.clear();
        this.actions.addAll(actions);
        return self();
    }

    public T setMonitors(List<StateMonitor> monitors) {
        Objects.requireNonNull(monitors);
        this.monitors.clear();
        this.monitors.addAll(monitors);
        return self();
    }

    public T setLimitReductions(List<LimitReduction> limitReductions) {
        Objects.requireNonNull(limitReductions);
        this.limitReductions.clear();
        this.limitReductions.addAll(limitReductions);
        return self();
    }

    public T setWithLogs(boolean withLogs) {
        this.withLogs = withLogs;
        return self();
    }

    protected abstract T self();

    public abstract S getParameters();
}
