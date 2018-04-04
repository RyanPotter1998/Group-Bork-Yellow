
 

import java.util.ArrayList;
import java.util.Scanner;
import java.io.IOException;
import java.io.PrintWriter;

public class Room {

    class NoRoomException extends Exception {}
    /**
     * the part of the file being read that indicates the contents of the room
     * everything after this point will contain data to be parsed and put into the Room class
     */
    static String CONTENTS_STARTER = "Contents: ";
    /**
     * Name of the Room
     */
    private String title;
    /**
     * Description of the Room
     */
    private String desc;
    /**
     * Says whether or not you have been in the room
     */
    private boolean beenHere;
    /**
     * Arraylist that contains all the Items in the Room
     */
    private ArrayList<Item> contents;
    /**
     * ArrayList that contains all the SCARY MONSTERS OH NO!!!!!
     */
    private ArrayList</*Monster*/String> monsters;
    /**
     * ArrayList that contains all the exits in the room
     */
    private ArrayList<Exit> exits;
    /**
     * constructor for Room class
     * @param title String title of the room
     */
    Room(String title) {
        init();
        this.title = title;
    }
    /**
     * constructor used for when starting to read a file
     * @param s Scanner used for Scanning the File being read from the dungeon class
     * @param d Dungeon for which the room will belong
     * @throws NoRoomException self explanitory, thrown if no room is found in the reading of a file
     */
    Room(Scanner s, Dungeon d) throws NoRoomException,
        Dungeon.IllegalDungeonFormatException {

        this(s, d, true);
    }

    /** Given a Scanner object positioned at the beginning of a "room" file
        entry, read and return a Room object representing it. 
        @param d The containing {@link edu.umw.stephen.bork.Dungeon} object, 
        necessary to retrieve {@link edu.umw.stephen.bork.Item} objects.
        @param initState should items listed for this room be added to it?
        @throws NoRoomException The reader object is not positioned at the
        start of a room entry. A side effect of this is the reader's cursor
        is now positioned one line past where it was.
        @throws IllegalDungeonFormatException A structural problem with the
        dungeon file itself, detected when trying to read this room.
     */
    Room(Scanner s, Dungeon d, boolean initState) throws NoRoomException,
        Dungeon.IllegalDungeonFormatException {

        init();
        title = s.nextLine();
        desc = "";
        if (title.equals(Dungeon.TOP_LEVEL_DELIM)) {
            throw new NoRoomException();
        }
        
        String lineOfDesc = s.nextLine();
        while (!lineOfDesc.equals(Dungeon.SECOND_LEVEL_DELIM) &&
               !lineOfDesc.equals(Dungeon.TOP_LEVEL_DELIM)) {

            if (lineOfDesc.startsWith(CONTENTS_STARTER)) {
                String itemsList = lineOfDesc.substring(CONTENTS_STARTER.length());
                String[] itemNames = itemsList.split(",");
                for (String itemName : itemNames) {
                    try {
                        if (initState) {
                            add(d.getItem(itemName));
                        }
                    } catch (Item.NoItemException e) {
                        throw new Dungeon.IllegalDungeonFormatException(
                            "No such item '" + itemName + "'");
                    }
                }
            } else {
                desc += lineOfDesc + "\n";
            }
            lineOfDesc = s.nextLine();
        }

        // throw away delimiter
        if (!lineOfDesc.equals(Dungeon.SECOND_LEVEL_DELIM)) {
            throw new Dungeon.IllegalDungeonFormatException("No '" +
                Dungeon.SECOND_LEVEL_DELIM + "' after room.");
        }
    }

    /** 
     * Common object initialization tasks.
     */
    private void init() {
        contents = new ArrayList<Item>();
        exits = new ArrayList<Exit>();
        beenHere = false;
    }
    /**
     * Returns title of Room
     * @return String title of Room
     */
    String getTitle() { return title; }
    /**
     * Sets the Description of the Room
     * @param String description of Room
     */
    void setDesc(String desc) { this.desc = desc; }
    
