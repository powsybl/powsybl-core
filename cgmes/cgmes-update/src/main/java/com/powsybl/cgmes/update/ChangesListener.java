package com.powsybl.cgmes.update;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkListener;

public class ChangesListener implements NetworkListener {
    /**
     * *class to register network changes, and add to changeListUpdate
     *
     * @param network          represent a grid network object
     * @param changeListUpdate is an empty list to ctore iidm changes
     */

    public ChangesListener(Network network, List<IidmChange> changeList) {
        this.network = network;
        this.changeList = changeList;
    }

    @Override
    public void onCreation(Identifiable identifiable) {
        LOG.info("Calling onCreation method...");
        String variant = null;
        IidmChangeOnCreate change = new IidmChangeOnCreate(identifiable, variant);
        changeList.add(change);
        // TODO remove prints
        System.out.println("variant is " + change.getVariant()
            + "\nidentifiableName " + identifiable.getName()
            + "\nidentifiableID " + identifiable.getId()
            + "\nattribute is " + change.getAttribute());
    }

    @Override
    public void onRemoval(Identifiable identifiable) {
        LOG.info("Calling onRemoval method...");
        String variant = null;
        IidmChangeOnRemove change = new IidmChangeOnRemove(identifiable, variant);
        changeList.add(change);
    }

    @Override
    public void onUpdate(Identifiable identifiable, String attribute, String variantId, Object oldValue,
        Object newValue) {
        LOG.info("Calling onUpdate method...");
        IidmChangeOnUpdate change = new IidmChangeOnUpdate(identifiable, attribute, oldValue, newValue, variantId);
        changeList.add(change);
        System.out.println("variant is " + change.getVariant()
            + "\nidentifiableName " + identifiable.getName()
            + "\nidentifiableID " + identifiable.getId()
            + "\nattribute is " + change.getAttribute());
    }

    @Override
    public void onUpdate(Identifiable identifiable, String attribute, Object oldValue, Object newValue) {
        LOG.info("Calling onUpdate method...");
//        String variantId = network.getVariantManager().getWorkingVariantId();
        String variantId = null;
        onUpdate(identifiable, attribute, variantId, oldValue, newValue);
    }

    private final Network network;
    private List<IidmChange> changeList;

    private static final Logger LOG = LoggerFactory.getLogger(ChangesListener.class);
}
