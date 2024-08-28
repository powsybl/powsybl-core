/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import java.util.Objects;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractExtensionSerDeAlternative<T extends Extendable, E extends Extension<T>> implements ExtensionSerDeAlternative<T, E> {

    private final String alternativeExtensionName;
    private final String categoryName;

    protected AbstractExtensionSerDeAlternative(String alternativeExtensionName, String categoryName) {
        this.alternativeExtensionName = Objects.requireNonNull(alternativeExtensionName);
        this.categoryName = Objects.requireNonNull(categoryName);
    }

    @Override
    public String getCategoryName() {
        return categoryName;
    }

    @Override
    public String getExtensionName() {
        return alternativeExtensionName;
    }
}
