/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.List;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public final class ExtinctExtensionSerDeService {

    private static final ServiceLoaderCache<ExtinctExtensionSerDe> EXTINCT_SERDE_SERVICE = new ServiceLoaderCache<>(ExtinctExtensionSerDe.class);

    private ExtinctExtensionSerDeService() {
    }

    public static List<ExtinctExtensionSerDe> findAll() {
        return EXTINCT_SERDE_SERVICE.getServices();
    }

}
