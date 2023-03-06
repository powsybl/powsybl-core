/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.TieLineUtil;
import com.powsybl.iidm.network.util.Identifiables;

import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.iidm.network.util.TieLineUtil.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class MergedLine implements TieLine {

    private final MergingViewIndex index;

    private final DanglingLineAdapter half1;

    private final DanglingLineAdapter half2;

    private String id;

    private final String name;

    private final Properties properties = new Properties();

    MergedLine(final MergingViewIndex index, final DanglingLine dl1, final DanglingLine dl2, boolean ensureIdUnicity) {
        this.index = Objects.requireNonNull(index, "merging view index is null");
        this.half1 = index.getDanglingLine(dl1);
        // must be reoriented. TieLine is defined as networkNode1-boundaryNode--boundaryNode-networkNode2
        // and in danglingLines the networkNode is always at end1
        this.half2 = index.getDanglingLine(dl2);
        this.id = ensureIdUnicity ? Identifiables.getUniqueId(buildMergedId(dl1.getId(), dl2.getId()), index::contains) : buildMergedId(dl1.getId(), dl2.getId());
        this.name = buildMergedName(dl1.getId(), dl2.getId(), dl1.getOptionalName().orElse(null), dl2.getOptionalName().orElse(null));
        mergeProperties(dl1, dl2, properties);
    }

    MergedLine(final MergingViewIndex index, final DanglingLine dl1, final DanglingLine dl2) {
        this(index, dl1, dl2, false);
    }

    @Override
    public MergingView getNetwork() {
        return index.getView();
    }

    DanglingLine getDanglingLine1() {
        return half1.getDelegate();
    }

    DanglingLine getDanglingLine2() {
        return half2.getDelegate();
    }

    @Override
    public String getId() {
        return id;
    }

    MergedLine setId(String id) {
        Objects.requireNonNull(id, "id is null");
        this.id = Identifiables.getUniqueId(id, index::contains);
        return this;
    }

    @Override
    public double getR() {
        return TieLineUtil.getR(half1, half2);
    }

    @Override
    public double getX() {
        return TieLineUtil.getX(half1, half2);
    }

    @Override
    public double getG1() {
        return TieLineUtil.getG1(half1, half2);
    }

    @Override
    public double getG2() {
        return TieLineUtil.getG2(half1, half2);
    }

    @Override
    public double getB1() {
        return TieLineUtil.getB1(half1, half2);
    }

    @Override
    public double getB2() {
        return TieLineUtil.getB2(half1, half2);
    }

    @Override
    public Optional<String> getOptionalName() {
        return Optional.ofNullable(name);
    }

    @Override
    public String getNameOrId() {
        return getOptionalName().orElse(id);
    }

    @Override
    public boolean hasProperty() {
        return !properties.isEmpty();
    }

    @Override
    public boolean hasProperty(final String key) {
        return properties.containsKey(key);
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet().stream().map(Object::toString).collect(Collectors.toSet());
    }

    @Override
    public boolean isFictitious() {
        return getDanglingLine1().isFictitious() || getDanglingLine2().isFictitious();
    }

    @Override
    public void setFictitious(boolean fictitious) {
        getDanglingLine1().setFictitious(fictitious);
        getDanglingLine2().setFictitious(fictitious);
    }

    @Override
    public String getProperty(final String key) {
        Object val = properties.get(key);
        return val != null ? val.toString() : null;
    }

    @Override
    public String getProperty(final String key, final String defaultValue) {
        Object val = properties.getOrDefault(key, defaultValue);
        return val != null ? val.toString() : null;
    }

    @Override
    public String setProperty(final String key, final String value) {
        getDanglingLine1().setProperty(key, value);
        getDanglingLine2().setProperty(key, value);
        return (String) properties.setProperty(key, value);
    }

    @Override
    public boolean removeProperty(String key) {
        boolean removed1 = getDanglingLine1().removeProperty(key);
        boolean removed2 = getDanglingLine2().removeProperty(key);
        properties.remove(key);
        return removed1 || removed2;
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public void remove() {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public <E extends Extension<TieLine>> void addExtension(final Class<? super E> type, final E extension) {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public <E extends Extension<TieLine>> E getExtension(final Class<? super E> type) {
        return null;
        // throw MergingView.createNotImplementedException();
    }

    @Override
    public <E extends Extension<TieLine>> E getExtensionByName(final String name) {
        // TODO(mathbagu): This method is used in the UCTE export so we prefer returning an empty list instead of throwing an exception
        // TODO(mathbagu): is it a good idea to extend AbstractExtendable?
        return null;
    }

    @Override
    public <E extends Extension<TieLine>> boolean removeExtension(final Class<E> type) {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public <E extends Extension<TieLine>> Collection<E> getExtensions() {
        // TODO(mathbagu): This method is used in the UCTE export so we prefer returning an empty list instead of throwing an exception
        // TODO(mathbagu): is it a good idea to extend AbstractExtendable?
        return Collections.emptyList();
    }

    @Override
    public String getImplementationName() {
        return "MergingView";
    }

    @Override
    public <E extends Extension<TieLine>, B extends ExtensionAdder<TieLine, E>> B newExtension(Class<B> type) {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public String getUcteXnodeCode() {
        return Optional.ofNullable(getDanglingLine1().getUcteXnodeCode()).orElseGet(() -> getDanglingLine2().getUcteXnodeCode());
    }

    @Override
    public DanglingLine getHalf1() {
        return half1;
    }

    @Override
    public DanglingLine getHalf2() {
        return half2;
    }

    @Override
    public DanglingLine getHalf(Branch.Side side) {
        switch (side) {
            case ONE:
                return half1;
            case TWO:
                return half2;
            default:
                throw new AssertionError("Unknown branch side " + side);
        }
    }

    @Override
    public DanglingLine getHalf(String voltageLevelId) {
        if (half1.getTerminal().getVoltageLevel().getId().equals(voltageLevelId)) {
            return half1;
        }
        if (half2.getTerminal().getVoltageLevel().getId().equals(voltageLevelId)) {
            return half2;
        }
        return null;
    }
}
