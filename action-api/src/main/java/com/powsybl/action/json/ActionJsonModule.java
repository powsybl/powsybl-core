package com.powsybl.action.json;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.action.*;
import com.powsybl.iidm.network.identifiers.NetworkElementIdentifier;
import com.powsybl.iidm.network.identifiers.json.IdentifierDeserializer;
import com.powsybl.iidm.network.identifiers.json.IdentifierSerializer;

public class ActionJsonModule extends SimpleModule {

    public ActionJsonModule() {
        addSerializer(ActionList.class, new ActionListSerializer());
        addSerializer(NetworkElementIdentifier.class, new IdentifierSerializer());
        addDeserializer(ActionList.class, new ActionListDeserializer());
        addDeserializer(NetworkElementIdentifier.class, new IdentifierDeserializer());
        configureActionsSerialization();
    }

    /**
     * Deserializer for actions will be chosen based on the "type" property.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY, visible = true)
    interface ActionMixIn {
    }

    private <T, B> void registerActionBuilderType(Class<T> actionClass, Class<B> builderClass, String typeName,
                                                  JsonSerializer<T> serializer, JsonDeserializer<B> deserializer) {
        registerSubtypes(new NamedType(actionClass, typeName));
        registerSubtypes(new NamedType(builderClass, typeName));
        addDeserializer(builderClass, deserializer);
        addSerializer(actionClass, serializer);
    }

    private void configureActionsSerialization() {
        setMixInAnnotation(ActionBuilder.class, ActionJsonModule.ActionMixIn.class);
        registerActionBuilderType(SwitchAction.class, SwitchActionBuilder.class, SwitchAction.NAME,
                new SwitchActionSerializer(), new SwitchActionBuilderDeserializer());
        registerActionBuilderType(TerminalsConnectionAction.class, TerminalsConnectionActionBuilder.class,
            TerminalsConnectionAction.NAME, new TerminalsConnectionActionSerializer(),
            new TerminalsConnectionActionBuilderDeserializer());
        registerActionBuilderType(MultipleActionsAction.class, MultipleActionsActionBuilder.class, MultipleActionsAction.NAME,
                new MultipleActionsActionSerializer(), new MultipleActionsActionBuilderDeserializer());
        registerActionBuilderType(PhaseTapChangerTapPositionAction.class, PhaseTapChangerTapPositionActionBuilder.class,
            PhaseTapChangerTapPositionAction.NAME, new PhaseTapChangerTapPositionActionSerializer(), new PhaseTapChangerTapPositionActionBuilderDeserializer());
        registerActionBuilderType(RatioTapChangerTapPositionAction.class, RatioTapChangerTapPositionActionBuilder.class,
            RatioTapChangerTapPositionAction.NAME, new RatioTapChangerTapPositionActionSerializer(), new RatioTapChangerTapPositionActionBuilderDeserializer());
        registerActionBuilderType(PhaseTapChangerRegulationAction.class, PhaseTapChangerRegulationActionBuilder.class, PhaseTapChangerRegulationAction.NAME,
                new PhaseTapChangerRegulationActionSerializer(), new PhaseTapChangerRegulationActionBuilderBuilderDeserializer());
        registerActionBuilderType(RatioTapChangerRegulationAction.class, RatioTapChangerRegulationActionBuilder.class, RatioTapChangerRegulationAction.NAME,
                new RatioTapChangerRegulationActionSerializer(), new RatioTapChangerRegulationActionBuilderBuilderDeserializer());
        registerActionBuilderType(LoadAction.class, LoadActionBuilder.class, LoadAction.NAME, new LoadActionSerializer(), new LoadActionBuilderBuilderDeserializer());
        registerActionBuilderType(DanglingLineAction.class, DanglingLineActionBuilder.class, DanglingLineAction.NAME, new DanglingLineActionSerializer(), new DanglingLineActionBuilderBuilderDeserializer());
        registerActionBuilderType(HvdcAction.class, HvdcActionBuilder.class, HvdcAction.NAME, new HvdcActionSerializer(), new HvdcActionBuilderDeserializer());
        registerActionBuilderType(GeneratorAction.class, GeneratorActionBuilder.class, GeneratorAction.NAME,
                new GeneratorActionSerializer(), new GeneratorActionBuilderDeserializer());
        registerActionBuilderType(ShuntCompensatorPositionAction.class, ShuntCompensatorPositionActionBuilder.class,
            ShuntCompensatorPositionAction.NAME,
                new ShuntCompensatorPositionActionSerializer(), new ShuntCompensatorPositionActionBuilderDeserializer());
        registerActionBuilderType(StaticVarCompensatorAction.class, StaticVarCompensatorActionBuilder.class,
            StaticVarCompensatorAction.NAME, new StaticVarCompensatorActionSerializer(),
            new StaticVarCompensatorActionBuilderDeserializer());
        registerActionBuilderType(AreaInterchangeTargetUpdateAction.class, AreaInterchangeTargetUpdateActionBuilder.class, AreaInterchangeTargetUpdateAction.NAME,
            new AreaInterchangeTargetUpdateActionSerializer(), new AreaInterchangeTargetUpdateActionDeserializer());
    }
}
