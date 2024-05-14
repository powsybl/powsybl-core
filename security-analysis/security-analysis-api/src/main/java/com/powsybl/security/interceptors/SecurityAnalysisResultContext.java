/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.interceptors;

import com.powsybl.iidm.network.Network;

/**
 *
 * A context associated with a part of the result of a security analysis.
 *
 * <p>Implementations of security analysis must provide such a context to the
 * {@link com.powsybl.security.SecurityAnalysisResultBuilder result builder},
 * so that {@link SecurityAnalysisInterceptor interceptors} can use it.
 *
 * They can provide additional, implementation specific information to interceptors
 * by subclassing this interface.
 *
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
public interface SecurityAnalysisResultContext {

    Network getNetwork();

}
