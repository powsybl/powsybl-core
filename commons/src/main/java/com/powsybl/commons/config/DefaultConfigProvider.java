/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

/**
 * Service interface allowing a jar to bundle a default {@code config.yml} as a classpath
 * resource. Implementations are discovered through {@link java.util.ServiceLoader} (typically
 * registered with {@code @AutoService(DefaultConfigProvider.class)}).
 *
 * <p>The configuration contributed by all providers forms the lowest-precedence layer of the
 * platform configuration: it is overridden by the distribution config ({@code $installDir/etc}),
 * the user config ({@code ~/.itools}) and environment variables.
 *
 * <p>The conventional resource location is {@code /META-INF/powsybl/config.yml}.
 *
 * @author Gautier Bureau {@literal <gautier.bureau at gmail.com>}
 */
public interface DefaultConfigProvider {

    /**
     * A stable name identifying this provider. Used in logs and to attribute overlapping
     * properties when several providers define the same default.
     */
    String getName();

    /**
     * Ordering among bundled defaults. When two providers define the same property of the same
     * module, the value of the provider with the highest priority wins. Ties are broken by
     * {@link #getName()} for determinism.
     */
    int getPriority();

    /**
     * The classpath resource holding the bundled YAML configuration, e.g.
     * {@code "/META-INF/powsybl/config.yml"}.
     */
    String getResourceName();
}
