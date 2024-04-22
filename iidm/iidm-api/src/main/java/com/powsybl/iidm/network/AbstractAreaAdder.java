package com.powsybl.iidm.network;

public interface AbstractAreaAdder<T extends Area, A extends AbstractAreaAdder<T, A>> extends IdentifiableAdder<T, A> {

    A setAreaType(AreaType areaType);

    @Override
    T add();

}
