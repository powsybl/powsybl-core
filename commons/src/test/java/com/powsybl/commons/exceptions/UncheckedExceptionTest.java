/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.exceptions;

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import java.net.URISyntaxException;

import static org.junit.Assert.assertSame;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UncheckedExceptionTest {

    @Test
    public void classNotFoundTest() {
        ClassNotFoundException e = new ClassNotFoundException();
        assertSame(e, new UncheckedClassNotFoundException(e).getCause());
    }

    @Test
    public void illegalAccessTest() {
        IllegalAccessException e = new IllegalAccessException();
        assertSame(e, new UncheckedIllegalAccessException(e).getCause());
    }

    @Test
    public void instantiationTest() {
        InstantiationException e = new InstantiationException();
        assertSame(e, new UncheckedInstantiationException(e).getCause());
    }

    @Test
    public void interruptedTest() {
        InterruptedException e = new InterruptedException();
        assertSame(e, new UncheckedInterruptedException(e).getCause());
    }

    @Test
    public void jaxbTest() {
        JAXBException e = new JAXBException("");
        assertSame(e, new UncheckedJaxbException(e).getCause());
    }

    @Test
    public void parserConfigurationTest() {
        ParserConfigurationException e = new ParserConfigurationException();
        assertSame(e, new UncheckedParserConfigurationException(e).getCause());
    }

    @Test
    public void saxTest() {
        SAXException e = new SAXException();
        assertSame(e, new UncheckedSaxException(e).getCause());
    }

    @Test
    public void transformerTest() {
        TransformerException e = new TransformerException("");
        assertSame(e, new UncheckedTransformerException(e).getCause());
    }

    @Test
    public void xmlStreamTest() {
        XMLStreamException e = new XMLStreamException();
        assertSame(e, new UncheckedXmlStreamException(e).getCause());
    }

    @Test
    public void uriSyntaxTest() {
        URISyntaxException e = new URISyntaxException("", "");
        assertSame(e, new UncheckedUriSyntaxException(e).getCause());
    }
}
