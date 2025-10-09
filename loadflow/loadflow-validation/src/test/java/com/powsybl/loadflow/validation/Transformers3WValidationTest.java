/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.validation.io.ValidationWriter;
import com.powsybl.loadflow.validation.util.TwtTestData;
import org.apache.commons.io.output.NullWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class Transformers3WValidationTest extends AbstractValidationTest {

    private TwtTestData twtValidationData;

    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
        twtValidationData = new TwtTestData();
    }

    @Test
    void checkTwts() {
        assertFalse(Transformers3WValidation.INSTANCE.checkTransformer(twtValidationData.get3WTransformer(), strictConfig, NullWriter.INSTANCE));
        strictConfig.setThreshold(.3);
        assertTrue(Transformers3WValidation.INSTANCE.checkTransformer(twtValidationData.get3WTransformer(), strictConfig, NullWriter.INSTANCE));
        // check NaN values
        twtValidationData.setNanLeg1P();
        assertFalse(Transformers3WValidation.INSTANCE.checkTransformer(twtValidationData.get3WTransformer(), strictConfig, NullWriter.INSTANCE));
        strictConfig.setOkMissingValues(true);
        assertTrue(Transformers3WValidation.INSTANCE.checkTransformer(twtValidationData.get3WTransformer(), strictConfig, NullWriter.INSTANCE));
    }

    @Test
    void checkNetworkTwts() throws IOException {
        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getId()).thenReturn("network");
        Mockito.when(network.getThreeWindingsTransformerStream()).thenAnswer(dummy -> Stream.of(twtValidationData.get3WTransformer()));

        assertFalse(Transformers3WValidation.INSTANCE.checkTransformers(network, strictConfig, data));
        assertFalse(ValidationType.TWTS3W.check(network, strictConfig, tmpDir));

        ValidationWriter validationWriter = ValidationUtils.createValidationWriter(network.getId(), strictConfig, NullWriter.INSTANCE, ValidationType.TWTS);
        assertFalse(ValidationType.TWTS3W.check(network, strictConfig, validationWriter));

        strictConfig.setThreshold(.3);
        assertTrue(Transformers3WValidation.INSTANCE.checkTransformers(network, strictConfig, NullWriter.INSTANCE));
    }

}
