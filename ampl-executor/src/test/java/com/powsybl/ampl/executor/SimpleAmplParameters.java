/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.executor;

import com.powsybl.ampl.converter.AmplExportConfig;
import com.powsybl.ampl.converter.AmplSubset;
import com.powsybl.ampl.converter.version.AmplExportVersion;
import com.powsybl.commons.util.StringToIntMapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nicolas Pierre {@literal <nicolas.pierre@artelys.com>}
 */
public class SimpleAmplParameters implements AmplParameters {

    private boolean readingDone = false;

    @Override
    public Collection<AmplInputFile> getInputParameters() {
        return List.of(new AmplInputFile() {
            @Override
            public String getFileName() {
                return "simple_input.txt";
            }

            @Override
            public void write(BufferedWriter writer, StringToIntMapper<AmplSubset> networkAmplMapper) throws IOException {
                writer.write("some_content");
                writer.flush();
            }
        });
    }

    @Override
    public Collection<AmplOutputFile> getOutputParameters(boolean hasConverged) {
        if (hasConverged) {
            return List.of(new AbstractMandatoryOutputFile() {
                @Override
                public String getFileName() {
                    return "simple_output.txt";
                }

                @Override
                public void read(BufferedReader reader, StringToIntMapper<AmplSubset> networkAmplMapper) throws IOException {
                    readingDone = true;
                    assertTrue(reader.readLine()
                            .equals("This file must be encoded in UTF-8 ! éèà") && reader.readLine() == null,
                        "The content of the file is not read properly.");
                }
            });
        }
        return List.of();
    }

    public boolean isReadingDone() {
        return readingDone;
    }

    @Override
    public boolean isDebug() {
        return true;
    }

    @Override
    public String getDebugDir() {
        return null;
    }

    @Override
    public AmplExportConfig getAmplExportConfig() {
        return new AmplExportConfig(AmplExportConfig.ExportScope.ALL, false, AmplExportConfig.ExportActionType.CURATIVE, false, false, AmplExportVersion.defaultVersion(), false);
    }

}
