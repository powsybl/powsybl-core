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
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public interface ImmutableWrapperExtension<T extends Extendable, E extends Extension<T>> extends ExtensionProvider<T, E> {

    E wrap(E extension, T extendable);

}
