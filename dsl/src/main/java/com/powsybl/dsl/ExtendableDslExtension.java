/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.dsl;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;
import groovy.lang.Binding;
import groovy.lang.MetaClass;

import java.util.List;

/**
 * @author Paul Bui-Quang <paul.buiquang at rte-france.com>
 */
public interface ExtendableDslExtension<E extends Extendable<E>> {

    Class<E> getExtendableClass();

    void addToSpec(MetaClass extSpecMetaClass, List<Extension<E>> enxtensions, Binding binding);
}
