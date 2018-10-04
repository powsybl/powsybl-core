package com.powsybl.triplestore.jena;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
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
import org.apache.jena.shared.uuid.JenaUUID;
import org.apache.jena.vocabulary.RDF;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.triplestore.AbstractPowsyblTripleStore;
import com.powsybl.triplestore.PropertyBag;
import com.powsybl.triplestore.PropertyBags;
import com.powsybl.triplestore.TripleStoreException;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TripleStoreJena extends AbstractPowsyblTripleStore {

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
    public void read(String base, String name, InputStream is) {
        Model m = ModelFactory.createDefaultModel();
        m.read(is, base, formatFromName(name));
        dataset.addNamedModel(namedModelFromName(name), m);
        union = union.union(m);
    }

    private String formatFromName(String filename) {
        if (filename.endsWith(".ttl")) {
            return "TURTLE";
        } else if (filename.endsWith(".xml")) {
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
            writer.setProperty("prettyTypes", subjectsTypes(m));
            writer.write(m, outputStream(ds, n), n);
        }
    }

    @Override
    public void dump(PrintStream out) {
        out.println("dump CGMES Jena model. Graph names and sizes with subjects types");
        Iterator<String> k = dataset.listNames();
        while (k.hasNext()) {
            String n = k.next();
            Model m = dataset.getNamedModel(n);
            out.println("    " + n + " : " + m.size());
            out.println("        " + Arrays.toString(subjectsTypes(m)).replace(",", ",\n        "));
        }
    }

    @Override
    public void clear(String name) {
        Iterator<String> k = dataset.listNames();
        while (k.hasNext()) {
            String n = k.next();
            if (n.contains(name)) {
                Model m = dataset.getNamedModel(n);
                union.remove(m);
                m.removeAll();
            }
        }
    }

    @Override
    public PropertyBags query(String query) {
        String query1 = adjustedQuery(query);
        PropertyBags results = new PropertyBags();
        // Because Jena in-memory does not support default graph as union of named
        // graphs
        // We use the dataset for maintaining separate graphs, but query in general
        // against union
        // Only query against dataset if we found a GRAPH clause in the query text
        try (QueryExecution q = queryExecutionFromQueryText(query1)) {
            // Uncomment to analyze the algebra of the query
            // q.getContext().set(ARQ.symLogExec,true) ;
            ResultSet r = q.execSelect();
            List<String> names = r.getResultVars();
            while (r.hasNext()) {
                QuerySolution s = r.next();
                PropertyBag result = new PropertyBag(names);
                names.stream().forEach(name -> {
                    if (s.contains(name)) {
                        result.put(name, stringValue(s.get(name)));
                    }
                });
                if (result.size() > 0) {
                    results.add(result);
                }
            }
        }
        return results;
    }

    @Override
    public void add(String graph, String objType, PropertyBags statements) {
        String name = null;
        Iterator<String> k = dataset.listNames();
        while (k.hasNext()) {
            String n = k.next();
            if (n.contains("EQ")) {
                name = n.replace("EQ", graph);
                break;
            }
        }

        String context = name;
        Model m = dataset.getNamedModel(context);
        if (m == null) {
            m = ModelFactory.createDefaultModel();
            m.setNsPrefixes(union.getNsPrefixMap());
        }

        Iterator<PropertyBag> itStatements = statements.iterator();
        while (itStatements.hasNext()) {
            PropertyBag statement = itStatements.next();
            createStatements(m, objType, statement);
        }
        dataset.addNamedModel(context, m);
        union = union.union(m);
    }

    private void createStatements(Model m, String objType, PropertyBag statement) {

        Resource resource = m.createResource(JenaUUID.generate().asString());
        Property parentPredicate = RDF.type;
        Resource parentObject = m.createResource(objType);
        Statement parentSt = m.createStatement(resource, parentPredicate, parentObject);
        m.add(parentSt);

        List<String> names = statement.propertyNames();
        names.stream().forEach(name -> {
            Property predicate = m.createProperty(objType + "." + name);
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
    }

    private QueryExecution queryExecutionFromQueryText(String query) {
        if (containsGraphClause(query)) {
            return QueryExecutionFactory.create(query, dataset);
        } else {
            return QueryExecutionFactory.create(query, union);
        }
    }

    private boolean containsGraphClause(String query) {
        return query.indexOf("GRAPH ") >= 0;
    }

    private String stringValue(RDFNode n) {
        if (n.isResource()) {
            return n.asResource().getURI();
        } else if (n.isLiteral()) {
            return n.asLiteral().getValue().toString();
        }
        return n.toString();
    }

    public Resource[] subjectsTypes(Model model) {
        Set<Resource> types = new HashSet<>();
        ResIterator rs = model.listSubjects();
        while (rs.hasNext()) {
            Resource r = rs.nextResource();
            Statement s = type(r);
            if (s != null) {
                types.add(s.getObject().asResource());
            }
        }
        return types.toArray(new Resource[types.size()]);
    }

    private Statement type(Resource r) {
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

    private boolean validType(RDFNode n) {
        if (!(n instanceof Resource)) {
            return false;
        }
        if (((Resource) n).isAnon()) {
            return false;
        }
        // Only allow resources with namespace and fragment ID
        String uri = ((Resource) n).getURI();
        int split = Util.splitNamespaceXML(uri);
        return !(split == 0 || split == uri.length());
    }

    private String namedModelFromName(String filename) {
        return namespaceForContexts() + filename;
    }

    private final Dataset dataset;
    private Model union;
    private RDFWriter writer;
}
