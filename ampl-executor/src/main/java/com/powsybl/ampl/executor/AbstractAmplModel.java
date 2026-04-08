/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.executor;

import com.powsybl.ampl.converter.OutputFileFormat;

import static com.powsybl.ampl.converter.AmplConstants.DEFAULT_VARIANT_INDEX;

/**
 * @author Nicolas Pierre {@literal <nicolas.pierre@artelys.com>}
 */
public abstract class AbstractAmplModel implements AmplModel {

    public int getVariant() {
        return DEFAULT_VARIANT_INDEX;
    }

    public String getNetworkDataPrefix() {
        return "ampl";
    }

    public OutputFileFormat getOutputFormat() {
        return OutputFileFormat.getDefault();
    }

}
