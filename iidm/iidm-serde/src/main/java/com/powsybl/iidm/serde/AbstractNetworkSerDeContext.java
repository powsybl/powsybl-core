/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.serde.anonymizer.Anonymizer;

import java.util.Objects;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public abstract class AbstractNetworkSerDeContext<T> extends AbstractSerDeContext<T> {

    private final IidmVersion version;

    AbstractNetworkSerDeContext(Anonymizer anonymizer, IidmVersion version) {
        super(anonymizer);
        this.version = Objects.requireNonNull(version);
    }

    public IidmVersion getVersion() {
        return version;
    }
}
