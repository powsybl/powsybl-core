/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.triplestore.impl.rdf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.eclipse.rdf4j.common.xml.XMLUtil;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Literals;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class PowsyblWriter extends RDFXMLWriter {

    private String lastObjNamespace;
    private String lastObjLocalName;

    public PowsyblWriter(OutputStream out) {
        super(out);
    }

    public PowsyblWriter(Writer writer) {
        super(writer);
    }

    @Override
    public void handleStatement(Statement st) {
        if (!writingStarted) {
            throw new RDFHandlerException("Document writing has not yet been started");
        }

        Resource subj = st.getSubject();
        IRI pred = st.getPredicate();
        Value obj = st.getObject();

        // Verify that an XML namespace-qualified name can be created for the predicate
        String predString = pred.toString();
        int predSplitIdx = XMLUtil.findURISplitIndex(predString);
        if (predSplitIdx == -1) {
            throw new RDFHandlerException("Unable to create XML namespace-qualified name for predicate: " + predString);
        }

        String predNamespace = predString.substring(0, predSplitIdx);
        String predLocalName = predString.substring(predSplitIdx);

        try {
            if (!headerWritten) {
                writeHeader();
            }

            // SUBJECT
            if (!subj.equals(lastWrittenSubject)) {
                writeNewSubject(subj, obj, st.getContext().stringValue());
            } else {
                writeLastSubject(obj, predNamespace, predLocalName);
            }

            // Don't write </rdf:Description> yet, maybe the next statement
            // has the same subject.
        } catch (IOException e) {
            throw new RDFHandlerException(e);
        }
    }

    private void writeNewSubject(Resource subj, Value obj, String ctxt) throws IOException {
        flushPendingStatements();

        String objString = obj.toString();
        int objSplitIdx = XMLUtil.findURISplitIndex(objString);
        if (objSplitIdx == -1) {
            throw new RDFHandlerException("Unable to create XML namespace-qualified name for predicate: " + objString);
        }
        String objNamespace = objString.substring(0, objSplitIdx);
        String objLocalName = objString.substring(objSplitIdx);

        // Write new subject:
        writeNewLine();
        writeStartOfStartTag(objNamespace, objLocalName);

        // TODO This is hard-coded logic for processing CGMES data
        IRI uri = (IRI) subj;
        String attName = "ID";
        String value = uri.toString();
        String prefix = namespaceTable.get(uri.getNamespace());
        if (uri.getNamespace().equals("urn:uuid:")) {
            if (objLocalName.equals("FullModel")) {
                attName = "about";
            } else {
                value = "_" + uri.getLocalName();
            }
        }
        if (prefix != null && prefix.equals("data")) {
            if (ctxt.contains("_SSH_") || (ctxt.contains("_DY_") && objLocalName.equals("EnergyConsumer"))
                    || (ctxt.contains("_TP_") && !objLocalName.equals("TopologicalNode"))) {
                attName = "about";
                value = "#" + uri.getLocalName();
            } else {
                value = uri.getLocalName();
            }
        }

        writeAttribute(RDF.NAMESPACE, attName, value);
        writeEndOfStartTag();
        writeNewLine();
        lastWrittenSubject = subj;
        lastObjNamespace = objNamespace;
        lastObjLocalName = objLocalName;
    }

    private void writeLastSubject(Value obj, String predNamespace, String predLocalName) throws IOException {
        // PREDICATE
        writeIndent();
        writeStartOfStartTag(predNamespace, predLocalName);

        // OBJECT
        if (obj instanceof Resource) {
            writeResource(obj);
        } else if (obj instanceof Literal) {
            writeLiteral(obj);
            writeEndTag(predNamespace, predLocalName);
        }

        writeNewLine();
    }

    private void writeResource(Value obj) throws IOException {
        Resource objRes = (Resource) obj;

        if (objRes instanceof BNode) {
            BNode bNode = (BNode) objRes;
            writeAttribute(RDF.NAMESPACE, "nodeID", getValidNodeId(bNode));
        } else {
            IRI uri = (IRI) objRes;
            String value = uri.toString();
            String prefix = namespaceTable.get(uri.getNamespace());
            // TODO review the use of hard-coded literal "data" for CGMES
            if (prefix != null && prefix.equals("data")) {
                value = "#" + uri.getLocalName();
            }
            writeAttribute(RDF.NAMESPACE, "resource", value);
        }

        writeEndOfEmptyTag();
    }

    private void writeLiteral(Value obj) throws IOException {
        Literal objLit = (Literal) obj;
        // datatype attribute
        boolean isXMLLiteral = false;

        // language attribute
        if (Literals.isLanguageLiteral(objLit)) {
            writeAttribute("xml:lang", objLit.getLanguage().orElse(""));
        } else {
            IRI datatype = objLit.getDatatype();
            // Check if datatype is rdf:XMLLiteral
            isXMLLiteral = datatype.equals(RDF.XMLLITERAL);

            if (isXMLLiteral) {
                writeAttribute(RDF.NAMESPACE, "parseType", "Literal");
            } else if (!datatype.equals(XMLSchema.STRING)) {
                writeAttribute(RDF.NAMESPACE, "datatype", datatype.toString());
            }
        }

        writeEndOfStartTag();

        // label
        if (isXMLLiteral) {
            // Write XML literal as plain XML
            writer.write(objLit.getLabel());
        } else {
            writeCharacterData(objLit.getLabel());
        }
    }

    @Override
    protected void flushPendingStatements()
            throws IOException {
        if (lastWrittenSubject != null) {
            // The last statement still has to be closed:
            writeEndTag(lastObjNamespace, lastObjLocalName);
            writeNewLine();

            lastWrittenSubject = null;
            lastObjNamespace = null;
            lastObjLocalName = null;
        }
    }
}
