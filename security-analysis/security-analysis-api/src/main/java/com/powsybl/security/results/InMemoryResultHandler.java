package com.powsybl.security.results;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.security.strategy.OperatorStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class InMemoryResultHandler implements SecurityAnalysisResultHandler {

    public static class StateResult {

        private final List<BranchResult> branchResultList;
        private final List<ThreeWindingsTransformerResult> threeWindingsTransformerResultList;
        private final List<BusResult> busResultList;

        StateResult() {
            branchResultList = new ArrayList<>();
            threeWindingsTransformerResultList = new ArrayList<>();
            busResultList = new ArrayList<>();
        }

        List<BranchResult> getBranchResultList() {
            return branchResultList;
        }

        List<ThreeWindingsTransformerResult> getThreeWindingsTransformerResultList() {
            return threeWindingsTransformerResultList;
        }

        List<BusResult> getBusResultList() {
            return busResultList;
        }

        void addBranchResult(BranchResult branchResult) {
            branchResultList.add(branchResult);
        }

        void addThreeWindingsTransformerResult(ThreeWindingsTransformerResult  threeWindingsTransformerResult) {
            threeWindingsTransformerResultList.add(threeWindingsTransformerResult);
        }

        void addBusResult(BusResult busResult) {
            busResultList.add(busResult);
        }
    }

    StateResult baseCaseResult;
    Map<String, StateResult> postContingencyResults;
    Map<String, StateResult> operatorStrategyResults;

    public InMemoryResultHandler() {
        baseCaseResult = new StateResult();
        postContingencyResults = new HashMap<>();
        operatorStrategyResults = new HashMap<>();
    }

    @Override
    public void writeBranchResult(Contingency contingency, OperatorStrategy operatorStrategy, BranchResult branchResult) {
        registerResult(contingency, operatorStrategy, branchResult, StateResult::addBranchResult);
    }

    @Override
    public void writeThreeWindingsTransformerResult(Contingency contingency, OperatorStrategy operatorStrategy, ThreeWindingsTransformerResult threeWindingsTransformerResult) {
        registerResult(contingency, operatorStrategy, threeWindingsTransformerResult, StateResult::addThreeWindingsTransformerResult);
    }

    @Override
    public void writeBusResult(Contingency contingency, OperatorStrategy operatorStrategy, BusResult busResult) {
        registerResult(contingency, operatorStrategy, busResult, StateResult::addBusResult);
    }

    StateResult getBaseCaseResult() {
        return baseCaseResult;
    }

    Map<String, StateResult> getPostContingencyResults() {
        return postContingencyResults;
    }

    Map<String, StateResult> getOperatorStrategyResults() {
        return operatorStrategyResults;
    }

    <T> void registerResult(Contingency contingency, OperatorStrategy operatorStrategy, T result, BiConsumer<StateResult, T> resultAdder) {
        if (contingency == null) {
            resultAdder.accept(baseCaseResult, result);
        } else if (operatorStrategy == null) {
            resultAdder.accept(postContingencyResults.computeIfAbsent(contingency.getId(), k -> new StateResult()), result);
        } else {
            resultAdder.accept(operatorStrategyResults.computeIfAbsent(operatorStrategy.getId(), k -> new StateResult()), result);
        }
    }
}
