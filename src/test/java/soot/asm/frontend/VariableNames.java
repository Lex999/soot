package soot.asm.frontend;

public class VariableNames {

    public int differentVarsSameIndex(int a, boolean b) {
        int result;
        if (b) {
            int c = 13;
            result = a + c;
        } else {
            int d = 14;
            result = a + d;
        }
        return result;
    }
}
