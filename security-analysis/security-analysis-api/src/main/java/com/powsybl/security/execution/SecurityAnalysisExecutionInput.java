/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.execution;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.LimitViolationType;
import com.powsybl.security.SecurityAnalysisInput;
import com.powsybl.security.SecurityAnalysisParameters;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptors;

import java.util.*;

/**
 * Arguments for a {@link SecurityAnalysisExecution} :
 * <ul>
 *     <li>a {@link Network} and the variant to be considered</li>
 *     <li>some {@link SecurityAnalysisParameters}</li>
 *     <li>a set of requested result extensions</li>
 *     <li>the set of violation types to be considered</li>
 *     <li>an optional {@link ByteSource} which describes contingencies</li>
 * </ul>
 *
 * <p>Design note: here we only want serializable objects for forwarding purpose,
 * therefore some fields are fully serializable business objects
 * while others are more in their "source" format as they do not support serialization out of the box.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class SecurityAnalysisExecutionInput {

    private NetworkVariant networkVariant;
    private ByteSource contingenciesSource;
    private SecurityAnalysisParameters parameters;
    private List<String> resultExtensions = new ArrayList<>();
    private Set<LimitViolationType> violationTypes = EnumSet.noneOf(LimitViolationType.class);

    public Optional<ByteSource> getContingenciesSource() {
        return Optional.ofNullable(contingenciesSource);
    }

    public List<String> getResultExtensions() {
        return resultExtensions;
    }

    public Set<LimitViolationType> getViolationTypes() {
        return violationTypes;
    }

    public SecurityAnalysisParameters getParameters() {
        return parameters;
    }

    public NetworkVariant getNetworkVariant() {
        return networkVariant;
    }

    public void setContingenciesSource(ByteSource contingenciesSource) {
        this.contingenciesSource = contingenciesSource;
    }

    public void addResultExtension(String resultExtensions) {
        this.resultExtensions.add(Objects.requireNonNull(resultExtensions));
    }

    public void addResultExtensions(Collection<String> resultExtensions) {
        this.resultExtensions.addAll(Objects.requireNonNull(resultExtensions));
    }

    public void addViolationType(LimitViolationType violationType) {
        this.violationTypes.add(Objects.requireNonNull(violationType));
    }

    public void addViolationTypes(Collection<LimitViolationType> violationTypes) {
        this.violationTypes.addAll(Objects.requireNonNull(violationTypes));
    }

    public void setParameters(SecurityAnalysisParameters parameters) {
        this.parameters = parameters;
    }

    public void setNetworkVariant(Network network, String variantId) {
        this.networkVariant = new NetworkVariant(network, variantId);
    }

    public void copyTo(SecurityAnalysisInput input) {
        input.setParameters(parameters);
        resultExtensions.stream()
                .map(SecurityAnalysisInterceptors::createInterceptor)
                .forEach(input::addInterceptor);
        if (!violationTypes.isEmpty()) {
            input.getFilter().setViolationTypes(ImmutableSet.copyOf(violationTypes));
        }
    }
}
