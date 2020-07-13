/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.triplestore.impl.blazegraph;

import com.bigdata.journal.Options;
import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import com.bigdata.rdf.store.AbstractTripleStore;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.triplestore.api.*;
import info.aduna.iteration.Iterations;
import org.openrdf.model.*;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.*;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TripleStoreBlazegraph extends AbstractPowsyblTripleStore {

    static final String NAME = "blazegraph";

    public TripleStoreBlazegraph() {
        final Properties props = new Properties();
        props.put(Options.BUFFER_MODE, "MemStore");
        props.put(AbstractTripleStore.Options.QUADS_MODE, "true");
        props.put(BigdataSail.Options.TRUTH_MAINTENANCE, "false");

        // Quiet
        System.getProperties().setProperty("com.bigdata.Banner.quiet", "true");
        System.getProperties().setProperty("com.bigdata.util.config.LogUtil.quiet", "true");

        BigdataSail sail = new BigdataSail(props); // instantiate a sail
        repo = new BigdataSailRepository(sail); // create a Sesame repository

        try {
            repo.initialize();
        } catch (RepositoryException x) {
            LOG.error("Repository could not be created {}", x.getMessage());
        }
    }

    @Override
    public String getImplementationName() {
        return NAME;
    }

    private void closeConnection(RepositoryConnection cnx, String operation) {
        if (cnx != null) {
            try {
                cnx.close();
            } catch (RepositoryException x) {
                LOG.error("{}. Error closing repository connection: {}", operation, x.getMessage());
            }
        }
    }

    @Override
    public void read(InputStream is, String base, String name) {
        RepositoryConnection cnx = null;
        try {
            cnx = repo.getConnection();
            cnx.begin();
            Resource context = context(cnx, name);
            read(is, base, name, cnx, context);
            cnx.commit();
            addNamespaceForBase(cnx, base);
        } catch (RepositoryException x) {
            throw new TripleStoreException(String.format("Reading. Repo problem %s %s", name, base), x);
        } finally {
            closeConnection(cnx, "Reading");
        }
    }

    private static void read(InputStream is, String base, String name, RepositoryConnection cnx, Resource context)
        throws RepositoryException {
        try {
            cnx.add(is, base, guessFormatFromName(name), context);
        } catch (IOException x) {
            LOG.error("Reading. IO problem {}", x.getMessage());
        } catch (RDFParseException x) {
            LOG.error("Reading. RDF parsing problem {}", x.getMessage());
        }
    }

    private static RDFFormat guessFormatFromName(String name) {
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
                write(ds, conn, context);
            }
        } catch (RepositoryException x) {
            throw new TripleStoreException(String.format("Writing on %s", ds), x);
        } finally {
            closeConnection(conn, "Writing on " + ds);
        }
    }

    @Override
    public void write(DataSource ds, String contextName) {
        RepositoryConnection conn = null;
        try {
            conn = repo.getConnection();
            RepositoryResult<Resource> contexts = conn.getContextIDs();
            while (contexts.hasNext()) {
                Resource context = contexts.next();
                if (context.stringValue().equals(contextName)) {
                    write(ds, conn, context);
                }
            }
        } catch (RepositoryException x) {
            throw new TripleStoreException(String.format("Writing on %s", ds), x);
        } finally {
            closeConnection(conn, "Writing on " + ds);
        }
    }

    private void write(DataSource ds, RepositoryConnection conn, Resource context) throws RepositoryException {
        LOG.info("Writing context {}", context);

        RepositoryResult<Statement> statements = conn.getStatements(null, null, null, true, context);
        Model model = new LinkedHashModel();
        Iterations.addAll(statements, model);
        copyNamespacesToModel(conn, model);

        String outname = context.toString();
        write(model, outputStream(ds, outname));
    }

    @Override
    public void print(PrintStream out) {
        out.println("TripleStore based on Blazegraph");
        RepositoryConnection conn;
        try {
            conn = repo.getConnection();
            try {
                RepositoryResult<Resource> ctxs = conn.getContextIDs();
                while (ctxs.hasNext()) {
                    Resource ctx = ctxs.next();
                    int size = statementsCount(conn, ctx);
                    out.println("    " + ctx + " : " + size);
                    if (PRINT_ALL_STATEMENTS) {
                        RepositoryResult<Statement> statements = conn.getStatements(null, null, null, true, ctx);
                        while (statements.hasNext()) {
                            Statement statement = statements.next();
                            out.println("        " + statement.getSubject() + " " + statement.getPredicate() + " "
                                + statement.getObject());
                        }
                    }
                }
            } finally {
                closeConnection(conn, "Printing");
            }
        } catch (RepositoryException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public Set<String> contextNames() {
        HashSet<String> names = new HashSet<>();
        RepositoryConnection conn = null;
        try {
            conn = repo.getConnection();
            RepositoryResult<Resource> rs = conn.getContextIDs();
            while (rs.hasNext()) {
                names.add(rs.next().stringValue());
            }
            return names;
        } catch (RepositoryException x) {
            LOG.error("getting context names : {}", x.getMessage());
        } finally {
            closeConnection(conn, "Getting context names");
        }
        return names;
    }

    @Override
    public void clear(String contextName) {
        RepositoryConnection cnx = null;
        try {
            cnx = repo.getConnection();
            cnx.clear(context(cnx, contextName));
        } catch (RepositoryException x) {
            LOG.error("clearing context {} : {}", contextName, x.getMessage());
        } finally {
            closeConnection(cnx, "Clearing context " + contextName);
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
            closeConnection(cnx, "Querying");
        }
    }

    @Override
    public void add(TripleStore source) {
        Objects.requireNonNull(source);
        Repository sourceRepository;
        if (source instanceof TripleStoreBlazegraph) {
            sourceRepository = ((TripleStoreBlazegraph) source).repo;
            RepositoryConnection sourceConn = null;
            try {
                sourceConn = sourceRepository.getConnection();
                copyFrom(sourceConn);
            } catch (RepositoryException e) {
                LOG.error("Connecting to the source repository : {}", e.getMessage());
            } finally {
                if (sourceConn != null) {
                    closeConnection(sourceConn, "Copying from source");
                }
            }
        } else {
            throw new TripleStoreException(String.format("Add to %s from source %s is not supported",
                getImplementationName(), source.getImplementationName()));
        }
    }

    private void copyFrom(RepositoryConnection sourceConn) {
        RepositoryConnection targetConn = null;
        try {
            targetConn = repo.getConnection();
            targetConn.begin();
            copyNamespaces(sourceConn, targetConn);
            copyStatements(sourceConn, targetConn);
            targetConn.commit();
        } catch (RepositoryException e) {
            LOG.error("Copying from source : {}", e.getMessage());
        } finally {
            if (targetConn != null) {
                closeConnection(targetConn, "Copying from source");
            }
        }
    }

    private static void copyNamespaces(RepositoryConnection source, RepositoryConnection target) {
        try {
            RepositoryResult<Namespace> ns = source.getNamespaces();
            for (Namespace namespace : Iterations.asList(ns)) {
                target.setNamespace(namespace.getPrefix(), namespace.getName());
            }
        } catch (RepositoryException e) {
            LOG.error("Copying Namespaces : {}", e.getMessage());
        }
    }

    private static void copyStatements(RepositoryConnection sourceConn, RepositoryConnection targetConn)
        throws RepositoryException {
        ValueFactory vfac = targetConn.getValueFactory();

        for (Resource context : Iterations.asList(sourceConn.getContextIDs())) {
            RepositoryResult<Statement> statements = sourceConn.getStatements(null, null, null, true, context);
            // we need to get StringValue from each item, to get rid of the source repo
            // references.
            for (Statement st : Iterations.asList(statements)) {
                URI s = vfac.createURI(st.getSubject().stringValue());
                URI p = vfac.createURI(st.getPredicate().stringValue());
                Resource targetContext = vfac.createURI(st.getContext().stringValue());
                Statement targetStatement;
                if (st.getObject() instanceof URI) {
                    URI o = vfac.createURI(st.getObject().stringValue());
                    targetStatement = vfac.createStatement(s, p, o, targetContext);
                } else if (st.getObject() instanceof Literal) {
                    Literal o = vfac.createLiteral(st.getObject().stringValue());
                    targetStatement = vfac.createStatement(s, p, o, targetContext);
                } else {
                    throw new TripleStoreException(String.format("Unsupported value: %s for subject %s",
                        st.getObject().stringValue(), s.stringValue()));
                }
                Objects.requireNonNull(targetStatement);
                targetConn.add(targetStatement);
            }
        }
    }

    @Override
    public void update(String query) {
        try {
            RepositoryConnection conn = repo.getConnection();
            update(conn, adjustedQuery(query));
        } catch (RepositoryException e) {
            throw new TripleStoreException("Opening repo for update", e);
        }
    }

    private void update(RepositoryConnection conn, String query) {
        try {
            conn.prepareUpdate(QueryLanguage.SPARQL, query).execute();
        } catch (MalformedQueryException | UpdateExecutionException | RepositoryException e) {
            throw new TripleStoreException(String.format("Query [%s]", query), e);
        } finally {
            closeConnection(conn, "Update operation");
        }
    }

    @Override
    public void add(String contextName, String objNs, String objType, PropertyBags statements) {
        RepositoryConnection cnx = null;
        try {
            cnx = repo.getConnection();
            addStatements(cnx, contextName, objNs, objType, statements);

        } catch (RepositoryException x) {
            throw new TripleStoreException(String.format("Adding statements for graph %s", contextName), x);
        } finally {
            closeConnection(cnx, "Adding statements to context " + contextName);
        }
    }

    @Override
    public String add(String contextName, String objNs, String objType, PropertyBag properties) {
        RepositoryConnection cnx = null;
        try {
            cnx = repo.getConnection();
            return addStatement(cnx, contextName, objNs, objType, properties);
        } catch (RepositoryException x) {
            throw new TripleStoreException(String.format("Adding statements for graph %s", contextName), x);
        } finally {
            closeConnection(cnx, "Adding statements to context " + contextName);
        }
    }

    private void addStatements(RepositoryConnection cnx, String contextName, String objNs, String objType,
        PropertyBags statements)
        throws RepositoryException {

        cnx.begin();
        for (PropertyBag statement : statements) {
            createStatements(cnx, objNs, objType, statement, context(cnx, contextName));
        }
        cnx.commit();
    }

    private String addStatement(RepositoryConnection cnx, String contextName, String objNs, String objType,
        PropertyBag properties) throws RepositoryException {
        cnx.begin();
        String id = createStatements(cnx, objNs, objType, properties, context(cnx, contextName));
        cnx.commit();
        return id;
    }

    private static String createStatements(RepositoryConnection cnx, String objNs, String objType,
        PropertyBag statement,
        Resource context) {
        try {
            URI resource = cnx.getValueFactory().createURI(cnx.getNamespace("data"),
                "_" + UUID.randomUUID().toString());
            URI parentPredicate = RDF.TYPE;
            URI parentObject = cnx.getValueFactory().createURI(objNs + objType);
            Statement parentSt = cnx.getValueFactory().createStatement(resource, parentPredicate, parentObject);
            cnx.add(parentSt, context);

            for (String name : statement.propertyNames()) {
                String property = statement.isClassProperty(name) ? name : objType + "." + name;
                URI predicate = cnx.getValueFactory().createURI(objNs + property);
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
            return resource.getLocalName();
        } catch (Exception x) {
            throw new TripleStoreException(String.format("Creating statements for object type %s", objNs + objType), x);
        }
    }

    private static PropertyBags query(RepositoryConnection cnx, String query) throws RepositoryException {
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
                    names.forEach(name -> {
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

    private static int statementsCount(RepositoryConnection conn, Resource ctx) {
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

    private static void write(Model statements, OutputStream out) {
        try (PrintStream pout = new PrintStream(out)) {
            RDFWriter w = new RDFXMLPrettyWriter(pout);
            Rio.write(statements, w);
        } catch (Exception x) {
            throw new TripleStoreException("Writing model statements", x);
        }
    }

    private static void copyNamespacesToModel(RepositoryConnection conn, Model m) throws RepositoryException {
        RepositoryResult<Namespace> ns = conn.getNamespaces();
        while (ns.hasNext()) {
            m.setNamespace(ns.next());
        }
    }

    private static void addNamespaceForBase(RepositoryConnection cnx, String base) throws RepositoryException {
        cnx.setNamespace("data", base + "/#");
    }

    private Resource context(RepositoryConnection conn, String contextName) {
        String name1 = contextName.replace(namespaceForContexts(), "");
        return conn.getValueFactory().createURI(namespaceForContexts(), name1);
    }

    @Override
    public void addNamespace(String prefix, String namespace) {
        RepositoryConnection cnx = null;
        try {
            cnx = repo.getConnection();
            cnx.setNamespace(prefix, namespace);
        } catch (RepositoryException x) {
            throw new TripleStoreException(String.format("Adding namespace %s", namespace), x);
        } finally {
            closeConnection(cnx, "Adding namespace " + namespace);
        }
    }

    @Override
    public List<PrefixNamespace> getNamespaces() {
        List<PrefixNamespace> namespaces = new ArrayList<>();
        RepositoryConnection cnx = null;
        try {
            cnx = repo.getConnection();
            RepositoryResult<Namespace> ns = cnx.getNamespaces();
            while (ns.hasNext()) {
                Namespace namespace = ns.next();
                namespaces.add(new PrefixNamespace(namespace.getPrefix(), namespace.getName()));
            }
        } catch (RepositoryException x) {
            throw new TripleStoreException("Getting namespaces", x);
        } finally {
            closeConnection(cnx, "Getting namespaces");
        }
        return namespaces;
    }

    private final Repository repo;

    private static final Logger LOG = LoggerFactory.getLogger(TripleStoreBlazegraph.class);

    private static final boolean PRINT_ALL_STATEMENTS = false;
}
