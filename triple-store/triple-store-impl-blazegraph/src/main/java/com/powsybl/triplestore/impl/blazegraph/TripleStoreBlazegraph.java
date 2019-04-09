/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.triplestore.impl.blazegraph;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
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
import com.powsybl.triplestore.api.AbstractPowsyblTripleStore;
import com.powsybl.triplestore.api.CgmesContext;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.TripleStoreException;

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

        BigdataSail sail = new BigdataSail(props); // instantiate a sail
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
            Resource context = context(cnx, name);
            read(base, name, is, cnx, context);
            cnx.commit();
            addNamespaceForBase(cnx, base);
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

    private static void read(String base, String name, InputStream is, RepositoryConnection cnx, Resource context)
            throws RepositoryException {
        try {
            cnx.add(is, base, formatFromName(name), context);
        } catch (IOException x) {
            LOG.error("Reading. IO problem {}", x.getMessage());
        } catch (RDFParseException x) {
            LOG.error("Reading. RDF parsing problem {}", x.getMessage());
        }
    }

    private static RDFFormat formatFromName(String name) {
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
                copyNamespacesToModel(conn, model);

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
                conn.close();
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
            if (conn != null) {
                try {
                    conn.close();
                } catch (RepositoryException x) {
                    LOG.error("closing when getting context names : {}", x.getMessage());
                }
            }
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
    public void add(CgmesContext cgmesContext, String objNs, String objType, PropertyBags statements) {
        RepositoryConnection cnx = null;
        try {
            cnx = repo.getConnection();
            addStatements(cnx, cgmesContext, objNs, objType, statements);

        } catch (RepositoryException x) {
            throw new TripleStoreException(String.format("Adding statements for graph %s", cgmesContext.getProfile().name()), x);
        } finally {
            if (cnx != null) {
                try {
                    cnx.close();
                } catch (RepositoryException x) {
                    LOG.error("Adding statements for graph {}. Closing repository connection", cgmesContext.getProfile().name());
                }
            }
        }
    }

    @Override
    public String add(CgmesContext cgmesContext, String objNs, String objType, PropertyBag properties) {
        RepositoryConnection cnx = null;
        try {
            cnx = repo.getConnection();
            return addStatement(cnx, cgmesContext, objNs, objType, properties);
        } catch (RepositoryException x) {
            throw new TripleStoreException(String.format("Adding statements for graph %s", cgmesContext.getProfile().name()), x);
        } finally {
            if (cnx != null) {
                try {
                    cnx.close();
                } catch (RepositoryException x) {
                    LOG.error("Adding statements for graph {}. Closing repository connection", cgmesContext.getProfile().name());
                }
            }
        }
    }

    private void addStatements(RepositoryConnection cnx, CgmesContext cgmesContext, String objNs, String objType, PropertyBags statements)
            throws RepositoryException {

        cnx.begin();

        String name = getContextName(cnx, cgmesContext);

        Resource context = cnx.getValueFactory().createURI(name);
        for (PropertyBag statement : statements) {
            createStatements(cnx, objNs, objType, statement, context);
        }

        cnx.commit();
    }

    private String addStatement(RepositoryConnection cnx, CgmesContext cgmesContext, String objNs, String objType, PropertyBag properties) throws RepositoryException {
        cnx.begin();
        String name = getContextName(cnx, cgmesContext);
        Resource context = cnx.getValueFactory().createURI(name);
        String id = createStatements(cnx, objNs, objType, properties, context);
        cnx.commit();
        return id;
    }

    private String getContextName(RepositoryConnection cnx, CgmesContext cgmesContext) throws RepositoryException {
        String name = null;
        RepositoryResult<Resource> ctxs = cnx.getContextIDs();
        while (ctxs.hasNext()) {
            String ctx = ctxs.next().stringValue();
            if (ctx.contains("EQ")) {
                name = ctx.replace("EQ", cgmesContext.getProfile().name());
                break;
            }
        }
        if (name == null) {
            name = namespaceForContexts() + cgmesContext.getName();
        }
        return name;
    }

    private static String createStatements(RepositoryConnection cnx, String objNs, String objType, PropertyBag statement,
            Resource context) {
        try {
            UUID uuid = new UUID();
            URI resource = uuid.evaluate(cnx.getValueFactory());
            URI parentPredicate = RDF.TYPE;
            Literal parentObject = cnx.getValueFactory().createLiteral(objNs + objType);
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
        cnx.setNamespace("data", base + "#");
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
    public List<com.powsybl.triplestore.api.Namespace> getNamespaces() {
        List<com.powsybl.triplestore.api.Namespace> namespaces = new ArrayList<>();
        RepositoryConnection cnx = null;
        try {
            cnx = repo.getConnection();
            RepositoryResult<Namespace> ns = cnx.getNamespaces();
            while (ns.hasNext()) {
                Namespace namespace = ns.next();
                namespaces.add(new com.powsybl.triplestore.api.Namespace(namespace.getPrefix(), namespace.getName()));
            }
        } catch (RepositoryException x) {
            throw new TripleStoreException(String.format("Getting namespaces"), x);
        } finally {
            if (cnx != null) {
                try {
                    cnx.close();
                } catch (RepositoryException x) {
                    LOG.error(x.getMessage());
                }
            }
        }
        return namespaces;
    }

    private final Repository repo;

    private static final Logger LOG = LoggerFactory.getLogger(TripleStoreBlazegraph.class);

    private static final boolean PRINT_ALL_STATEMENTS = false;

}
