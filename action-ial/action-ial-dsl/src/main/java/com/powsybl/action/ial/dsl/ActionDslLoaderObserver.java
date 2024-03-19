/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.ial.dsl;

import com.powsybl.contingency.dsl.ContingencyDslObserver;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface ActionDslLoaderObserver extends ContingencyDslObserver {

    void begin(String dslFile);

    void contingencyFound(String contingencyId);

    void ruleFound(String ruleId);

    void actionFound(String actionId);

    void end();
}
