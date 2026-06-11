/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.scripting.groovy;

import groovy.lang.Binding;

import java.util.Map;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface GroovyScriptExtension {

    /**
     * Method used to load the extension and, usually, to bind methods or variables based on context objects
     * @param binding           The context which functions will be created in
     * @param contextObjects    Context objects (ComputationManager, Writer, etc.) used in groovy script extensions
     */
    void load(Binding binding, Map<Class<?>, Object> contextObjects);

    void unload();

}
