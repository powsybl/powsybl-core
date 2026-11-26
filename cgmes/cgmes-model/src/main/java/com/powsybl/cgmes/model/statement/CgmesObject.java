/**
 * Copyright (c) 2026, Elia Group (http://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.model.statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The RDF statements of one CGMES object in one profile, grouped by their subject.
 *
 * @param subject the rdf:ID of the CGMES object
 * @param type the CIM class of the object, for example {@code SynchronousMachine}
 * @param properties the properties of the object, in the order of the file
 *
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public record CgmesObject(String subject, String type, List<CgmesProperty> properties) {

    public CgmesObject {
        Objects.requireNonNull(subject);
        Objects.requireNonNull(type);
        properties = List.copyOf(properties);
    }

    /**
     * Get the statements of this object: first the class, then one statement for each property.
     */
    public List<CgmesStatement> statements() {
        List<CgmesStatement> statements = new ArrayList<>(properties.size() + 1);
        statements.add(new CgmesStatement(subject, CgmesStatement.TYPE, type, true));
        for (CgmesProperty property : properties) {
            statements.add(new CgmesStatement(subject, property.predicate(), property.object(), property.resource()));
        }
        return statements;
    }

    /**
     * Start a new object of the given class.
     */
    public static Builder builder(String type, String subject) {
        return new Builder(type, subject);
    }

    /**
     * Collect the properties of a {@link CgmesObject} in the order of the file.
     */
    public static final class Builder {

        private final String type;
        private final String subject;
        private final List<CgmesProperty> properties = new ArrayList<>();

        private Builder(String type, String subject) {
            this.type = type;
            this.subject = subject;
        }

        /**
         * Add a property with a literal object.
         */
        public Builder literal(String predicate, String object) {
            properties.add(new CgmesProperty(predicate, object, false));
            return this;
        }

        /**
         * Add a property with a resource object.
         */
        public Builder resource(String predicate, String object) {
            properties.add(new CgmesProperty(predicate, object, true));
            return this;
        }

        public CgmesObject build() {
            return new CgmesObject(subject, type, properties);
        }
    }
}
