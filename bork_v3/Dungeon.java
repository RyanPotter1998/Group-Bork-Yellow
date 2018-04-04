
 

import java.util.Hashtable;
import java.util.Scanner;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;

public class Dungeon {

    public static class IllegalDungeonFormatException extends Exception {
        public IllegalDungeonFormatException(String e) {
            super(e);
        }
    }

    // Variables relating to both dungeon file and game state storage.
    /**
     * part of file that indictes the transition from one section to be parsed to another
     * like from Room to Exit
     */
    public static String TOP_LEVEL_DELIM = "===";
    /**
     * part of file being read that inicates the end of one class and the beginning of another class of the same type
     * like from Room to Room
     */
    public static String SECOND_LEVEL_DELIM = "---";

    // Variables relating to dungeon file (.bork) storage.
    /**
     * Beginning of the Room Section of the file (.bork) being read
     */
    public static String ROOMS_MARKER = "Rooms:";
    /**
     * Beginning of the Exit Section of the file (.bork) being read
     */
    public static String EXITS_MARKER = "Exits:";
    /**
     * Beginning of the Item Section of the file (.bork) being read
     */
    public static String ITEMS_MARKER = "Items:";
    
    // Variables relating to game state (.sav) storage.
    /**
     * tells the file (.sav) which Dungeon file (.bork) to read to re-construct the dungeon
     */
    static String FILENAME_LEADER = "Dungeon file: ";
    /**
     * beginning of the section in the file (.sav) that changes the stateof rooms
     * like whether it has been visited and what items are currently in it.
     */
    static String ROOM_STATES_MARKER = "Room states:";
    /**
     * The name of the Dungeon
     */
    private String name;
    /**
     * The Room that functions as the "seed" where all other rooms will be built around
     * also the room that the player will be starting in in a new game.
     */
    private Room entry;
    /**
     * Hashtable that contains all the Rooms in the Dungeon and the names of said rooms
     */
    private Hashtable<String,Room> rooms;
    /**
     * Hashtable that contains all the names in the Dungeon and their names
     */
    private Hashtable<String,Item> items;
    /**
     * Name of the file being read
     */
    private String filename;
    /**
     * constructor for Dungeon Class
     * @param name String to be used as Dungeon's name
     * @param entry Room used as the start of the dungeon from which the rest of the dungeon will be built around
     */
    Dungeon(String name, Room entry) {
        init();
        this.filename = null;    // null indicates not hydrated from file.
        this.name = name;
        this.entry = entry;
        rooms = new Hashtable<String,Room>();
    }

    /**
     * Read from the .bork filename passed, and instantiate a Dungeon object
     * based on it.
     * @param filename the name of the file being used
     * @throws FileNotFoundException if there isn't a file to be found, ya dingus
     * @throws IllegalDungeonFormatException if the file being read was constructed incorrectly
     */
    public Dungeon(String filename) throws FileNotFoundException, 
        IllegalDungeonFormatException {

        this(filename, true);
    }

    /**
     * Read from the .bork filename passed, and instantiate a Dungeon object
     * based on it, including (possibly) the items in their original locations.
     * @param filename String of the File being read
     * @param initState boolean used to instantiate the Dungeon
     * @throws FileNotFoundException if there isnt a file to be read, you cannot play the game
     * @throws IllegalDungeonFormatException if the file being read has been constructed incorrectly
     */
    public Dungeon(String filename, boolean initState) 
        throws FileNotFoundException, IllegalDungeonFormatException {

        init();
        this.filename = filename;

        Scanner s = new Scanner(new FileReader(filename));
        name = s.nextLine();

        s.nextLine();   // Throw away version indicator.

        // Throw away delimiter.
        if (!s.nextLine().equals(TOP_LEVEL_DELIM)) {
            throw new IllegalDungeonFormatException("No '" +
                TOP_LEVEL_DELIM + "' after version indicator.");
        }

        // Throw away Items starter.
        if (!s.nextLine().equals(ITEMS_MARKER)) {
            throw new IllegalDungeonFormatException("No '" +
                ITEMS_MARKER + "' line where expected.");
        }

        try {
            // Instantiate items.
            while (true) {
                add(new Item(s));
            }
        } catch (Item.NoItemException e) {  /* end of items */ }

        // Throw away Rooms starter.
        if (!s.nextLine().equals(ROOMS_MARKER)) {
            throw new IllegalDungeonFormatException("No '" +
                ROOMS_MARKER + "' line where expected.");
        }

        try {
            // Instantiate and add first room (the entry).
            entry = new Room(s, this, initState);
            add(entry);

            // Instantiate and add other rooms.
            while (true) {
                add(new Room(s, this, initState));
            }
        } catch (Room.NoRoomException e) {  /* end of rooms */ }

        // Throw away Exits starter.
        if (!s.nextLine().equals(EXITS_MARKER)) {
            throw new IllegalDungeonFormatException("No '" +
                EXITS_MARKER + "' line where expected.");
        }

        try {
            // Instantiate exits.
            while (true) {
                Exit exit = new Exit(s, this);
            }
        } catch (Exit.NoExitException e) {  /* end of exits */ }

        s.close();
    }
    
