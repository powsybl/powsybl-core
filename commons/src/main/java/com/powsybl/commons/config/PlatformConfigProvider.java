/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

/**
 * Provides the default PlatformConfig returned by {@link PlatformConfig#defaultConfig()}.
 * You must have exactly one implementation of this interface on the classpath
 * found by {@link java.util.ServiceLoader}, which will be used to load it. In
 * normal use, The {@link PlatformConfigProvider#getPlatformConfig()} method will be called only once
 * and its result will be cached and reused for the whole duration of the app.
 *
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 */
public interface PlatformConfigProvider {

    String getName();

    PlatformConfig getPlatformConfig();

}
