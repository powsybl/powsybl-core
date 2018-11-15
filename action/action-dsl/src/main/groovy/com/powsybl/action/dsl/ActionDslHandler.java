/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl;

import com.powsybl.contingency.Contingency;

/**
 *
 * Users may define their own handling of action DSL objects,
 * by implementing this interface and calling {@link ActionDslLoader#loadDsl} method.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public interface ActionDslHandler {

    void addContingency(Contingency contingency);
    void addRule(Rule rule);
    void addAction(Action action);
}
