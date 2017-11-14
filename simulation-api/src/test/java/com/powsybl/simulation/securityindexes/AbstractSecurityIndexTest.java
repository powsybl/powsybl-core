/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation.securityindexes;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public abstract class AbstractSecurityIndexTest extends AbstractConverterTest {

    protected abstract AbstractSecurityIndex create();

    protected static List<SecurityIndex> readSecurityIndexes(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            return SecurityIndexParser.fromXml("contingency", reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected static void write(AbstractSecurityIndex securityIndex, Path path) {
        try (Writer writer = Files.newBufferedWriter(path)) {
            IndentingXMLStreamWriter xmlWriter = new IndentingXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
            xmlWriter.setIndent("    ");
            securityIndex.toXml(xmlWriter);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
