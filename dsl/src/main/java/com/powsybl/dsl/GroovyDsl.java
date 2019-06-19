/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dsl;

import groovy.lang.Binding;

/**
 * A groovy DSL has the ability to define groovy variables and methods into a groovy {@link Binding},
 * allowing to build expressive languages usable for arbitrary purposes
 * (contingency definitions, ... ).
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public interface GroovyDsl<C>  {

    /**
     * Gives the {@link Binding} the ability to interpret some keywords,
     * typically by binding functions to keywords.
     *
     * <p>Those functions may use the provided context for getting input data as well as
     * providing / updating output data, when an actual script will be evaluated.
     *
     * @param binding The groovy binding to be enhanced with this DSL.
     * @param context The context available for interpretation.
     */
    void enable(Binding binding, C context);

}
