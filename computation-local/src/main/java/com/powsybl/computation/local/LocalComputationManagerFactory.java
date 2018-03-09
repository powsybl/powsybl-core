/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.local;

import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.ComputationManagerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalComputationManagerFactory implements ComputationManagerFactory {

    @Override
    public ComputationManager create() {
        try {
            return new LocalComputationManager();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
