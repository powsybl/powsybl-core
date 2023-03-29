/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.executor;

import com.powsybl.ampl.converter.AmplSubset;
import com.powsybl.commons.util.StringToIntMapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Nicolas Pierre <nicolas.pierre@artelys.com>
 */
public class SimpleAmplParameters implements AmplParameters {

    @Override
    public Collection<AmplInputFile> getInputParameters() {
        return List.of(new AmplInputFile() {
            @Override
            public String getFileName() {
                return "simple_input.txt";
            }

            @Override
            public InputStream getParameterFileAsStream(StringToIntMapper<AmplSubset> networkAmplMapper) {
                return new ByteArrayInputStream("some_content".getBytes());
            }
        });
    }

    @Override
    public Collection<AmplOutputFile> getOutputParameters(boolean hasConverged) {
        return Collections.emptyList();
    }
}
