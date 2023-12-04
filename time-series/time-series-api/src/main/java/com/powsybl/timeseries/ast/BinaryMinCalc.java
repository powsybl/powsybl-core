package com.powsybl.timeseries.ast;

import com.fasterxml.jackson.core.JsonParser;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.Deque;

public class BinaryMinCalc extends AbstractBinaryMinMax {

    static final String NAME = "binaryMin";

    public BinaryMinCalc(NodeCalc left, NodeCalc right) {
        super(left, right);
    }

    @Override
    protected String getJsonName() {
        return NAME;
    }

    static NodeCalc parseJson(JsonParser parser) throws IOException {
        ParsingContext context = parseJson2(parser);
        return new BinaryMinCalc(context.left, context.right);
    }

    @Override
    public <R, A> R accept(NodeCalcVisitor<R, A> visitor, A arg, int depth) {
        if (depth < NodeCalcVisitors.RECURSION_THRESHOLD) {
            Pair<NodeCalc, NodeCalc> p = visitor.iterate(this, arg);
            R leftValue = null;
            NodeCalc leftNode = p.getLeft();
            if (leftNode != null) {
                leftValue = leftNode.accept(visitor, arg, depth + 1);
            }
            R rightValue = null;
            NodeCalc rightNode = p.getRight();
            if (rightNode != null) {
                rightValue = rightNode.accept(visitor, arg, depth + 1);
            }
            return visitor.visit(this, arg, leftValue, rightValue);
        } else {
            return NodeCalcVisitors.visit(this, arg, visitor);
        }
    }

    @Override
    public <R, A> void acceptIterate(NodeCalcVisitor<R, A> visitor, A arg, Deque<Object> nodesStack) {
        Pair<NodeCalc, NodeCalc> p = visitor.iterate(this, arg);
        Object leftNode = p.getLeft();
        leftNode = leftNode == null ? NodeCalcVisitors.NULL : leftNode;
        Object rightNode = p.getRight();
        rightNode = rightNode == null ? NodeCalcVisitors.NULL : rightNode;
        nodesStack.push(leftNode);
        nodesStack.push(rightNode);
    }

    @Override
    public <R, A> R acceptHandle(NodeCalcVisitor<R, A> visitor, A arg, Deque<Object> resultsStack) {
        Object rightResult = resultsStack.pop();
        rightResult = rightResult == NodeCalcVisitors.NULL ? null : rightResult;
        Object leftResult = resultsStack.pop();
        leftResult = leftResult == NodeCalcVisitors.NULL ? null : leftResult;
        return visitor.visit(this, arg, (R) leftResult, (R) rightResult);
    }

    @Override
    public int hashCode() {
        return left.hashCode() + right.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BinaryMinCalc binaryMinCalc) {
            return binaryMinCalc.left.equals(left) && binaryMinCalc.right == right;
        }
        return false;
    }
}
