package com.powsybl.triplestore.rdf4j;

/*
 * #%L
 * Triple store abstraction
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.algebra.evaluation.function.rdfterm.UUID;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.triplestore.AbstractPowsyblTripleStore;
import com.powsybl.triplestore.PropertyBag;
import com.powsybl.triplestore.PropertyBags;
import com.powsybl.triplestore.TripleStoreException;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TripleStoreRDF4J extends AbstractPowsyblTripleStore {

    public TripleStoreRDF4J() {
        repo = new SailRepository(new MemoryStore());
        repo.initialize();
    }

    @Override
    public void read(String base, String name, InputStream is) {
        try (RepositoryConnection conn = repo.getConnection()) {
            conn.setIsolationLevel(IsolationLevels.NONE);
            Resource context = contextFromFile(conn, name);
            // We add data with a context (graph) to keep the source of information
            // When we write we want to keep data split by graph
            conn.add(is, base, formatFromFile(name), context);
            setNamespaces(conn, base);
        } catch (IOException x) {
            throw new TripleStoreException(String.format("Reading %s %s", base, name), x);
        }
    }

    RDFFormat formatFromFile(String filename) {
        if (filename.endsWith(".ttl")) {
            return RDFFormat.TURTLE;
        } else if (filename.endsWith(".xml")) {
            return RDFFormat.RDFXML;
        }
        return RDFFormat.RDFXML;
    }

    @Override
    public void write(DataSource ds) {
        try (RepositoryConnection conn = repo.getConnection()) {
            RepositoryResult<Resource> contexts = conn.getContextIDs();
            while (contexts.hasNext()) {
                Resource context = contexts.next();
                LOGGER.info("Writing context {}", context);

                RepositoryResult<Statement> statements;
                statements = conn.getStatements(null, null, null, context);
                Model model = QueryResults.asModel(statements);
                setNamespaces(model, conn);

                String outname = context.toString();
                write(model, outputStream(ds, outname));
            }
        }
    }

    @Override
    public void dump(PrintStream out) {
        out.println("dump CGMES RDF4J model. Graph names and sizes");
        try (RepositoryConnection conn = repo.getConnection()) {
            RepositoryResult<Resource> ctxs = conn.getContextIDs();
            while (ctxs.hasNext()) {
                Resource ctx = ctxs.next();
                int size = statementsCount(conn, ctx);
                out.println("    " + ctx + " : " + size);
            }
        }
    }

    @Override
    public void clear(String name) {
        try (RepositoryConnection conn = repo.getConnection()) {
            Resource context = conn.getValueFactory().createIRI(name);
            conn.clear(context);
        }
    }

    @Override
    public PropertyBags query(String query) {
        String query1 = adjustedQuery(query);
        PropertyBags results = new PropertyBags();
        try (RepositoryConnection conn = repo.getConnection()) {
            // Default language is SPARQL
            TupleQuery q = conn.prepareTupleQuery(query1);
            // Duplicated triplets are returned in queries
            // when an object is defined in a file and referrenced in another (rdf:ID and
            // rdf:about)
            // and data has been added to repository with contexts
            // and we query without using explicit GRAPH clauses
            // This means that we have to filter distinct results
            try (TupleQueryResult r = QueryResults.distinctResults(q.evaluate())) {
                List<String> names = r.getBindingNames();
                while (r.hasNext()) {
                    BindingSet s = r.next();
                    PropertyBag result = new PropertyBag(names);

                    names.stream().forEach(name -> {
                        if (s.hasBinding(name)) {
                            String value = s.getBinding(name).getValue().stringValue();
                            result.put(name, value);
                        }
                    });
                    if (result.size() > 0) {
                        results.add(result);
                    }
                }
            }
        }
        return results;
    }

    @Override
    public void add(String graph, String objType, PropertyBags statements) {
        try (RepositoryConnection conn = repo.getConnection()) {
            conn.setIsolationLevel(IsolationLevels.NONE);

            String name = null;
            RepositoryResult<Resource> ctxs = conn.getContextIDs();
            while (ctxs.hasNext()) {
                String ctx = ctxs.next().stringValue();
                if (ctx.contains("EQ")) {
                    name = ctx.replace("EQ", graph);
                    break;
                }
            }

            Resource context = conn.getValueFactory().createIRI(name);

            statements.forEach(statement -> createStatements(conn, objType, statement, context));
        }
    }

    private void createStatements(RepositoryConnection cnx, String objType, PropertyBag statement, Resource context) {

        UUID uuid = new UUID();
        IRI resource = uuid.evaluate(cnx.getValueFactory());
        IRI parentPredicate = RDF.TYPE;
        IRI parentObject = cnx.getValueFactory().createIRI(objType);
        Statement parentSt = cnx.getValueFactory().createStatement(resource, parentPredicate, parentObject);
        cnx.add(parentSt, context);

        List<String> names = statement.propertyNames();
        names.stream().forEach(name -> {
            IRI predicate = cnx.getValueFactory().createIRI(objType + "." + name);
            Statement st;
            if (statement.isResource(name)) {
                String namespace = cnx.getNamespace(statement.namespacePrefix(name));
                IRI object = cnx.getValueFactory().createIRI(namespace, statement.get(name));
                st = cnx.getValueFactory().createStatement(resource, predicate, object);
            } else {
                Literal object = cnx.getValueFactory().createLiteral(statement.get(name));
                st = cnx.getValueFactory().createStatement(resource, predicate, object);
            }
            cnx.add(st, context);
        });
    }

    private void write(Model m, OutputStream out) {
        try (PrintStream pout = new PrintStream(out)) {
            RDFWriter w = new PowsyblWriter(out);
            w.getWriterConfig().set(BasicWriterSettings.PRETTY_PRINT, true);
            Rio.write(m, w);
        }
    }

    private int statementsCount(RepositoryConnection conn, Resource ctx) {
        RepositoryResult<Statement> statements = conn.getStatements(null, null, null, ctx);
        int counter = 0;
        while (statements.hasNext()) {
            counter++;
            statements.next();
        }
        return counter;
    }

    private void setNamespaces(Model m, RepositoryConnection conn) {
        // FIXME we should not set here this namespace
        m.setNamespace("cim", conn.getNamespace("cim"));
        m.setNamespace("data", conn.getNamespace("data"));
    }

    private void setNamespaces(RepositoryConnection cnx, String base) {
        // FIXME we should not set here this namespace
        cnx.setNamespace("cim", "http://iec.ch/TC57/2009/CIM-schema-cim14#");
        cnx.setNamespace("data", base + "#");
    }

    private Resource contextFromFile(RepositoryConnection conn, String filename) {
        return conn.getValueFactory().createIRI(namespaceForContexts(), filename);
    }

    private final Repository repo;

    private static final Logger LOGGER = LoggerFactory.getLogger(TripleStoreRDF4J.class);
}
