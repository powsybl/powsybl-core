/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.interceptors;

import com.powsybl.iidm.network.Network;

import java.util.Objects;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class NSituationHolderContext extends SecurityAnalysisResultContextImpl {

    private final String nSituationId;

    public NSituationHolderContext(Network network, String nSituationId) {
        super(network);
        this.nSituationId = Objects.requireNonNull(nSituationId);
    }

    public String getNSituationId() {
        return nSituationId;
    }
}
