package ngoy.internal.parser;

public class ForOfDef {
    // could be 'let' or 'var' to infer the type
    public final String itemType;
    public final String itemName;
    public final String listName;

    public ForOfDef(String itemType, String itemName, String listName) {
        this.itemType = itemType;
        this.itemName = itemName;
        this.listName = listName;
    }
}
