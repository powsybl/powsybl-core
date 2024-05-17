/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public class AreaTypeSerDe extends AbstractSimpleIdentifiableSerDe<AreaType, AreaTypeAdder, Network> {

    static final AreaTypeSerDe INSTANCE = new AreaTypeSerDe();

    static final String ROOT_ELEMENT_NAME = "areaType";
    static final String ARRAY_ELEMENT_NAME = "areaTypes";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(final AreaType areaType, final Network parent, final NetworkSerializerContext context) {
        // No root element to write except the ID
    }

    @Override
    protected AreaTypeAdder createAdder(final Network network) {
        return network.newAreaType();
    }

    @Override
    protected AreaType readRootElementAttributes(final AreaTypeAdder adder, final Network parent, final NetworkDeserializerContext context) {
        return adder.add();  // No root element to read except the ID
    }

    @Override
    protected void readSubElements(final AreaType identifiable, final NetworkDeserializerContext context) {
        // No sub-elements to read: just end reading
        context.getReader().readEndNode();
    }
}
