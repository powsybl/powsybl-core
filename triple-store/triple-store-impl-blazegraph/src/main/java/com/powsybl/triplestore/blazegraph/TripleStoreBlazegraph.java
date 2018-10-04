package com.powsybl.triplestore.blazegraph;

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
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResults;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.algebra.evaluation.function.rdfterm.UUID;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bigdata.journal.Options;
import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import com.bigdata.rdf.store.AbstractTripleStore;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.triplestore.AbstractPowsyblTripleStore;
import com.powsybl.triplestore.PropertyBag;
import com.powsybl.triplestore.PropertyBags;
import com.powsybl.triplestore.TripleStoreException;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TripleStoreBlazegraph extends AbstractPowsyblTripleStore {

    public TripleStoreBlazegraph() {
        final Properties props = new Properties();
        props.put(Options.BUFFER_MODE, "MemStore");
        props.put(AbstractTripleStore.Options.QUADS_MODE, "true");
        props.put(BigdataSail.Options.TRUTH_MAINTENANCE, "false");

        // Quiet
        System.getProperties().setProperty("com.bigdata.Banner.quiet", "true");
        System.getProperties().setProperty("com.bigdata.util.config.LogUtil.quiet", "true");

        sail = new BigdataSail(props); // instantiate a sail
        repo = new BigdataSailRepository(sail); // create a Sesame repository

        try {
            repo.initialize();
        } catch (RepositoryException x) {
            LOG.error("Repository could not be created {}", x.getMessage());
        }
    }

    @Override
    public void read(String base, String name, InputStream is) {
        RepositoryConnection cnx = null;
        try {
            cnx = repo.getConnection();
            cnx.begin();
            Resource context = contextFromName(cnx, name);
            read(base, name, is, cnx, context);
            cnx.commit();
            setNamespaces(cnx, base);
        } catch (RepositoryException x) {
            throw new TripleStoreException(String.format("Reading. Repo problem %s %s", name, base), x);
        } finally {
            if (cnx != null) {
                try {
                    cnx.close();
                } catch (RepositoryException x) {
                    LOG.error("Reading. Closing repo connection {} {}", name, base);
                }
            }
        }
    }

    private void read(String base, String name, InputStream is, RepositoryConnection cnx, Resource context)
            throws RepositoryException {
        try {
            cnx.add(is, base, formatFromName(name), context);
        } catch (IOException x) {
            LOG.error("Reading. IO problem {}", x.getMessage());
        } catch (RDFParseException x) {
            LOG.error("Reading. RDF parsing problem {}", x.getMessage());
        }
    }

    RDFFormat formatFromName(String name) {
        if (name.endsWith(".ttl")) {
            return RDFFormat.TURTLE;
        } else if (name.endsWith(".xml")) {
            return RDFFormat.RDFXML;
        }
        return RDFFormat.RDFXML;
    }

    @Override
    public void write(DataSource ds) {
        RepositoryConnection conn = null;
        try {
            conn = repo.getConnection();
            RepositoryResult<Resource> contexts = conn.getContextIDs();
            while (contexts.hasNext()) {
                Resource context = contexts.next();
                LOG.info("Writing context {}", context);

                RepositoryResult<Statement> statements = conn.getStatements(null, null, null, true, context);
                Model model = new LinkedHashModel();
                QueryResults.addAll(statements, model);
                setNamespaces(model, conn);

                String outname = context.toString();
                write(model, outputStream(ds, outname));
            }
        } catch (RepositoryException x) {
            throw new TripleStoreException(String.format("Writing on %s", ds), x);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (RepositoryException x) {
                    LOG.error("Writing on {}. Closing repository connection", ds);
                }
            }
        }
    }

    @Override
    public void dump(PrintStream out) {
        out.println("dump CGMES Blazegraph model.");
        RepositoryConnection conn;
        try {
            conn = repo.getConnection();
            try {
                RepositoryResult<Resource> ctxs = conn.getContextIDs();
                while (ctxs.hasNext()) {
                    Resource ctx = ctxs.next();
                    int size = statementsCount(conn, ctx);
                    out.println("DUMP    " + ctx + " : " + size);
                    if (DUMP_ALL_STATEMENTS) {
                        RepositoryResult<Statement> statements = conn.getStatements(null, null, null, true, ctx);
                        while (statements.hasNext()) {
                            Statement statement = statements.next();
                            out.println("DUMP        " + statement.getSubject() + " " + statement.getPredicate() + " "
                                    + statement.getObject());
                        }
                    }
                }
            } finally {
                conn.close();
            }
        } catch (RepositoryException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void clear(String name) {
        RepositoryConnection cnx = null;
        try {
            cnx = repo.getConnection();
            Resource context = cnx.getValueFactory().createURI(name);
            cnx.clear(context);
        } catch (RepositoryException x) {
            LOG.error(x.getMessage());
        } finally {
            if (cnx != null) {
                try {
                    cnx.close();
                } catch (RepositoryException x) {
                    LOG.error(x.getMessage());
                }
            }
        }
    }

    @Override
    public PropertyBags query(String query) {
        RepositoryConnection cnx = null;
        try {
            cnx = repo.getConnection();
            return query(cnx, adjustedQuery(query));
        } catch (RepositoryException x) {
            LOG.error(x.getMessage());
            return null;
        } finally {
            if (cnx != null) {
                try {
                    cnx.close();
                } catch (RepositoryException x) {
                    LOG.error(x.getMessage());
                }
            }
        }
    }

    @Override
    public void add(String graph, String objType, PropertyBags statements) {
        RepositoryConnection cnx = null;
        try {
            cnx = repo.getConnection();
            addStatements(cnx, graph, objType, statements);

        } catch (RepositoryException x) {
            throw new TripleStoreException(String.format("Adding statements for graph %s", graph), x);
        } finally {
            if (cnx != null) {
                try {
                    cnx.close();
                } catch (RepositoryException x) {
                    LOG.error("Adding statements for graph {}. Closing repository connection", graph);
                }
            }
        }
    }

    private void addStatements(RepositoryConnection cnx, String graph, String objType, PropertyBags statements)
            throws RepositoryException {

        cnx.begin();

        String name = null;
        RepositoryResult<Resource> ctxs = cnx.getContextIDs();
        while (ctxs.hasNext()) {
            String ctx = ctxs.next().stringValue();
            if (ctx.contains("EQ")) {
                name = ctx.replace("EQ", graph);
                break;
            }
        }

        Resource context = cnx.getValueFactory().createURI(name);
        Iterator<PropertyBag> itStatements = statements.iterator();
        while (itStatements.hasNext()) {
            PropertyBag statement = itStatements.next();
            createStatements(cnx, objType, statement, context);
        }

        cnx.commit();
    }

    private void createStatements(RepositoryConnection cnx, String objType, PropertyBag statement, Resource context) {

        try {
            UUID uuid = new UUID();
            URI resource = uuid.evaluate(cnx.getValueFactory());
            URI parentPredicate = RDF.TYPE;
            Literal parentObject = cnx.getValueFactory().createLiteral(objType);
            Statement parentSt = cnx.getValueFactory().createStatement(resource, parentPredicate, parentObject);
            cnx.add(parentSt, context);

            List<String> names = statement.propertyNames();
            Iterator<String> it = names.iterator();
            while (it.hasNext()) {
                String name = it.next();
                URI predicate = cnx.getValueFactory().createURI(objType + "." + name);
                Statement st;
                if (statement.isResource(name)) {
                    String namespace = cnx.getNamespace(statement.namespacePrefix(name));
                    URI object = cnx.getValueFactory().createURI(namespace + statement.get(name));
                    st = cnx.getValueFactory().createStatement(resource, predicate, object);
                } else {
                    Literal object = cnx.getValueFactory().createLiteral(statement.get(name));
                    st = cnx.getValueFactory().createStatement(resource, predicate, object);
                }
                cnx.add(st, context);
            }
        } catch (Exception x) {
            throw new TripleStoreException(String.format("Creating statements for object type %s", objType), x);
        }
    }

    private PropertyBags query(RepositoryConnection cnx, String query) throws RepositoryException {
        PropertyBags results = new PropertyBags();
        try {
            final TupleQuery tupleQuery = cnx.prepareTupleQuery(QueryLanguage.SPARQL, query);
            tupleQuery.setIncludeInferred(false);
            TupleQueryResult r = tupleQuery.evaluate();
            try {
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
            } finally {
                r.close();
            }
        } catch (MalformedQueryException | QueryEvaluationException x) {
            throw new TripleStoreException(String.format("Query [%s]", query), x);
        }
        return results;
    }

    private int statementsCount(RepositoryConnection conn, Resource ctx) {
        int counter = 0;
        try {
            RepositoryResult<Statement> statements = conn.getStatements(null, null, null, true, ctx);
            while (statements.hasNext()) {
                counter++;
                statements.next();
            }
        } catch (RepositoryException e) {
            LOG.error(e.getMessage());
        }
        return counter;
    }

    private void write(Model statements, OutputStream out) {
        try (PrintStream pout = new PrintStream(out)) {
            RDFWriter w = new RDFXMLPrettyWriter(pout);
            Rio.write(statements, w);
        } catch (Exception x) {
            throw new TripleStoreException("Writing model statements", x);
        }
    }

    private void setNamespaces(Model m, RepositoryConnection cnx) throws RepositoryException {
        // FIXME we should not set here this namespace
        m.setNamespace("cim", cnx.getNamespace("cim"));
        m.setNamespace("data", cnx.getNamespace("data"));
    }

    private void setNamespaces(RepositoryConnection cnx, String base) throws RepositoryException {
        // FIXME we should not set here this namespace
        cnx.setNamespace("cim", "http://iec.ch/TC57/2009/CIM-schema-cim14#");
        cnx.setNamespace("data", base + "#");
    }

    private Resource contextFromName(RepositoryConnection conn, String filename) {
        return conn.getValueFactory().createURI(namespaceForContexts(), filename);
    }

    private final Repository repo;
    private final BigdataSail sail;

    private static final Logger LOG = LoggerFactory.getLogger(TripleStoreBlazegraph.class);

    private static final boolean DUMP_ALL_STATEMENTS = false;
}
