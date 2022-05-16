/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.triplestore.impl.jena;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.triplestore.api.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.util.IteratorCollection;
import org.apache.jena.vocabulary.RDF;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TripleStoreJena extends AbstractPowsyblTripleStore {

    static final String NAME = "jena";

    public TripleStoreJena() {
        // creates an in-memory Jena model that is able to contain multiple graphs
        dataset = DatasetFactory.createMem();

        // Create a model just to obtain a writer and configure it
        writer = ModelFactory.createDefaultModel().getWriter("RDF/XML-ABBREV");
        writer.setProperty("showXmlDeclaration", "true");
        writer.setProperty("tab", "8");
        writer.setProperty("relativeURIs", "same-document,relative");

        // We create a model that will be the union of all loaded graphs,
        // to be able to make queries over all data without the need for specifying a
        // graph
        // https://stackoverflow.com/questions/6981467/jena-arq-difference-between-model-graph-and-dataset
        union = ModelFactory.createDefaultModel();
    }

    @Override
    public String getImplementationName() {
        return NAME;
    }

    @Override
    public void read(InputStream is, String baseName, String contextName) {
        Model m = ModelFactory.createDefaultModel();
        m.read(is, baseName, guessFormatFromName(contextName));
        dataset.addNamedModel(namedModelFromName(contextName), m);
        union = union.union(m);
    }

    private static String guessFormatFromName(String name) {
        if (name.endsWith(".ttl")) {
            return "TURTLE";
        } else if (name.endsWith(".xml")) {
            return "RDF/XML";
        }
        return "RDF/XML";
    }

    @Override
    public void write(DataSource ds) {
        Iterator<String> k = dataset.listNames();
        while (k.hasNext()) {
            String n = k.next();
            Model m = dataset.getNamedModel(n);
            write(m, ds, n);
        }
    }

    @Override
    public void write(DataSource ds, String contextName) {
        Model m = dataset.getNamedModel(contextName);
        if (m == null) {
            throw new IllegalArgumentException("Invalid context: " + contextName);
        }
        write(m, ds, contextName);
    }

    private void write(Model model, DataSource ds, String contextName) {
        try (OutputStream os = outputStream(ds, contextName)) {
            writer.setProperty("prettyTypes", subjectsTypes(model));
            writer.write(model, os, contextName);
        } catch (IOException e) {
            throw new TripleStoreException(String.format("Error when closing the output stream %s in data source %s", contextName, ds), e);
        }
    }

    @Override
    public void print(PrintStream out) {
        out.println("TripleStore based on Jena. Graph names and sizes with subjects types");
        Iterator<String> k = dataset.listNames();
        while (k.hasNext()) {
            String n = k.next();
            Model m = dataset.getNamedModel(n);
            out.println("    " + n + " : " + m.size());
            out.println("        " + Arrays.toString(subjectsTypes(m)).replace(",", ",\n        "));
        }
    }

    @Override
    public Set<String> contextNames() {
        Iterable<String> it = dataset::listNames;
        return StreamSupport.stream(it.spliterator(), false).collect(Collectors.toSet());
    }

    @Override
    public void clear(String contextName) {
        String mname = namedModelFromName(contextName);
        Model m = dataset.getNamedModel(mname);
        union.remove(m);
        dataset.removeNamedModel(mname);
        m.removeAll();
    }

    @Override
    public PropertyBags query(String query) {
        String query1 = adjustedQuery(query);
        PropertyBags results = new PropertyBags();
        // Because Jena in-memory does not support default graph
        // as the union of named graphs
        // We use the dataset for maintaining separate graphs,
        // but query in general against union
        // Only query against dataset if we found a GRAPH clause in the query text
        try (QueryExecution q = queryExecutionFromQueryText(query1)) {
            // If we want to analyze the algebra of the query,
            // set ARQ.symLogExec to true in the query context
            ResultSet r = q.execSelect();
            List<String> names = r.getResultVars();
            while (r.hasNext()) {
                QuerySolution s = r.next();
                PropertyBag result = new PropertyBag(names);
                names.forEach(name -> {
                    if (s.contains(name)) {
                        result.put(name, stringValue(s.get(name)));
                    }
                });
                if (!result.isEmpty()) {
                    results.add(result);
                }
            }
        }
        return results;
    }

    @Override
    public void add(TripleStore source) {
        Objects.requireNonNull(source);
        Dataset sourceDataset;
        if (source instanceof TripleStoreJena) {
            sourceDataset = ((TripleStoreJena) source).dataset;
            for (String name : IteratorCollection.iteratorToList(sourceDataset.listNames())) {
                String context = namedModelFromName(name);
                if (sourceDataset.containsNamedModel(context)) {
                    Model targetModel = ModelFactory.createDefaultModel();
                    Model sourceModel = sourceDataset.getNamedModel(context);
                    copyNamespaces(sourceModel, targetModel);

                    for (Statement st : IteratorCollection.iteratorToList(sourceModel.listStatements())) {
                        targetModel.add(st);
                    }
                    dataset.addNamedModel(context, targetModel);
                    union = union.union(targetModel);
                }
            }
        } else {
            throw new TripleStoreException(String.format("Add to %s from source %s is not supported",
                getImplementationName(), source.getImplementationName()));
        }
    }

    private void copyNamespaces(Model source, Model target) {
        source.getNsPrefixMap().entrySet().forEach(e -> target.setNsPrefix(e.getKey(), e.getValue()));
    }

    @Override
    public void update(String query) {
        Objects.requireNonNull(dataset);
        try {
            UpdateAction.execute(UpdateFactory.create(adjustedQuery(query)), dataset);
        } catch (QueryException e) {
            throw new TripleStoreException(String.format("Query [%s]", query), e);
        }
    }

    @Override
    public void add(String contextName, String objNs, String objType, PropertyBags statements) {
        Model m = getModel(contextName);
        for (PropertyBag statement : statements) {
            createStatements(m, objNs, objType, statement);
        }
        dataset.addNamedModel(contextName, m);
        union = union.union(m);
    }

    @Override
    public String add(String contextName, String objNs, String objType, PropertyBag properties) {
        Model m = getModel(contextName);
        String id = createStatements(m, objNs, objType, properties);
        dataset.addNamedModel(contextName, m);
        union = union.union(m);
        return id;
    }

    private Model getModel(String context) {
        Model m = dataset.getNamedModel(context);
        if (m == null) {
            m = ModelFactory.createDefaultModel();
        }
        if (m.getNsPrefixMap().isEmpty()) {
            m.setNsPrefixes(union.getNsPrefixMap());
        }
        return m;
    }

    private String createStatements(Model m, String objNs, String objType, PropertyBag statement) {

        Resource resource = m.createResource(m.getNsPrefixURI("data") + AbstractPowsyblTripleStore.createRdfId());
        Property parentPredicate = RDF.type;
        Resource parentObject = m.createResource(objNs + objType);
        Statement parentSt = m.createStatement(resource, parentPredicate, parentObject);
        m.add(parentSt);

        List<String> names = statement.propertyNames();
        names.forEach(name -> {
            String property = statement.isClassProperty(name) ? name : objType + "." + name;
            Property predicate = m.createProperty(objNs + property);
            Statement st;
            if (statement.isResource(name)) {
                String namespace = m.getNsPrefixURI(statement.namespacePrefix(name));
                Resource object = m.createResource(namespace + statement.get(name));
                st = m.createStatement(resource, predicate, object);
            } else {
                String object = statement.get(name);
                st = m.createStatement(resource, predicate, object);
            }
            m.add(st);
        });
        return resource.getLocalName();
    }

    private QueryExecution queryExecutionFromQueryText(String query) {
        if (containsGraphClause(query)) {
            return QueryExecutionFactory.create(query, dataset);
        } else {
            return QueryExecutionFactory.create(query, union);
        }
    }

    private static boolean containsGraphClause(String query) {
        return query.contains("GRAPH ");
    }

    private static String stringValue(RDFNode n) {
        if (n.isResource()) {
            return n.asResource().getURI();
        } else if (n.isLiteral()) {
            return n.asLiteral().getValue().toString();
        }
        return n.toString();
    }

    private static Resource[] subjectsTypes(Model model) {
        Set<Resource> types = new HashSet<>();
        ResIterator rs = model.listSubjects();
        while (rs.hasNext()) {
            Resource r = rs.nextResource();
            Statement s = type(r);
            if (s != null) {
                types.add(s.getObject().asResource());
            }
        }
        return types.toArray(new Resource[0]);
    }

    private static Statement type(Resource r) {
        Statement rslt;
        try {
            if (r instanceof Statement) {
                rslt = ((Statement) r).getStatementProperty(RDF.type);
                if (rslt == null || (!rslt.getObject().equals(RDF.Statement))) {
                    throw new TripleStoreException(String.format("Looking for RDF.type for statement %s", r));
                }
            } else {
                rslt = r.getRequiredProperty(RDF.type);
            }
        } catch (PropertyNotFoundException x) {
            if (r instanceof Statement) {
                throw new TripleStoreException(String.format("Missing RDF.type for statement %s", r), x);
            }
            rslt = null;
        }
        if (rslt == null || !validType(rslt.getObject())) {
            return null;
        }
        return rslt;
    }

    private static boolean validType(RDFNode n) {
        if (!(n instanceof Resource)) {
            return false;
        }
        if (n.isAnon()) {
            return false;
        }
        // Only allow resources with namespace and fragment ID
        String uri = ((Resource) n).getURI();
        int split = Util.splitNamespaceXML(uri);
        return !(split == 0 || split == uri.length());
    }

    private String namedModelFromName(String contextName) {
        if (contextName.startsWith(namespaceForContexts())) {
            return contextName;
        }
        return namespaceForContexts() + contextName;
    }

    @Override
    public void addNamespace(String prefix, String namespace) {
        union.setNsPrefix(prefix, namespace);
    }

    @Override
    public List<PrefixNamespace> getNamespaces() {
        List<PrefixNamespace> namespaces = new ArrayList<>();
        Map<String, String> namespacesMap = union.getNsPrefixMap();
        namespacesMap.keySet().forEach(
            prefix -> namespaces.add(new PrefixNamespace(prefix, namespacesMap.get(prefix))));
        return namespaces;
    }

    private final Dataset dataset;
    private Model union;
    private RDFWriter writer;
}
