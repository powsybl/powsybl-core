/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.converter;

import com.google.auto.service.AutoService;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.json.SecurityAnalysisResultSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

/**
 * A SecurityAnalysisResultExporter implementation which export the result in JSON
 *
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
@AutoService(SecurityAnalysisResultExporter.class)
public class JsonSecurityAnalysisResultExporter implements SecurityAnalysisResultExporter {
    @Override
    public String getFormat() {
        return "JSON";
    }

    @Override
    public String getComment() {
        return "Export a security analysis result in JSON format";
    }

    @Override
    public void export(SecurityAnalysisResult result, Writer writer) {
        try {
            SecurityAnalysisResultSerializer.write(result, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
