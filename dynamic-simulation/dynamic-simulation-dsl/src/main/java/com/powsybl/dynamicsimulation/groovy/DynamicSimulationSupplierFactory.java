/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.groovy;

import com.powsybl.commons.PowsyblException;
import com.powsybl.dynamicsimulation.CurvesSupplier;
import com.powsybl.dynamicsimulation.DynamicModelsSupplier;
import com.powsybl.dynamicsimulation.EventModelsSupplier;
import org.apache.commons.io.FilenameUtils;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public final class DynamicSimulationSupplierFactory {

    private static final String GROOVY_EXTENSION = "groovy";

    private DynamicSimulationSupplierFactory() {
    }

    public static DynamicModelsSupplier createDynamicModelsSupplier(Path path, String providerName) {
        String extension = FilenameUtils.getExtension(path.toString());
        if (extension.equals(GROOVY_EXTENSION)) {
            return new GroovyDynamicModelsSupplier(path, GroovyExtension.find(DynamicModelGroovyExtension.class, providerName));
        } else {
            throw new PowsyblException("Unsupported dynamic model format: " + extension);
        }
    }

    public static DynamicModelsSupplier createDynamicModelsSupplier(InputStream is, String providerName) {
        return new GroovyDynamicModelsSupplier(is, GroovyExtension.find(DynamicModelGroovyExtension.class, providerName));
    }

    public static EventModelsSupplier createEventModelsSupplier(Path path, String providerName) {
        String extension = FilenameUtils.getExtension(path.toString());
        if (extension.equals(GROOVY_EXTENSION)) {
            return new GroovyEventModelsSupplier(path, GroovyExtension.find(EventModelGroovyExtension.class, providerName));
        } else {
            throw new PowsyblException("Unsupported events format: " + extension);
        }
    }

    public static EventModelsSupplier createEventModelsSupplier(InputStream is, String providerName) {
        return new GroovyEventModelsSupplier(is, GroovyExtension.find(EventModelGroovyExtension.class, providerName));
    }

    public static CurvesSupplier createCurvesSupplier(Path path, String providerName) {
        String extension = FilenameUtils.getExtension(path.toString());
        if (extension.equals(GROOVY_EXTENSION)) {
            return new GroovyCurvesSupplier(path, GroovyExtension.find(CurveGroovyExtension.class, providerName));
        } else {
            throw new PowsyblException("Unsupported curves format: " + extension);
        }
    }
}
