
 

import java.util.Scanner;
import java.util.Hashtable;

public class Item {

    static class NoItemException extends Exception {}
    /**
     * The primary name by which the item is called in the file
     * probably wont have any space in it
     */
    private String primaryName;
    /**
     * pertains to how heavy that this item is
     * if you have too much weight, you will not be able to carry anymore
     */
    private int weight;
    /**
     * hashtable that stores messages pertaining to verbs done to this object.
     */
    private Hashtable<String,String> messages;

    /**
     * constructor for the Item Class
     * @param s Scanner used in reading file to construct items
     * @throws NoItemException if no item found in file
     * @throws Dungeon.IllegalDungeonFormatException if file being read was constructed incorrectly
     */
    Item(Scanner s) throws NoItemException,
        Dungeon.IllegalDungeonFormatException {

        messages = new Hashtable<String,String>();

        // Read item name.
        primaryName = s.nextLine();
        if (primaryName.equals(Dungeon.TOP_LEVEL_DELIM)) {
            throw new NoItemException();
        }

        // Read item weight.
        weight = Integer.valueOf(s.nextLine());

        // Read and parse verbs lines, as long as there are more.
        String verbLine = s.nextLine();
        while (!verbLine.equals(Dungeon.SECOND_LEVEL_DELIM)) {
            if (verbLine.equals(Dungeon.TOP_LEVEL_DELIM)) {
                throw new Dungeon.IllegalDungeonFormatException("No '" +
                    Dungeon.SECOND_LEVEL_DELIM + "' after item.");
            }
            String[] verbParts = verbLine.split(":");
            messages.put(verbParts[0],verbParts[1]);
            
            verbLine = s.nextLine();
        }
    }
    /**
     * Returns a true or a false for if this Item is also called by something else
     * @param name String of the items possible other name
     * @return boolean for if it called by that name or not
     */
    boolean goesBy(String name) {
        // could have other aliases
        return this.primaryName.equals(name);
    }
    /**
     * primary name by which the Item goes by in the File
     * @return primaryName the name of the Item
     */
    String getPrimaryName() { return primaryName; }
    /**
     * gets the message of pertaining to action being used on Item
     * @param verb String of action being done to Item
     * @return String message of what happens to Item when Verb is done to it
     */
    public String getMessageForVerb(String verb) {
        return messages.get(verb);
    }
    /**
     * no idea what this does, as we already have a "getPrimaryName" method
     * the only difference is that it is public, so it can probably be used by lots of classes without interfering with the Item
     * itself. or maybe it just makes the Item turn into a String for storage purposes in an array ¯\_(ツ)_/¯ 
     * @return primaryName the Primary name of the Iten
     */
    public String toString() {
        return primaryName;
    }
}
