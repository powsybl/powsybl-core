/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.export;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

/**
 *
 * @author Jérémy LABOUS <jlabous@silicom.fr>
 */
public interface ExportPostProcessor {

    String getName();

    void process(Network network, String format, Object nativeModel, ComputationManager computationManager);

}
