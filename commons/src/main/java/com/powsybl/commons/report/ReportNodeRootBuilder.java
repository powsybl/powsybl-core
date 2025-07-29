/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

/**
 * A builder to create a root {@link ReportNode} object.
 *
 * @author Alice Caron {@literal <alice.caron at rte-france.com>}
 */
public interface ReportNodeRootBuilder extends ReportNodeBuilder {

    /**
     * Provide the message to build the {@link ReportNode} with.
     * @param name functional log
     * @return a reference to this object
     */
    ReportNodeRootBuilder withName(String name);
}
