/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.dgs;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.TestUtil;
import com.powsybl.powerfactory.model.PowerFactoryException;
import com.powsybl.powerfactory.model.StudyCase;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class DgsDataTest extends AbstractConverterTest {

    private String toJson(StudyCase studyCase) throws IOException {
        try (StringWriter writer = new StringWriter()) {
            studyCase.writeJson(writer);
            String json = writer.toString();
            return TestUtil.normalizeLineSeparator(json);
        }
    }

    private static StudyCase loadCase(String fileName) {
        String studyName = Files.getNameWithoutExtension(fileName);
        InputStream is = Objects.requireNonNull(DgsDataTest.class.getResourceAsStream(fileName));
        DgsReader dgsReader = new DgsReader();
        return dgsReader.read(studyName, new InputStreamReader(is));
    }

    private static String loadReference(String path) {
        try {
            InputStream is = Objects.requireNonNull(DgsDataTest.class.getResourceAsStream(path));
            return TestUtil.normalizeLineSeparator(new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    public void twoBusesTest() throws IOException {
        assertTrue(test("/TwoBuses.dgs", "/TwoBuses.json"));
    }

    @Test
    public void towerTest() throws IOException {
        assertTrue(test("/Tower.dgs", "/Tower.json"));
    }

    @Test
    public void twoBusesCommaAsDecimalSeparatorTest() throws IOException {
        assertTrue(test("/TwoBusesCommaAsDecimalSeparator.dgs", "/TwoBusesCommaAsDecimalSeparator.json"));
    }

    @Test
    public void emptyMatrixTest() throws IOException {
        PowerFactoryException e = assertThrows(PowerFactoryException.class, () -> loadCase("/EmptyMatrix.dgs"));
        assertEquals("RealMatrix: Unexpected number of cols: 'GPScoords' rows: 1 cols: 1 expected cols: 2", e.getMessage());
    }

    private boolean test(String dgs, String json) throws IOException {
        StudyCase studyCase = loadCase(dgs);
        String expectedJson = loadReference(json);

        assertNotNull(studyCase);
        assertEquals(expectedJson, toJson(studyCase));

        return true;
    }
}
