/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.OverloadManagementSystem;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class OverloadManagementSystemMockExt extends AbstractExtension<OverloadManagementSystem> {
    private final String foo;

    public OverloadManagementSystemMockExt(OverloadManagementSystem overloadManagementSystem, String foo) {
        super(overloadManagementSystem);
        this.foo = foo;
    }

    public String getFoo() {
        return foo;
    }

    @Override
    public String getName() {
        return "omsMock";
    }
}
