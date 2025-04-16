/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions.util;

import com.google.common.base.Suppliers;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.extensions.ExtensionSerDe;

import java.util.function.Supplier;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public final class DefaultExtensionsSupplier implements ExtensionsSupplier {
    private static final String EXTENSION_CATEGORY_NAME = "network";

    private static final Supplier<ExtensionProviders<ExtensionSerDe>> EXTENSIONS_SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionSerDe.class, EXTENSION_CATEGORY_NAME));

    private DefaultExtensionsSupplier() {
    }

    // Bill Pugh Singleton Implementation
    private static class SingletonHelper {
        private static final DefaultExtensionsSupplier INSTANCE = new DefaultExtensionsSupplier();
    }

    public static DefaultExtensionsSupplier getInstance() {
        return SingletonHelper.INSTANCE;
    }

    @Override
    public ExtensionProviders<ExtensionSerDe> get() {
        return EXTENSIONS_SUPPLIER.get();
    }
}
