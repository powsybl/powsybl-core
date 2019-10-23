package com.powsybl.cgmes.conversion.update;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        changeList.add(change);
//        LOG.info("variant is " + change.getVariant()
//            + "\nidentifiable " + identifiable.getClass().getSimpleName()
//            + "\nidentifiableID " + identifiable.getId()
//            + "\nattribute is " + change.getAttribute());
    }

    @Override
    public void onUpdate(Identifiable identifiable, String attribute, Object oldValue, Object newValue) {
        LOG.info("Calling onUpdate method...");
        String variantId = null;
        onUpdate(identifiable, attribute, variantId, oldValue, newValue);
    }

    @Override
    public void onVariantCreated(String sourceVariantId, String targetVariantId) {
        List<IidmChange> tmp = new ArrayList<IidmChange>();
        tmp = changeList.stream()
            .filter(s -> {
                boolean valid = s instanceof IidmChangeOnUpdate
                    && s.getVariant() != null;
                return valid;
            })
            .map(s -> {
                IidmChangeOnUpdate v = new IidmChangeOnUpdate(
                    s.getIdentifiable(), s.getAttribute(), s.getOldValue(), s.getNewValue(), s.getVariant());
                v.setVariant(targetVariantId);
                return v;
            }).collect(Collectors.toList());

        changeList.addAll(tmp);
    }

    private List<IidmChange> changeList;

    private static final Logger LOG = LoggerFactory.getLogger(ChangesListener.class);
}
