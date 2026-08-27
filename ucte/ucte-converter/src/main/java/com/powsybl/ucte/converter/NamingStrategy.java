/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.ucte.converter;

import com.powsybl.iidm.network.*;
import com.powsybl.ucte.network.UcteElementId;
import com.powsybl.ucte.network.UcteNodeCode;

/**
 * A naming strategy generates the UCTE codes of a network's buses and elements. Implementations must be
 * stateless: {@link #initialize(Network)} builds and returns a fresh {@link Context} for each export,
 * which is then passed to every other method, since a single {@link NamingStrategy} instance is shared
 * and reused across exports.
 *
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public interface NamingStrategy {

    Context initialize(Network network);

    String getName();

    UcteNodeCode getUcteNodeCode(Context context, String id);

    UcteNodeCode getUcteNodeCode(Context context, Bus bus);

    UcteNodeCode getUcteNodeCode(Context context, BoundaryLine boundaryLine);

    UcteElementId getUcteElementId(Context context, String id);

    UcteElementId getUcteElementId(Context context, Switch sw);

    UcteElementId getUcteElementId(Context context, Branch branch);

    UcteElementId getUcteElementId(Context context, BoundaryLine boundaryLine);

    /**
     * Per-export state of a {@link NamingStrategy}, built by {@link #initialize(Network)}. Deliberately
     * minimal: the state needed to assign UCTE codes (e.g. the codes already assigned) is specific to
     * each {@link NamingStrategy} implementation and lives on its own {@link Context} subtype, see
     * {@link AbstractNamingStrategy.Context}.
     */
    interface Context {

        Network getNetwork();
    }
}
