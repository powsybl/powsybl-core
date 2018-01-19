/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.extensions.Extendable;

import java.util.Properties;

/**
 * An object that is part of the network model and that is identified uniquely
 * by a <code>String</code> id.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Identifiable<I extends Identifiable<I>> extends Extendable<I> {

    /**
     * Get the unique identifier of the object.
     */
    String getId();

    /**
     * Get an the (optional) name  of the object.
     */
    String getName();

    /**
     * Check that this object has some properties.
     */
    boolean hasProperty();

    /**
     * Get properties associated to the object.
     */
    Properties getProperties();
}
