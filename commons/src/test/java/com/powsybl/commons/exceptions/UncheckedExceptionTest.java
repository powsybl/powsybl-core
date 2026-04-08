/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.exceptions;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class UncheckedExceptionTest {

    @Test
    void classCastExceptionTest() {
        ClassCastException e = new ClassCastException();
        assertSame(e, new UncheckedClassCastExceptionException(e).getCause());
    }

    @Test
    void classCastExceptionMessageTest() {
        String message = "X of type Z cannot be converted to Y";
        UncheckedClassCastExceptionException exception = new UncheckedClassCastExceptionException(message);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void classNotFoundTest() {
        ClassNotFoundException e = new ClassNotFoundException();
        assertSame(e, new UncheckedClassNotFoundException(e).getCause());
    }

    @Test
    void illegalAccessTest() {
        IllegalAccessException e = new IllegalAccessException();
        assertSame(e, new UncheckedIllegalAccessException(e).getCause());
    }

    @Test
    void instantiationTest() {
        InstantiationException e = new InstantiationException();
        assertSame(e, new UncheckedInstantiationException(e).getCause());
    }

    @Test
    void interruptedTest() {
        InterruptedException e = new InterruptedException();
        assertSame(e, new UncheckedInterruptedException(e).getCause());
    }

    @Test
    void parserConfigurationTest() {
        ParserConfigurationException e = new ParserConfigurationException();
        assertSame(e, new UncheckedParserConfigurationException(e).getCause());
    }

    @Test
    void saxTest() {
        SAXException e = new SAXException();
        assertSame(e, new UncheckedSaxException(e).getCause());
    }

    @Test
    void transformerTest() {
        TransformerException e = new TransformerException("");
        assertSame(e, new UncheckedTransformerException(e).getCause());
    }

    @Test
    void xmlStreamTest() {
        XMLStreamException e = new XMLStreamException();
        assertSame(e, new UncheckedXmlStreamException(e).getCause());
    }

    @Test
    void uriSyntaxTest() {
        URISyntaxException e = new URISyntaxException("", "");
        assertSame(e, new UncheckedUriSyntaxException(e).getCause());
    }
}
