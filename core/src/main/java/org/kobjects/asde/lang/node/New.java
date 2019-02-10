package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.asde.lang.GlobalSymbol;
import org.kobjects.typesystem.Classifier;

import java.util.Map;

public class New extends Node {
    final String name;
    Classifier classifier;

    public New(Program program, String name) throws Exception {
        this.name = name;
        GlobalSymbol symbol = program.getSymbol(name);
        if (symbol == null) {
            throw new Exception("'" + name + "' is not defined");
        }
        Object value = symbol.getValue();
        if (!(value instanceof Classifier)) {
            throw new Exception("'" + name + "' is not a classifier");
        }
        classifier = (Classifier) value;
    }

    @Override
    protected void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
        // TODO
    }

    @Override
    public Object eval(EvaluationContext evaluationContext) {
        return classifier.createInstance();
    }

    @Override
    public Classifier returnType() {
        return classifier;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node,Exception> errors) {
        appendLinked(asb, "new " + name, errors);
    }
}
