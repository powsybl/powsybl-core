/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface Traverser {

    /**
     * Called for each traversal step
     * @param v1 the node the traversal comes from
     * @param e the edge encountered
     * @param v2 the node the traversal will go to, if the returned TraverseResult is {@link TraverseResult#CONTINUE}
     * @return {@link TraverseResult#CONTINUE} to continue traversal, {@link TraverseResult#TERMINATE_PATH} to stop
     * the current traversal path, {@link TraverseResult#TERMINATE_TRAVERSER} to stop all the traversal paths
     */
    TraverseResult traverse(int v1, int e, int v2);
}
