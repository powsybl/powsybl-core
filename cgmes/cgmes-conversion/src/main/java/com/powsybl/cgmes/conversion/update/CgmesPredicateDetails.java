/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import java.util.Objects;

/**
 * The Class CgmesPredicateDetails contains details we need to know to construct
 * a triple: the predicate name, the triplestore context, if the value is node or
 * Literal, and the new Subject if we need to create new element (e.g
 * transformer end)
 *
 * @author Elena Kaltakova <kaltakovae at aia.es>
 *
 */
public class CgmesPredicateDetails {

    public CgmesPredicateDetails(String rdfPredicate, String triplestoreContext, boolean valueIsNode,
        String value, String newSubject) {
        this.rdfPredicate = rdfPredicate;
        this.context = triplestoreContext;
        this.valueIsNode = valueIsNode;
        this.value = value;
        this.newSubject = newSubject;
    }

    public CgmesPredicateDetails(String rdfPredicate, String triplestoreContext, boolean valueIsNode,
        String value) {
        this.rdfPredicate = rdfPredicate;
        this.context = triplestoreContext;
        this.valueIsNode = valueIsNode;
        this.value = value;
        this.newSubject = null;
    }

    public String getRdfPredicate() {
        return rdfPredicate;
    }

    public String getContext() {
        return context;
    }

    public boolean valueIsNode() {
        return valueIsNode;
    }

    public String getValue() {
        return value;
    }

    public String getNewSubject() {
        return newSubject;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setNewSubject(String newSubject) {
        Objects.requireNonNull(newSubject);
        this.newSubject = newSubject;
    }

    private String rdfPredicate;
    private String context;
    private boolean valueIsNode;
    private String newSubject;
    private String value;
}
