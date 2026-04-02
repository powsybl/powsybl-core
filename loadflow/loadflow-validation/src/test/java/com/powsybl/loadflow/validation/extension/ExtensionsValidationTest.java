/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation.extension;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.loadflow.validation.ValidationConfig;
import com.powsybl.tools.ToolRunningContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 *
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
class ExtensionsValidationTest {

    ExtensionsValidation extensionsValidation = new ExtensionsValidation();
    private final Network network = EurostagTutorialExample1Factory.create();

    @Test
    void getExtensionsShouldSucceed() {
        assertEquals(1, ExtensionsValidation.getExtensions().size());
    }

    @Test
    void getExtensionsNamesShouldReturnExistingExtension() {
        assertEquals(List.of("extensionValidationMock1"), ExtensionsValidation.getExtensionsNames());
    }

    @Test
    void getExtensionShouldReturnExistingExtension() {
        assertTrue(ExtensionsValidation.getExtension("extensionValidationMock1").isPresent());
        assertEquals("extensionValidationMock1", ExtensionsValidation.getExtension("extensionValidationMock1").get().getName());
    }

    @Test
    void runExtensionValidationsShouldSucceed() {
        //Given
        ValidationConfig config = ValidationConfig.load();
        ToolRunningContext context = new ToolRunningContext(mock(PrintStream.class), mock(PrintStream.class), mock(FileSystem.class), mock(ComputationManager.class), mock(ComputationManager.class));

        ExtensionValidation extension1 = mock(ExtensionValidation.class);
        ExtensionValidation extension2 = mock(ExtensionValidation.class);

        when(extension1.getName()).thenReturn("ExtensionName1");
        when(extension1.check(any(), any())).thenReturn(true);

        when(extension2.getName()).thenReturn("ExtensionName2");
        when(extension2.check(any(), any())).thenReturn(false);

        try (MockedStatic<ExtensionsValidation> mocked = mockStatic(ExtensionsValidation.class)) {
            mocked.when(ExtensionsValidation::getExtensions).thenReturn(List.of(extension1, extension2));
            // When
            extensionsValidation.runExtensionValidations(network, config, context);
            // Then
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(context.getOutputStream(), times(2)).println(captor.capture());
            List<String> messages = captor.getAllValues();
            assertTrue(messages.get(0).contains("validation type: Extension/ExtensionName1 - result: success"));
            assertTrue(messages.get(1).contains("validation type: Extension/ExtensionName2 - result: fail"));
            // check method was called on extensions
            verify(extension1, times(1)).check(network, config);
            verify(extension2, times(1)).check(network, config);
        }
    }
}
