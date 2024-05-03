/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.execution;

import com.google.common.io.ByteSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.SecurityAnalysisParameters;

import java.util.Objects;

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
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class SecurityAnalysisExecutionInput extends AbstractSecurityAnalysisExecutionInput<SecurityAnalysisExecutionInput> {

    private SecurityAnalysisParameters parameters;

    public SecurityAnalysisParameters getParameters() {
        return parameters;
    }

    public SecurityAnalysisExecutionInput setParameters(SecurityAnalysisParameters parameters) {
        this.parameters = Objects.requireNonNull(parameters);
        return self();
    }

    @Override
    protected SecurityAnalysisExecutionInput self() {
        return this;
    }
}
