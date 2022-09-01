/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.anonymizer.Anonymizer;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractNetworkXmlContext<T> extends AbstractConverterContext<T> {

    private final IidmXmlVersion version;

    AbstractNetworkXmlContext(Anonymizer anonymizer, IidmXmlVersion version) {
        super(anonymizer);
        this.version = Objects.requireNonNull(version);
    }

    public IidmXmlVersion getVersion() {
        return version;
    }
}
