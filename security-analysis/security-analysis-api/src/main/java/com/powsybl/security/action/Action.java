/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.action;

/**
 * an action is taken according to a operator strategy when a condition occurs
 * it aims to solved contingency
 *
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public interface Action {

    String getType();

    String getId();
}
