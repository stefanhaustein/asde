package org.kobjects.asde.lang.program;

import java.util.HashSet;
import java.util.LinkedHashSet;

public class ProgramValidationContext {
    final LinkedHashSet<String> dependencyChain = new LinkedHashSet<>();
    public final Program program;
    final HashSet<GlobalSymbol> validated = new HashSet<>();

    public ProgramValidationContext(Program program) {
        this.program = program;
    }

    void startChain(String name) {
        dependencyChain.clear();
        dependencyChain.add(name);
    }

    public GlobalSymbol resolve(String name) {
        GlobalSymbol symbol = program.getSymbol(name);
        if (symbol == null) {
            return null;
        }
        // We only check symbols with initializers for circles -- in contrast to constants, function dependencies
        // work without explicit initialization.
        if (dependencyChain.contains(name) && symbol.initializer != null) {
            throw new RuntimeException("Circular dependency: " + dependencyChain + " -> " + name);
        }
        if (!validated.contains(symbol)) {
           dependencyChain.add(name);
           symbol.validate(this);
           dependencyChain.remove(name);
        }
        return symbol;
    }


}