package org.kobjects.typesystem;

public class FunctionType implements Type {
    private final Type returnType;
    private Type[] parameterTypes;

    public FunctionType(Type returnType, Type... parameterTypes) {
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    public Type getReturnType() {
        return returnType;
    }

    public Type getParameterType(int index) {
        return parameterTypes[index];
    }

    public int getParameterCount() {
        return parameterTypes.length;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameterTypes[i].toString());
        }
        sb.append(") -> ");
        sb.append(returnType.toString().substring(0, 1));
        return sb.toString();
    }

}