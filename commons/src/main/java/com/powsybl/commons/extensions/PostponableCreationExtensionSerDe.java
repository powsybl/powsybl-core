/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import com.powsybl.commons.io.DeserializerContext;

import java.util.function.Function;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public interface PostponableCreationExtensionSerDe<T extends Extendable<T>, E extends Extension<T>>
        extends ExtensionSerDe<T, E> {

    @Override
    default E read(T extendable, DeserializerContext context) {
        return extensionCreator(context).apply(extendable);
    }

    Function<T, E> extensionCreator(DeserializerContext context);
}
