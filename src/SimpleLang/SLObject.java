package SimpleLang;

public class SLObject {
    private SLType type;
    private int value;

    public SLObject() {
        type = SLType.UNIT;
    }

    public SLObject(int value) {
        this.value = value;
        this.type = SLType.INT;
    }

    public SLObject(Boolean value) {
        this.type = SLType.BOOL;
        setBool(value);
    }

    public int getValue() {
        if (type != SLType.INT) {return 0;}
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Boolean getBool() {
        if (type != SLType.BOOL) {return false;}
        return (value > 0);
    }

    public void setBool(Boolean bool) {
        if (bool) {
            value = 1;
        }
        else {
            value = 0;
        }
    }
}
