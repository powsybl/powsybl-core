/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.contingency.tasks.CompoundModificationTask;
import com.powsybl.contingency.tasks.ModificationTask;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Contingency {

    private final String id;

    private final List<ContingencyElement> elements;

    public Contingency(String id, ContingencyElement... elements) {
        this(id, Arrays.asList(elements));
    }

    public Contingency(String id, List<ContingencyElement> elements) {
        this.id = Objects.requireNonNull(id);
        this.elements = Objects.requireNonNull(elements);
    }

    public String getId() {
        return id;
    }

    public Collection<ContingencyElement> getElements() {
        return Collections.unmodifiableCollection(elements);
    }

    public ModificationTask toTask() {
        List<ModificationTask> subTasks = elements.stream().map(ContingencyElement::toTask).collect(Collectors.toList());

        return new CompoundModificationTask(subTasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, elements);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Contingency) {
            Contingency other = (Contingency) obj;
            return id.equals(other.id) && elements.equals(other.elements);
        }
        return false;
    }
}
