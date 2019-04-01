/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionProvider;

/**
 * All immutable extension's wrapper should extend {@link AbstractImmutableWrapperExtension}.
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public interface ImmutableWrapperExtension<T extends Extendable, E extends Extension<T>> extends ExtensionProvider<T, E> {

    /**
     * @param extension the mutable extension to be wrapped
     * @param immutableExtendable the immutable extendable to be binded with the immutable extension
     * @return Returns an immutable extension
     */
    E wrap(E extension, T immutableExtendable);

}
