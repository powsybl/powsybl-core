package com.powsybl.cgmes.conversion.update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.NetworkListener;

public class ChangesListener implements NetworkListener {
    /**
     * *class to register network changes, and add to changeListUpdate
     *
     * @param network          represent a grid network object
     * @param changeListUpdate is an empty list to ctore iidm changes
     */

    public ChangesListener(List<IidmChange> changeList) {
        this.changeList = changeList;
        this.ignoreList = ignore;
    }

    @Override
    public void onCreation(Identifiable identifiable) {
        LOG.info("Calling onCreation method...");
        String variant = null;
        IidmChangeOnCreate change = new IidmChangeOnCreate(identifiable, variant);
        changeList.add(change);
    }

    @Override
    public void onRemoval(Identifiable identifiable) {
        String variant = null;
        IidmChangeOnRemove change = new IidmChangeOnRemove(identifiable, variant);
        changeList.add(change);
    }

    @Override
    public void onUpdate(Identifiable identifiable, String attribute, String variantId, Object oldValue,
        Object newValue) {
        IidmChangeOnUpdate change = new IidmChangeOnUpdate(identifiable, attribute, oldValue, newValue, variantId);
        addToChangeLog(change);
    }

    @Override
    public void onUpdate(Identifiable identifiable, String attribute, Object oldValue, Object newValue) {
        LOG.info("Calling onUpdate method...");
        String variantId = null;
        onUpdate(identifiable, attribute, variantId, oldValue, newValue);
    }

    @Override
    public void onVariantCreated(String sourceVariantId, String targetVariantId) {
        List<IidmChange> tmp = changeList.stream()
            .filter(s -> {
                return (s instanceof IidmChangeOnUpdate
                    && s.getVariant() != null);
            })
            .map(s -> {
                IidmChangeOnUpdate v = new IidmChangeOnUpdate(
                    s.getIdentifiable(), s.getAttribute(), s.getOldValue(), s.getNewValue(), s.getVariant());
                v.setVariant(targetVariantId);
                return v;
            }).collect(Collectors.toList());

        changeList.addAll(tmp);
    }

    private void addToChangeLog(IidmChange change) {
        if (!ignoreList.contains(change.getAttribute())) {
            changeList.add(change);
        }

    }

    public static final String CONNECTED_COMPONENT_NUMBER = "connectedComponentNumber";
    public static final String CONSTANT_2 = "*value*";
    public static final String CONSTANT_N = "*value*";

    public static final Set<String> ignore = Collections.unmodifiableSet(
        new HashSet<String>(Arrays.asList(
            CONNECTED_COMPONENT_NUMBER,
            CONSTANT_2,
            CONSTANT_N)));

    private List<IidmChange> changeList;
    private Set<String> ignoreList;

    private static final Logger LOG = LoggerFactory.getLogger(ChangesListener.class);
}
