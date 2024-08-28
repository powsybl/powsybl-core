/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractExtensionSerDeAlternative<T extends Extendable, E extends Extension<T>> implements ExtensionSerDeAlternative<T, E> {

    private final String alternativeExtensionName;
    private final ExtensionSerDe<T, E> provider;

    protected AbstractExtensionSerDeAlternative(String alternativeExtensionName, ExtensionSerDe<T, E> provider) {
        this.alternativeExtensionName = Objects.requireNonNull(alternativeExtensionName);
        this.provider = Objects.requireNonNull(provider);
    }

    @Override
    public String getCategoryName() {
        return provider.getCategoryName();
    }

    @Override
    public String getExtensionName() {
        return alternativeExtensionName;
    }

    @Nonnull
    @Override
    public ExtensionSerDe<T, E> getProvider() {
        return provider;
    }
}
