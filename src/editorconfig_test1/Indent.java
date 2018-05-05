package editorconfig_test1;


/**
 * Класс, хранящий кол-во пробелов и кол-во табуляций в отступе
 * 
 * @author akropon
 */
public class Indent {
    public int spaces;
    public int tabs;

    public Indent() {
        spaces = 0;
        tabs = 0;
    }

    public Indent(int spaces, int tabs) {
        this.spaces = spaces;
        this.tabs = tabs;
    }
    
    public Indent sub(Indent what) {
        return new Indent(spaces-what.spaces, tabs-what.tabs);
    }
    
    public Indent add(Indent what) {
        return new Indent(spaces+what.spaces, tabs+what.tabs);
    }
    
    public Indent getInv() {
        return new Indent(-spaces, -tabs);
    }
    
    public Indent getClone() {
        return new Indent(spaces, tabs);
    }
    
    public boolean equals(Indent indent) {
        return spaces==indent.spaces && tabs==indent.tabs;
    }

    @Override
    public String toString() {
        return String.format("[%2d,%2d]", spaces, tabs);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Indent) 
            return equals((Indent)obj);
        else
            return false;
    }
}
