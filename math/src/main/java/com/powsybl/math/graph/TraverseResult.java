/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph;

/**
 * Result of graph traversal step, used to decide whether to pursue or not current traversal.
 * <ul>
 *     <li>{@link #CONTINUE} indicates that the traversal should go on,</li>
 *     <li>{@link #TERMINATE_PATH} indicates that the current traversal path should stop,</li>
 *     <li>{@link #TERMINATE_TRAVERSER} indicates that all the traversal paths should stop.</li>
 * </ul>
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public enum TraverseResult {
    /** Indicates that traversal should continue */
    CONTINUE,

    /** Indicates that traversal should terminate on current path */
    TERMINATE_PATH,

    /** Indicates that traversal should break, i.e., terminate on all paths */
    TERMINATE_TRAVERSER
}
