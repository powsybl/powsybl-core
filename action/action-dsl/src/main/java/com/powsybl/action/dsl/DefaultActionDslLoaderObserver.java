/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DefaultActionDslLoaderObserver implements ActionDslLoaderObserver {
    @Override
    public void begin(String dslFile) {
        // empty default implementation
    }

    @Override
    public void contingencyFound(String contingencyId) {
        // empty default implementation
    }

    @Override
    public void ruleFound(String ruleId) {
        // empty default implementation
    }

    @Override
    public void actionFound(String actionId) {
        // empty default implementation
    }

    @Override
    public void end() {
        // empty default implementation
    }
}
