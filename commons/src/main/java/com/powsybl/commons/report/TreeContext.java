/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Context attached to a {@link ReportNode} tree.
 * It contains parameters intended to be used by all the {@link ReportNode} sharing the same tree.
 * Hence, all the nodes of a tree should point to the same {@link TreeContext} through {@link ReportNode#getTreeContext()}.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface TreeContext {

    TreeContext NO_OP = new TreeContextNoOp();

    /**
     * Get the dictionary of message templates indexed by their key.
     */
    Map<String, String> getDictionary();

    /**
     * Get the {@link DateTimeFormatter} to use for timestamps, if enabled.
     */
    DateTimeFormatter getTimestampFormatter();

    /**
     * Merge given {@link TreeContext} with current one.
     */
    void merge(TreeContext treeContext);
}
