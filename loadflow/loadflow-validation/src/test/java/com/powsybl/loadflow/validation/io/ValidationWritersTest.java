/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation.io;

import com.google.common.collect.Sets;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.validation.ValidationConfig;
import com.powsybl.loadflow.validation.ValidationOutputWriter;
import com.powsybl.loadflow.validation.ValidationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class ValidationWritersTest {

    private final ValidationConfig config = new ValidationConfig(ValidationConfig.THRESHOLD_DEFAULT, ValidationConfig.VERBOSE_DEFAULT, "LoadFlowMock",
                                                                 ValidationConfig.TABLE_FORMATTER_FACTORY_DEFAULT, ValidationConfig.EPSILON_X_DEFAULT,
                                                                 ValidationConfig.APPLY_REACTANCE_CORRECTION_DEFAULT, ValidationOutputWriter.CSV_MULTILINE,
                                                                 new LoadFlowParameters(), ValidationConfig.OK_MISSING_VALUES_DEFAULT,
                                                                 ValidationConfig.NO_REQUIREMENT_IF_REACTIVE_BOUND_INVERSION_DEFAULT, ValidationConfig.COMPARE_RESULTS_DEFAULT,
                                                                 ValidationConfig.CHECK_MAIN_COMPONENT_ONLY_DEFAULT, ValidationConfig.NO_REQUIREMENT_IF_SETPOINT_OUTSIDE_POWERS_BOUNDS);
    private final Set<ValidationType> usedValidationTypes = Sets.immutableEnumSet(ValidationType.BUSES, ValidationType.FLOWS, ValidationType.GENERATORS);
    private final Set<ValidationType> unusedValidationTypes = Sets.immutableEnumSet(ValidationType.SHUNTS, ValidationType.SVCS, ValidationType.TWTS);

    private FileSystem fileSystem;
    private ValidationWriters validationWriters;

    @BeforeEach
    void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        Path folder = Files.createDirectory(fileSystem.getPath("/folder"));
        validationWriters = new ValidationWriters("network", usedValidationTypes, folder, config);
    }

    @Test
    void getWriter() {
        usedValidationTypes.forEach(type -> {
            assertNotNull(validationWriters.getWriter(type));
        });
        unusedValidationTypes.forEach(type -> {
            assertNull(validationWriters.getWriter(type));
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        validationWriters.close();
        fileSystem.close();
    }

}
