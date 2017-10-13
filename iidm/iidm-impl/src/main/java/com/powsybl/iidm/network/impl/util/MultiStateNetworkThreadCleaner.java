/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.util;

import com.google.auto.service.AutoService;
import com.powsybl.commons.concurrent.CleanableExecutors;
import com.powsybl.iidm.network.impl.ThreadLocalMultiStateContext;

/**
 * Thread cleaner that reset the current network state.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(CleanableExecutors.ThreadCleaner.class)
public class MultiStateNetworkThreadCleaner implements CleanableExecutors.ThreadCleaner {

    @Override
    public void clean() {
        ThreadLocalMultiStateContext.INSTANCE.reset();
    }
}
