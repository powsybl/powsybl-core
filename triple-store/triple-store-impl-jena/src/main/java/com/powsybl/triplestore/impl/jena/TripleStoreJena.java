/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.triplestore.impl.jena;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.util.IteratorCollection;
import org.apache.jena.vocabulary.RDF;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.triplestore.api.AbstractPowsyblTripleStore;
import com.powsybl.triplestore.api.PrefixNamespace;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.TripleStore;
import com.powsybl.triplestore.api.TripleStoreException;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TripleStoreJena extends AbstractPowsyblTripleStore {

    static final String NAME = "jena";

    public TripleStoreJena() {
//       creates an in-memory Jena model that is able to contain multiple graphs
//       Jena version 3.0.0 has no transactional support.
//       DatasetFactory.createMem() is deprecated in 3.1.0.
//       Create an in-memory transactional Dataset. Introduced in Jena version 3.1.0
        dataset = DatasetFactory.createTxnMem();
//         Create a model just to obtain a writer and configure it
        writer = ModelFactory.createDefaultModel().getWriter("RDF/XML-ABBREV");
        writer.setProperty("showXmlDeclaration", "true");
        writer.setProperty("tab", "4");
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
        dataset.begin(TxnType.WRITE);
        try {
            Model m = ModelFactory.createDefaultModel();
            m.read(is, baseName, guessFormatFromName(contextName));
            addNamespaceForBase(m, baseName);
            dataset.addNamedModel(namedModelFromName(contextName), m);
            union = union.union(m);
            dataset.commit();
        } finally {
            dataset.end();
        }
    }

    private static void addNamespaceForBase(Model m, String baseName) {
        String basePrefix = m.getNsPrefixURI("data");
        if (basePrefix == null || basePrefix.isEmpty()) {
            m.setNsPrefix("data", baseName + "#");
        }
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
        dataset.begin(TxnType.WRITE);
        try {
            Iterator<String> contexts = dataset.listNames();
            while (contexts.hasNext()) {
                String context = contexts.next();
                Model model = dataset.getNamedModel(context);
                // when upgrading from 3.0.0 to latest versions, namespaces not present in
                // model, need to
                // be added from union
                model.setNsPrefixes(union.getNsPrefixMap());
                writer.setProperty("prettyTypes", subjectsTypes(model));
                // set xmlbase will improve output readability
                writer.setProperty("xmlbase", model.getNsPrefixMap().get("data"));
                writer.write(model, outputStream(ds, context), context);
            }
            dataset.commit();
        } finally {
            dataset.end();
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
        dataset.begin(TxnType.WRITE);
        try {
            String mname = namedModelFromName(contextName);
            Model m = dataset.getNamedModel(mname);
            union.remove(m);
            dataset.removeNamedModel(mname);
            m.removeAll();
            dataset.commit();
        } finally {
            dataset.end();
        }
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
            dataset.begin(TxnType.WRITE);
            try {
                sourceDataset = ((TripleStoreJena) source).dataset;
                union = union.union(((TripleStoreJena) source).union);
                for (String name : IteratorCollection.iteratorToList(sourceDataset.listNames())) {
                    String context = namedModelFromName(name);
                    if (sourceDataset.containsNamedModel(context)) {
                        Model targetModel = ModelFactory.createDefaultModel();
                        Model sourceModel = sourceDataset.getNamedModel(context);
                        copyNamespaces(union, targetModel);
                        for (Statement st : IteratorCollection.iteratorToList(sourceModel.listStatements())) {
                            targetModel.add(st);
                        }
                        dataset.addNamedModel(context, targetModel);
                    }
                }
                dataset.commit();
            } finally {
                dataset.end();
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
        UpdateAction.execute(UpdateFactory.create(adjustedQuery(query)), dataset);
    }

    @Override
    public void add(String contextName, String objNs, String objType, PropertyBags statements) {
        dataset.begin(TxnType.WRITE);
        try {
            Model m = getModel(contextName);
            for (PropertyBag statement : statements) {
                createStatements(m, objNs, objType, statement);
            }
            dataset.addNamedModel(contextName, m);
            union = union.union(m);
            dataset.commit();
        } finally {
            dataset.end();
        }
    }

    @Override
    public String add(String contextName, String objNs, String objType, PropertyBag properties) {
        String id;
        dataset.begin(TxnType.WRITE);
        try {
            Model m = getModel(contextName);
            id = createStatements(m, objNs, objType, properties);
            dataset.addNamedModel(contextName, m);
            union = union.union(m);
            dataset.commit();
        } finally {
            dataset.end();
        }
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

        Resource resource = m.createResource(m.getNsPrefixURI("data") + "_" + UUID.randomUUID().toString());
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
