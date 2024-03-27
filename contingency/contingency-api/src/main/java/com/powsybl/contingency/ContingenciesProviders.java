/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.powsybl.commons.config.ComponentDefaultConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.computation.Partition;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public final class ContingenciesProviders {

    private static final ContingenciesProvider EMPTY_PROVIDER = new EmptyContingencyListProvider();

    private ContingenciesProviders() {
    }

    /**
     * Returns a factory as defined in the {@link ComponentDefaultConfig}.
     */
    public static ContingenciesProviderFactory newDefaultFactory(PlatformConfig platformConfig) {
        return ComponentDefaultConfig.load(platformConfig).newFactoryImpl(ContingenciesProviderFactory.class);
    }

    /**
     * Returns a factory as defined in the {@link ComponentDefaultConfig}.
     */
    public static ContingenciesProviderFactory newDefaultFactory() {
        return newDefaultFactory(PlatformConfig.defaultConfig());
    }

    /**
     * Returns an empty list provider.
     */
    public static ContingenciesProvider emptyProvider() {
        return EMPTY_PROVIDER;
    }

    /**
     * Returns a contingencies provider which provides a subset of another provider,
     * defined by a {@link Partition}.
     */
    public static ContingenciesProvider newSubProvider(ContingenciesProvider provider, Partition subPart) {
        return new SubContingenciesProvider(provider, subPart);
    }

}
