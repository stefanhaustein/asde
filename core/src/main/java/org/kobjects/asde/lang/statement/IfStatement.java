package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class IfStatement extends Node {

    public IfStatement(Node condition) {
        super(condition);
    }

    @Override
    protected void onResolve(ResolutionContext resolutionContext) {
        if (resolutionContext.mode == ResolutionContext.ResolutionMode.FUNCTION && children[0].returnType() != Types.BOOLEAN) {
            throw new RuntimeException("Boolean condition value expected.");
        }
    }

    @Override
    public Object eval(Interpreter interpreter) {
        if (!evalChildToBoolean(interpreter, 0)) {
            interpreter.currentLine++;
            interpreter.currentIndex = 0;
        }
        return null;
    }

    @Override
    public Type returnType() {
        return Types.VOID;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        appendLinked(asb, "IF ", errors);
        children[0].toString(asb, errors);
        asb.append(" THEN ");
    }
}
