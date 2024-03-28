/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.triplestore.impl.rdf4j.test;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.impl.rdf4j.TripleStoreRDF4J;
import org.eclipse.rdf4j.common.transaction.IsolationLevels;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import javax.xml.transform.Source;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class PowsyblWriterSequenceFixTest {

    private final PropertyBags objects = new PropertyBags();
    private final List<String> objectProperties = Arrays.asList("id", "property1", "property2");
    private final String namespace = "http://test/";
    private final String contextName = "context1";
    private final String qualifiedContextName = "http://test/" + contextName;
    private final String objectType = "http://test/type1";
    private final String expected = "/fix-powsybl-writer-objects.xml";

    @BeforeEach
    void setUp() {
        PropertyBag object = new PropertyBag(objectProperties, true);
        object.put("id", "object1");
        object.put("property1", "object1-property1-value");
        object.put("property2", "object1-property2-value");
        objects.add(object);

        object = new PropertyBag(objectProperties, true);
        object.put("id", "object2");
        object.put("property1", "object2-property1-value");
        object.put("property2", "object2-property2-value");
        objects.add(object);
    }

    // The statements to be written by PowsyblWriter are stored in a Model,
    // Model extends from a Set<Statement>,
    // so no particular iteration order is guaranteed

    // PowsyblWriter tries to group together all the predicates of the same subject,
    // it needs to handle the statements in a particular order:
    // all statements of same subject received in sequence,
    // the first one being an rdf:type predicate that will be used to reify

    // When we use Rio.write (flag writeBySubject == false),
    // the statements will be sent to PowsyblWriters with the default iterator,
    // if they have been inserted in an order that does not match the expected one,
    // we will end up with an RDFHandlerException

    // When we force TripleStoreRDF4J to write by subject
    // (flag writeBySubject == true),
    // it does not matter in which order the statements
    // have been added to the triple store,
    // all attempts to write them to XML should be successful

    @Test
    void testRioWriteWithBadAddSequence() {
        assertThrows(RDFHandlerException.class, () -> test(false, this::addPropertiesObjects));
    }

    @Test
    void testRioWriteWithProperObjectSequenceBadTypeSequence() {
        assertThrows(RDFHandlerException.class, () -> test(false, this::addObjectPropertiesType));
    }

    @Test
    void testRioWriteWithProperAddSequence() {
        test(false, this::addObjectTypeProperties);
    }

    @Test
    void testRioWriteOverrideWithBadAddSequence() {
        test(true, this::addPropertiesObjects);
    }

    @Test
    void testRioWriteOverrideWithProperObjectSequenceBadTypeSequence() {
        test(true, this::addObjectPropertiesType);
    }

    private void test(boolean writeBySubject, BiConsumer<RepositoryConnection, Map<PropertyBag, IRI>> adder) {
        TripleStoreRDF4J ts = new TripleStoreRDF4J();
        ts.setWriteBySubject(writeBySubject);
        addStatements(ts, adder);
        writeAndCompareWithExpected(ts);
    }

    private void writeAndCompareWithExpected(TripleStoreRDF4J ts) {
        DataSource ds = new MemDataSource();
        ts.write(ds);

        try (InputStream is = ds.newInputStream(contextName)) {
            compareXml(getClass().getResourceAsStream(expected), is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void compareXml(InputStream expected, InputStream actual) {
        Source sexpected = Input.fromStream(expected).build();
        Source sactual = Input.fromStream(actual).build();
        Diff myDiff = DiffBuilder
            .compare(sexpected)
            .withTest(sactual)
            .ignoreWhitespace()
            .ignoreComments()
            .build();
        boolean hasDiff = myDiff.hasDifferences();
        if (hasDiff) {
            LOG.error(myDiff.toString());
        }
        assertFalse(hasDiff);
    }

    private void addStatements(TripleStoreRDF4J ts, BiConsumer<RepositoryConnection, Map<PropertyBag, IRI>> adder) {
        try (RepositoryConnection cnx = ts.getRepository().getConnection()) {
            cnx.setIsolationLevel(IsolationLevels.NONE);
            Map<PropertyBag, IRI> objectSubject = new HashMap<>();
            adder.accept(cnx, objectSubject);
        }
    }

    private void addObjectPropertiesType(RepositoryConnection cnx, Map<PropertyBag, IRI> objectSubject) {
        objects.forEach(object -> {
            objectProperties.forEach(property -> {
                addStatement(cnx, property, object, objectSubject);
            });
            addObjectTypeStatement(
                cnx,
                subject(cnx, object, objectSubject),
                objectType);
        });
    }

    private void addObjectTypeProperties(RepositoryConnection cnx, Map<PropertyBag, IRI> objectSubject) {
        objects.forEach(object -> {
            addObjectTypeStatement(
                cnx,
                subject(cnx, object, objectSubject),
                objectType);
            objectProperties.forEach(property -> {
                addStatement(cnx, property, object, objectSubject);
            });
        });
    }

    private void addPropertiesObjects(RepositoryConnection cnx, Map<PropertyBag, IRI> objectSubject) {
        objects.forEach(object -> addObjectTypeStatement(
            cnx,
            subject(cnx, object, objectSubject),
            objectType));
        objectProperties.forEach(property -> {
            objects.forEach(object -> {
                addStatement(cnx, property, object, objectSubject);
            });
        });
    }

    private void addStatement(
        RepositoryConnection cnx,
        String property,
        PropertyBag object,
        Map<PropertyBag, IRI> objectSubject) {

        IRI predicate = cnx.getValueFactory().createIRI(objectType + "." + property);
        Literal value = cnx.getValueFactory().createLiteral(object.get(property));
        Statement st = cnx.getValueFactory().createStatement(
            subject(cnx, object, objectSubject),
            predicate,
            value);
        Resource context = cnx.getValueFactory().createIRI(qualifiedContextName);
        cnx.add(st, context);
    }

    private void addObjectTypeStatement(RepositoryConnection cnx, IRI subject, String objectType) {
        IRI objectTypeIRI = cnx.getValueFactory().createIRI(objectType);
        Statement subjectTypeStatement = cnx.getValueFactory().createStatement(
            subject,
            RDF.TYPE,
            objectTypeIRI);
        Resource context = cnx.getValueFactory().createIRI(qualifiedContextName);
        cnx.add(subjectTypeStatement, context);
    }

    private IRI subject(
        RepositoryConnection cnx,
        PropertyBag object,
        Map<PropertyBag, IRI> objectSubject) {
        return objectSubject.computeIfAbsent(
            object,
            o -> cnx.getValueFactory().createIRI(namespace + o.get("id")));
    }

    private static final Logger LOG = LoggerFactory.getLogger(PowsyblWriterSequenceFixTest.class);
}
