package org.kobjects.asde.lang;


import org.kobjects.asde.lang.node.Node;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

import java.util.HashMap;
import java.util.HashSet;

public class FunctionValidationContext {
    public enum ResolutionMode {FUNCTION, INTERACTIVE, BASIC};
    public enum BlockType {
        ROOT, FOR, IF
    }

    public final Program program;
    public HashMap<Node, Exception> errors = new HashMap<>();
    public final ResolutionMode mode;

    /** Will be null when validating symbols! */
    public final FunctionImplementation functionImplementation;

    private int localSymbolCount;
    private Block currentBlock;
    public HashSet<GlobalSymbol> dependencies = new HashSet<>();
    private final ProgramValidationContext programValidationContext;
    private final ClassValidationContext classValidationContext;

    private FunctionValidationContext(ProgramValidationContext programValidationContext, ClassValidationContext classValidationContext, ResolutionMode mode, FunctionImplementation functionImplementation) {
        this.programValidationContext = programValidationContext;
        this.classValidationContext = classValidationContext;
        this.program = programValidationContext.program;
        this.mode = (program.legacyMode && functionImplementation == program.main) ? ResolutionMode.BASIC : mode;
        this.functionImplementation = functionImplementation;
        startBlock(BlockType.ROOT);
        if (functionImplementation != null) {
            for (int i = 0; i < functionImplementation.parameterNames.length; i++) {
                currentBlock.localSymbols.put(functionImplementation.parameterNames[i], new LocalSymbol(localSymbolCount++, functionImplementation.getType().getParameterType(i), false));
            }
        }
    }

    public FunctionValidationContext(ProgramValidationContext programValidationContext, ResolutionMode mode, FunctionImplementation functionImplementation) {
        this (programValidationContext, null, mode, functionImplementation);
    }

    public FunctionValidationContext(ClassValidationContext classValidationContext, FunctionImplementation functionImplementation) {
        this (classValidationContext.programValidationContext, classValidationContext, ResolutionMode.FUNCTION, functionImplementation);
    }


    public void startBlock(BlockType type) {
        currentBlock = new Block(currentBlock, type);
    }

    public void endBlock(BlockType type) {
        if (type != currentBlock.type) {
            throw new RuntimeException((currentBlock.type == BlockType.FOR ? "NEXT" : ("END " + currentBlock.type))
                    + " expected.");
        }
        currentBlock = currentBlock.parent;
    }

    public ResolvedSymbol declare(String name, Type type, boolean constant) {
        if (mode == ResolutionMode.FUNCTION) {
            if (currentBlock.localSymbols.containsKey(name)) {
                throw new RuntimeException("Local variable named '" + name + "' already exists");
            }
            LocalSymbol result = new LocalSymbol(localSymbolCount++, type, constant);
            currentBlock.localSymbols.put(name, result);
            return result;
        }
        ResolvedSymbol resolved = currentBlock.get(name);
        if (resolved != null) {
            return resolved;
        }
        if (classValidationContext != null) {
            resolved = classValidationContext.resolve(name);
            if (resolved != null) {
                return resolved;
            }
        }
        GlobalSymbol symbol = program.getSymbol(name);
        switch (mode) {
            case BASIC:
                if (symbol != null
                    && (symbol.scope == GlobalSymbol.Scope.PERSISTENT
                    || symbol.scope == GlobalSymbol.Scope.BUILTIN)) {
                    dependencies.add(symbol);
                    return symbol;
                }
                return new DynamicSymbol(name, mode);

            case INTERACTIVE:
                if (symbol != null) {
                    return symbol;
                }
                return new DynamicSymbol(name, mode);

            default:
                throw new IllegalStateException("Unrecognized mode " + mode);
        }
    }

    public ResolvedSymbol resolve(String name) {

        // Block level

        ResolvedSymbol resolved = currentBlock.get(name);
        if (resolved != null) {
            return resolved;
        }

        // Members

        if (classValidationContext != null) {
            resolved = classValidationContext.resolve(name);
            if (resolved != null) {
                return resolved;
            }
        }

        // Globals

        GlobalSymbol symbol = programValidationContext.resolve(name);  // Checks for cyclic dependencies.
        switch (mode) {
            case BASIC:
                if (symbol != null
                    && (symbol.scope == GlobalSymbol.Scope.PERSISTENT
                    || symbol.scope == GlobalSymbol.Scope.BUILTIN)) {
                    dependencies.add(symbol);
                    return symbol;
                }
                return new DynamicSymbol(name, mode);

            case INTERACTIVE:
                if (symbol != null) {
                    dependencies.add(symbol);
                    return symbol;
                }
                throw new RuntimeException("Variable not found: \"" + name + "\"");

            case FUNCTION:
                if (symbol == null || symbol.scope == GlobalSymbol.Scope.TRANSIENT) {
                    throw new RuntimeException("Variable not found: \"" + name + "\"");
                }
                if (symbol.initializer != null) {
                    dependencies.add(symbol);
                }
                return symbol;
            default:
                throw new IllegalStateException("Unrecognized mode " + mode);
        }
    }

    public void addError(Node node, Exception e) {
        errors.put(node, e);
    }

    public int getLocalVariableCount() {
        return localSymbolCount;
    }


    private class Block {
        final Block parent;
        final BlockType type;
        private final HashMap<String, LocalSymbol> localSymbols = new HashMap<>();

        Block(Block parent, BlockType blockType) {
            this.parent = parent;
            this.type = blockType;
        }

        LocalSymbol get(String name) {
            LocalSymbol result = localSymbols.get(name);
            return (result == null && parent != null) ? parent.get(name) : result;
        }
    }
}
