package com.powsybl.cgmes.update;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkListener;

public class ChangesListener implements NetworkListener {
    /**
     * *class to register network changes, and add to changeList
     *
     * @param network    represent a grid network object
     * @param changeList is an empty list to ctore iidm changes
     */

    public ChangesListener(Network network, List<IidmChangesObject> changeList) {
        this.network = network;
        this.changeList = changeList;
    }

    @Override
    public void onCreation(Identifiable identifiable) {
        LOGGER.info("Calling onCreation method...");
        String variant = network.getVariantManager().getWorkingVariantId();
        IidmChangesObject change = new IidmChangesObject(identifiable, variant);
        changeList.add(change);
        // TODO remove prints
        System.out.println("variant is " + change.getVariant() + "\nattribute is " + change.getAttribute());
    }

    @Override
    public void onRemoval(Identifiable identifiable) {
        LOGGER.info("Calling onRemoval method...");
        String variant = network.getVariantManager().getWorkingVariantId();
        IidmChangesObject change = new IidmChangesObject(identifiable, variant);
        changeList.add(change);
    }

    @Override
    public void onUpdate(Identifiable identifiable, String attribute, Object oldValue, Object newValue) {
        LOGGER.info("Calling onUpdate method...");
        String variant = network.getVariantManager().getWorkingVariantId();
        IidmChangesObject change = new IidmChangesObject(identifiable, attribute, oldValue, newValue, variant);
        changeList.add(change);
        // TODO remove prints
        System.out.println("variant is " + change.getVariant() + "\nidentifiable " + identifiable.getName()
            + "\nattribute is " + change.getAttribute()
            + "\noldValue " + oldValue.toString() + "\nnewValue " + newValue.toString());
    }

    private final Network network;
    private List<IidmChangesObject> changeList;
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangesListener.class);
}
