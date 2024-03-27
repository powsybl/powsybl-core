/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.dsl;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public interface ContingencyDslObserver {

    void begin(String dslFile);

    void contingencyFound(String contingencyId);

    void end();
}
