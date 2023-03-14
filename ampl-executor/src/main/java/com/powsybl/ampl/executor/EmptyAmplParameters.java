/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.executor;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Nicolas Pierre <nicolas.pierre@artelys.com>
 */
public class EmptyAmplParameters implements AmplParameters {

    @Override
    public Collection<AmplInputFile> getInputParameters() {
        return Collections.emptyList();
    }

    @Override
    public Collection<AmplOutputFile> getOutputParameters() {
        return Collections.emptyList();
    }
}