    /**
     * Store the current (changeable) state of this room to the writer
     * passed.
     * @param w PrintWriter used to Write to a save File
     * @throws IOException
     */
    void storeState(PrintWriter w) throws IOException {
        w.println(title + ":");
        w.println("beenHere=" + beenHere);
        if (contents.size() > 0) {
            w.print(CONTENTS_STARTER);
            for (int i=0; i<contents.size()-1; i++) {
                w.print(contents.get(i).getPrimaryName() + ",");
            }
            w.println(contents.get(contents.size()-1).getPrimaryName());
        }
        w.println(Dungeon.SECOND_LEVEL_DELIM);
    }
    /**
     * The "Load game" function, pertaining to the room portion of the ".sav" file
     * @param s Scanner used to read the file being passed through the Dungeon d
     * @param d Dungeon to which the room belongs to
     * @throws GameState.IllegalSaveFormatException if the file being read was not constructed correctly
     */
    void restoreState(Scanner s, Dungeon d) throws 
        GameState.IllegalSaveFormatException {

        String line = s.nextLine();
        if (!line.startsWith("beenHere")) {
            throw new GameState.IllegalSaveFormatException("No beenHere.");
        }
        beenHere = Boolean.valueOf(line.substring(line.indexOf("=")+1));

        line = s.nextLine();
        if (line.startsWith(CONTENTS_STARTER)) {
            String itemsList = line.substring(CONTENTS_STARTER.length());
            String[] itemNames = itemsList.split(",");
            for (String itemName : itemNames) {
                try {
                    add(d.getItem(itemName));
                } catch (Item.NoItemException e) {
                    throw new GameState.IllegalSaveFormatException(
                        "No such item '" + itemName + "'");
                }
            }
            s.nextLine();  // Consume "---".
        }
    }
    /**
     * Returns the Room's description
     * description includes how the room looks, any items inside it, as well as all the exits in the room
     * usually used in conjuction with the "describe" command, and when the Room is entered for the first time
     * @return String of the description of the Room
     */
    public String describe() {
        String description;
        if (beenHere) {
            description = title;
        } else {
            description = title + "\n" + desc;
        }
        for (Item item : contents) {
            description += "\nThere is a " + item.getPrimaryName() + " here.";
        }
        if (contents.size() > 0) { description += "\n"; }
        if (!beenHere) {
            for (Exit exit : exits) {
                description += "\n" + exit.describe();
            }
        }
        beenHere = true;
        return description;
    }
    /**
     * returns the Room to which you are going
     * @param dir String to the exit you are going to
     * @return Room from exit destination
     * @return null if you cannot leave
     */
    public Room leaveBy(String dir) {
        for (Exit exit : exits) {
            if (exit.getDir().equals(dir)) {
                return exit.getDest();
            }
        }
        return null;
    }
    /**
     * Adds exit to the Room's exit array, the directions by which you will leave
     * used in the construction of Rooms in a Dungeon
     * @param exit Exits by which you may leave the room being put into the Room's exit array
     */
    void addExit(Exit exit) {
        exits.add(exit);
    }
    /**
     * Adds items to the Room
     * used in the Construction of a Room class to house initial items in new game
     * also used when an item is dropped into a room
     * @param item Item to be added to the Room's contents array
     */
    void add(Item item) {
        contents.add(item);
    }
    /**
     * removes items from a room
     * normally used in the picking up of items to be put in inventory
     * probably could be used in some kind of event when an object is changed in someway
     * @param item item to be taken out of Room's contents array
     */
    void remove(Item item) {
        contents.remove(item);
    }
    /**
     * gets an item in a room based on a name
     * used when item is being picked up from Room, or being seen, probably with some sort of "look at" command
     * @param name String name of Item being called
     * @return item being called on by name
     * @throws Item.NoItemException if no item found
     */
    Item getItemNamed(String name) throws Item.NoItemException {
        for (Item item : contents) {
            if (item.goesBy(name)) {
                return item;
            }
        }
        throw new Item.NoItemException();
    }
    /**
     * returns the Room's arrayList of items, Contents
     * @return ArrayList<Items> contents of the room
     */
    ArrayList<Item> getContents() {
        return contents;
    }
    /**
     * adds a Monster into Room
     */
    void addMonster(){
    }
    /**
     * Forcibly evicts a Monster from the Room
     */
    void removeMonster(){
    }
    /**
     * gets you are monster from the Room's monster ArrayList, not sure why you would want one, though
     * @return Monster Class object, but right now its a void that doesnt return everything because there is no monster class
     */
    void getMonster(){
    }
}
