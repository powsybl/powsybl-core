/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.jaxb;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.exceptions.UncheckedJaxbException;
import com.powsybl.commons.exceptions.UncheckedTransformerException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class JaxbUtil {

    private static final Supplier<TransformerFactory> TRANSFORMER_FACTORY_SUPPLIER = Suppliers.memoize(TransformerFactory::newInstance);

    private JaxbUtil() {
    }

    public static void marshallElement(JAXBContext jaxbContext, Object jaxbElement, Writer writer) {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            // output pretty printed
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(jaxbElement, writer);
        } catch (JAXBException e) {
            throw new UncheckedJaxbException(e);
        }
    }

    public static void marshallElement(Class<?> aClass, Object jaxbElement, Writer writer) {
        marshallElement(JaxbContextCache.DEFAULT.createContext(aClass), jaxbElement, writer);
    }

    public static void marshallFile(Class<?> aClass, Object jaxbElement, Path file) {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            marshallElement(aClass, jaxbElement, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void marshallElement(JAXBContext jaxbContext, Object jaxbElement, Path file) {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            marshallElement(jaxbContext, jaxbElement, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void marshallElement(Class<?> aClass, Object jaxbElement, Path file) {
        marshallElement(JaxbContextCache.DEFAULT.createContext(aClass), jaxbElement, file);
    }

    public static <U> U unmarchallReader(JAXBContext jaxbContext, Reader reader, Reader xslReader) {
        try {
            if (xslReader != null) {
                JAXBResult result = new JAXBResult(jaxbContext);
                TransformerFactory tf = TRANSFORMER_FACTORY_SUPPLIER.get();
                Transformer t = tf.newTransformer(new StreamSource(xslReader));
                t.transform(new StreamSource(reader), result);
                return (U) result.getResult();
            } else {
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                return (U) unmarshaller.unmarshal(reader);
            }
        } catch (JAXBException e) {
            throw new UncheckedJaxbException(e);
        } catch (TransformerException e) {
            throw new UncheckedTransformerException(e);
        }
    }

    public static <U> U unmarchallReader(Class<U> aClass, Reader reader, Reader xslReader) {
        return unmarchallReader(JaxbContextCache.DEFAULT.createContext(aClass), reader, xslReader);
    }

    public static <U> U unmarchallReader(Class<U> aClass, Reader reader) {
        return unmarchallReader(JaxbContextCache.DEFAULT.createContext(aClass), reader, null);
    }

    public static <U> U unmarchallFile(Class<U> aClass, Path file) {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return unmarchallReader(JaxbContextCache.DEFAULT.createContext(aClass), reader, null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
