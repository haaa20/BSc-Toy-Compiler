public enum Operator {
    EQ(Group.COMP),
    LESS(Group.COMP),
    MORE(Group.COMP),
    LESSEQ(Group.COMP),
    MOREEQ(Group.COMP),
    PLUS(Group.INT),
    MINUS(Group.INT),
    MULTI(Group.INT),
    DIV(Group.INT),
    AND(Group.BOOL),
    OR(Group.BOOL),
    XOR(Group.BOOL)
    ;

    Group group;

    public enum Group {
        BOOL, INT, COMP;
    }

    Operator(Group group) {
        this.group = group;
    }

    public static Operator evaluate(String s) {
        return switch (s) {
            case "==" -> EQ;
            case "<" -> LESS;
            case ">" -> MORE;
            case "<=" -> LESSEQ;
            case ">=" -> MOREEQ;
            case "+" -> PLUS;
            case "-" -> MINUS;
            case "*" -> MULTI;
            case "/" -> DIV;
            case "&" -> AND;
            case "|" -> OR;
            case "^" -> XOR;
            default -> null;
        };
    }
}
