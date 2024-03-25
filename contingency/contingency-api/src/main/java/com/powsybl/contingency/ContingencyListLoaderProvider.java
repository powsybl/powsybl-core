/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.contingency;

import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.Objects;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public final class ContingencyListLoaderProvider {

    private static final ServiceLoaderCache<ContingencyListLoader> LOADERS = new ServiceLoaderCache<>(ContingencyListLoader.class);

    private ContingencyListLoaderProvider() {
    }

    public static ContingencyListLoader getLoader(String format) {
        Objects.requireNonNull(format);

        return LOADERS.getServices().stream()
                .filter(l -> l.getFormat().equals(format))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported format: " + format));
    }

}
