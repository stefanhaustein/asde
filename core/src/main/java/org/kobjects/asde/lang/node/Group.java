package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;

public class Group extends Node {
    public Group(Node child) {
        super(child);
    }

    @Override
    protected void onResolve(ValidationContext resolutionContext, int line) {
        // Nothing to do here.
    }

    @Override
    public Object eval(EvaluationContext evaluationContext) {
        return children[0].eval(evaluationContext);
    }

    @Override
    public Type returnType() {
        return children[0].returnType();
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
        appendLinked(asb, "(", errors);

        children[0].toString(asb, errors, preferAscii);
        appendLinked(asb, ")", errors);
    }
}
