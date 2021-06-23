/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.commons.json.JsonUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SensitivityFactoryJsonRecorder implements SensitivityFactorReader {

    private final SensitivityFactorReader delegate;

    private final Path jsonFile;

    public SensitivityFactoryJsonRecorder(SensitivityFactorReader delegate, Path jsonFile) {
        this.delegate = Objects.requireNonNull(delegate);
        this.jsonFile = Objects.requireNonNull(jsonFile);
    }

    @Override
    public void read(Handler handler) {
        Objects.requireNonNull(handler);
        JsonUtil.writeJson(jsonFile, jsonGenerator -> {
            try {
                jsonGenerator.writeStartArray();

                delegate.read((factorContext, functionType, functionId, variableType, variableId, variableSet, contingencyContext) -> {
                    SensitivityFactor.writeJson(jsonGenerator, functionType, functionId, variableType, variableId, variableSet, contingencyContext);
                    handler.onFactor(factorContext, functionType, functionId, variableType, variableId, variableSet, contingencyContext);
                });

                jsonGenerator.writeEndArray();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
