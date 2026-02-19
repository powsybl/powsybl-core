/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.naming.mock;

import com.powsybl.cgmes.conversion.naming.CgmesObjectReference;
import com.powsybl.cgmes.conversion.naming.IdentityNamingStrategy;
import com.powsybl.cgmes.conversion.naming.NamingStrategy;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.network.Identifiable;

/**
 * Simple mock naming strategy used for tests. It prefixes all generated CGMES ids with "MOCK_".
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class MockNamingStrategy implements NamingStrategy {

    private final NamingStrategy delegate = new IdentityNamingStrategy();

    @Override
    public String getName() {
        return "mock";
    }

    @Override
    public String getIidmId(String type, String id) {
        return delegate.getIidmId(type, id);
    }

    @Override
    public String getIidmName(String type, String name) {
        return delegate.getIidmName(type, name);
    }

    @Override
    public String getCgmesId(Identifiable<?> identifiable) {
        return "MOCK_" + delegate.getCgmesId(identifiable);
    }

    @Override
    public String getCgmesId(CgmesObjectReference... refs) {
        return "MOCK_" + delegate.getCgmesId(refs);
    }

    @Override
    public String getCgmesId(String identifier) {
        return "MOCK_" + delegate.getCgmesId(identifier);
    }

    @Override
    public String getCgmesIdFromAlias(Identifiable<?> identifiable, String aliasType) {
        return "MOCK_" + delegate.getCgmesIdFromAlias(identifiable, aliasType);
    }

    @Override
    public String getCgmesIdFromProperty(Identifiable<?> identifiable, String propertyName) {
        return "MOCK_" + delegate.getCgmesIdFromProperty(identifiable, propertyName);
    }

    @Override
    public void debug(String baseName, DataSource ds) {
        // no-op
    }
}
