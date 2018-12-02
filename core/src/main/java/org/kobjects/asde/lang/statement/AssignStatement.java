package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.node.AssignableNode;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class AssignStatement extends Node {
    public AssignStatement(Node target, Node value) throws Exception {
        super(target, value);
        if (!(target instanceof AssignableNode)) {
            throw new Exception("Assignment target is not assignable.");
        }
    }

    @Override
    protected void onResolve(ResolutionContext resolutionContext, int line, int index) {
        if (resolutionContext.mode == ResolutionContext.ResolutionMode.FUNCTION
                && !Types.match(children[0].returnType(), children[1].returnType())) {
            throw new RuntimeException("Assignment source and target types do not match.");
        }
    }

    @Override
    public Object eval(Interpreter interpreter) {
        ((AssignableNode) children[0]).set(interpreter, children[1].eval(interpreter));
        return null;
    }

    @Override
    public Type returnType() {
        return Types.VOID;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        children[0].toString(asb, errors);
        appendLinked(asb, " = ", errors);
        children[1].toString(asb, errors);
    }
}
