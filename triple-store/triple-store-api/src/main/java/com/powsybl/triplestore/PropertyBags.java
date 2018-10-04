package com.powsybl.triplestore;

/*
 * #%L
 * Triple stores for CGMES models
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class PropertyBags extends ArrayList<PropertyBag> {

    public PropertyBags() {
        super();
    }

    public PropertyBags(Collection<PropertyBag> ps) {
        super(ps);
    }

    public List<String> pluck(String property) {
        return stream()
                .map(r -> r.get(property))
                .sorted(Comparator.nullsLast(String::compareTo))
                .collect(Collectors.toList());
    }

    public List<String> pluckLocals(String property) {
        return stream()
                .map(r -> r.getLocal(property))
                .sorted(Comparator.nullsLast(String::compareTo))
                .collect(Collectors.toList());
    }

    public PropertyBags pivot(
            String idProperty,
            String keyProperty,
            List<String> pivotPropertyNames,
            String valueProperty) {
        int estimatedNumObjects = size() / pivotPropertyNames.size();
        Map<String, PropertyBag> objects = new HashMap<>(estimatedNumObjects);
        List<String> propertyNames = new ArrayList<>(pivotPropertyNames.size() + 1);
        propertyNames.add(idProperty);
        propertyNames.addAll(pivotPropertyNames);
        stream().forEach(b -> {
            String id = b.getId(idProperty);
            PropertyBag object = objects.computeIfAbsent(id, id1 -> {
                PropertyBag o1 = new PropertyBag(propertyNames);
                o1.put(idProperty, id1);
                return o1;
            });
            String property = b.get(keyProperty);
            String value = b.get(valueProperty);
            object.put(property, value);
        });
        return new PropertyBags(objects.values());
    }

    public PropertyBags pivotLocalNames(
            String idProperty,
            String keyProperty,
            List<String> pivotPropertyLocalNames,
            String valueProperty) {
        int estimatedNumObjects = size() / pivotPropertyLocalNames.size();
        Map<String, PropertyBag> objects = new HashMap<>(estimatedNumObjects);
        List<String> propertyNames = new ArrayList<>(pivotPropertyLocalNames.size() + 1);
        propertyNames.add(idProperty);
        propertyNames.addAll(pivotPropertyLocalNames);
        stream().forEach(b -> {
            String id = b.getId(idProperty);
            PropertyBag object = objects.computeIfAbsent(id, id1 -> {
                PropertyBag o1 = new PropertyBag(propertyNames);
                o1.put(idProperty, id1);
                return o1;
            });
            String property = b.getLocal(keyProperty);
            String value = b.get(valueProperty);
            object.put(property, value);
        });
        return new PropertyBags(objects.values());
    }

    public String tabulateLocals() {
        return tabulate((bag, property) -> bag.getLocal(property));
    }

    public String tabulate() {
        return tabulate((bag, property) -> bag.get(property));
    }

    private String tabulate(BiFunction<PropertyBag, String, String> getValue) {
        if (size() == 0) {
            return "";
        }
        List<String> names = get(0).propertyNames();
        String columnSeparator = " \t ";
        String lineSeparator = System.lineSeparator();

        StringBuilder s = new StringBuilder(size() * 80);
        s.append(names.stream().collect(Collectors.joining(columnSeparator)));
        s.append(lineSeparator);
        s.append(stream()
                .map(r -> names.stream()
                        .map(n -> r.containsKey(n) ? getValue.apply(r, n) : "N/A")
                        .collect(Collectors.joining(columnSeparator)))
                .collect(Collectors.joining(lineSeparator)));
        return s.toString();
    }
}
