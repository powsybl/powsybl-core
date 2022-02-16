/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.sensitivity.SensitivityAnalysisParameters;

/**
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public final class SensitivityJson {

    /**
     * A configuration loader interface for the {@link SensitivityAnalysisParameters} extensions loaded from the platform configuration
     * @param <E> The extension class
     */
    public interface ExtensionSerializer<E extends Extension<SensitivityAnalysisParameters>> extends ExtensionJsonSerializer<SensitivityAnalysisParameters, E> {
    }

    /**
     *  Lazily initialized list of extension serializers.
     */
    private static final Supplier<ExtensionProviders<ExtensionSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionSerializer.class, "sensitivity-parameters"));

    /**
     *  Gets the known extension serializers.
     */
    public static ExtensionProviders<ExtensionSerializer> getExtensionSerializers() {
        return SUPPLIER.get();
    }

    private SensitivityJson() {
    }

    public static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper()
                .registerModule(new SensitivityJsonModule());
    }
}