    /** 
     * Common object initialization tasks, regardless of which constructor
     * is used
     */
    private void init() {
        rooms = new Hashtable<String,Room>();
        items = new Hashtable<String,Item>();
    }

    /**
     * Store the current (changeable) state of this dungeon to the writer
     * passed.
     * @param w Printwriter that will write to the .sav file
     * @throws IOException
     */
    void storeState(PrintWriter w) throws IOException {
        w.println(FILENAME_LEADER + getFilename());
        w.println(ROOM_STATES_MARKER);
        for (Room room : rooms.values()) {
            room.storeState(w);
        }
        w.println(TOP_LEVEL_DELIM);
    }

    /**
     * Restore the (changeable) state of this dungeon to that reflected in the
     * reader passed.
     * @param s Scanner object being used to scan the ".sav" file being used to "load" a game
     * @throws GameState.IllegalSaveFormatException if the ".sav" file is constructed incorrectly
     */
    void restoreState(Scanner s) throws GameState.IllegalSaveFormatException {

        // Note: the filename has already been read at this point.
        
        if (!s.nextLine().equals(ROOM_STATES_MARKER)) {
            throw new GameState.IllegalSaveFormatException("No '" +
                ROOM_STATES_MARKER + "' after dungeon filename in save file.");
        }

        String roomName = s.nextLine();
        while (!roomName.equals(TOP_LEVEL_DELIM)) {
            getRoom(roomName.substring(0,roomName.length()-1)).
                restoreState(s, this);
            roomName = s.nextLine();
        }
    }
    /**
     * gets the entry room of the dungeon, the "seed" from which the rest of the dungeon is built
     * @return The dungeon's entrance
     */
    public Room getEntry() { return entry; }
    /**
     * Gets the name of the Dungeon
     * @return The name of the Dungeon
     */
    public String getName() { return name; }
    /**
     * Gets the name of the File being used for the construction of the Dungeon
     * @return String of name of File
     */
    public String getFilename() { return filename; }
    /**
     * adds a room to a the Dungeon's rooms Hashtable
     * called in the construction of the Dungeon
     * @param room Room being added to rooms HashTable
     */
    public void add(Room room) { rooms.put(room.getTitle(),room); }
    /**
     * Adds an Item to the Dungeon's items Hashtable
     * called in the construction of the Dungeon
     * @param item Item being added to items Hashtable
     */
    public void add(Item item) { items.put(item.getPrimaryName(),item); }
    /**
     * Gets a room based on the Room's name
     * @param roomTitle String name of the Room being called
     * @return Room being called
     */
    public Room getRoom(String roomTitle) {
        return rooms.get(roomTitle);
    }

    /**
     * Get the Item object whose primary name is passed. This has nothing to
     * do with where the Adventurer might be, or what's in his/her inventory,
     * etc.
     */
    public Item getItem(String primaryItemName) throws Item.NoItemException {
        
        if (items.get(primaryItemName) == null) {
            throw new Item.NoItemException();
        }
        return items.get(primaryItemName);
    }
}
