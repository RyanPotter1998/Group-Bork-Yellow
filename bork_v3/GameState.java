
 

import java.util.Scanner;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
/**
 * The GameState class holds everything about the current state of the game in it. it is possibly easier to envision the GameState class 
 * as the player itself, as it holds the current inventory, the players current health and current score.
 *
 */

public class GameState {

    public static class IllegalSaveFormatException extends Exception {
        public IllegalSaveFormatException(String e) {
            super(e);
        }
    }
    /**
     * the Default Save file that the Bork will be saved to if something goes wrong in the saving process, or it cannot get its file
     * from the original bork
     */
    static String DEFAULT_SAVE_FILE = "bork_save";
    /**
     * the extension that will be put at the end of a save file when saved
     */
    static String SAVE_FILE_EXTENSION = ".sav";
    /**
     * the version of bork that the sav file has to be in order to be read, must be in format
     * "Bork v #.0 save data" verbatim to be read
     * currently v3.0
     */
    static String SAVE_FILE_VERSION = "Bork v3.0 save data";
    /**
     * part of file that talks about the Adventurer, like his inventory of his current room.
     * if absent, file will not load properly and crash the game
     */
    static String ADVENTURER_MARKER = "Adventurer:";
    /**
     * The header below "Adventurer:" in the file that talks about the adventurers current room
     * things below it will contain Room data to be parse through in the Room Class
     */
    static String CURRENT_ROOM_LEADER = "Current room: ";
    /**
     * The Header in the Adventurer section that contains the player's current Inventory
     * things below it will contain Item data to be parsed through in the Item Class
     * and will be put into the players inventory
     */
    static String INVENTORY_LEADER = "Inventory: ";
    /**
     * can only be on "instance" at a time, sort of like the update variable for the whole game
     */
    private static GameState theInstance;
    /**
     * Class that contains everything you are going to be encountering throughout the game
     * contains all the Rooms, Exits, Items and Monsters found throughout the game
     */
    private Dungeon dungeon;
    /**
     * "Gee I wonder what an Inventory could be?"
     * an arrayList that contains all the objects that the Adventurer currently has on him
     */
    private ArrayList<Item> inventory;
    /**
     * What do you think it means?
     * Room Class that is the room you are currently in
     */
    private Room adventurersCurrentRoom;
    /**
     * number that when it reaches zero you die
     */
    private int health;
    /**
     * the bigger the number, the gooder you are at the game
     */
    private int score;
    /**
     *  updates the game so that it is its current state
     *  @return TheInstance variable, which is the GameState
     */
    static synchronized GameState instance() {
        if (theInstance == null) {
            theInstance = new GameState();
        }
        return theInstance;
    }
    /**
     * Constructor for GameState
     * @param none
     * 
     */
    private GameState() {
        inventory = new ArrayList<Item>();
    }
    /**
     * The "Load Game" function of the game. only called when loading a ".sav" file
     * @param filename String that is the filename
     * @throws IllegalSaveFormatException if save file not compatible
     * @throws IllegalSaveFormatException if there is no filename Leader after the version indicator
     * @throws IllegalSaveFormatException if there is an item name that does not exist in the dungeon
     */
    void restore(String filename) throws FileNotFoundException,
        IllegalSaveFormatException, Dungeon.IllegalDungeonFormatException {

        Scanner s = new Scanner(new FileReader(filename));

        if (!s.nextLine().equals(SAVE_FILE_VERSION)) {
            throw new IllegalSaveFormatException("Save file not compatible.");
        }

        String dungeonFileLine = s.nextLine();

        if (!dungeonFileLine.startsWith(Dungeon.FILENAME_LEADER)) {
            throw new IllegalSaveFormatException("No '" +
                Dungeon.FILENAME_LEADER + 
                "' after version indicator.");
        }

        dungeon = new Dungeon(dungeonFileLine.substring(
            Dungeon.FILENAME_LEADER.length()), false);
        dungeon.restoreState(s);

        s.nextLine();  // Throw away "Adventurer:".
        String currentRoomLine = s.nextLine();
        adventurersCurrentRoom = dungeon.getRoom(
            currentRoomLine.substring(CURRENT_ROOM_LEADER.length()));
        if (s.hasNext()) {
            String inventoryList = s.nextLine().substring(
                INVENTORY_LEADER.length());
            String[] inventoryItems = inventoryList.split(",");
            for (String itemName : inventoryItems) {
                try {
                    addToInventory(dungeon.getItem(itemName));
                } catch (Item.NoItemException e) {
                    throw new IllegalSaveFormatException("No such item '" +
                        itemName + "'");
                }
            }
        }
    }
    /**
     * the default "Save Game" function of bork
     * if something goes wrong with the normal store method, this will store the save data in file called "bork_save.sav"
     * throws IOException if something goes wrong with the reading and writing of files
     */
    void store() throws IOException {
        store(DEFAULT_SAVE_FILE);
    }
    /**
     * the "Save Game" function of bork,
     * will write the current condition of the game to a ".sav" file named after the ".bork" file used
     * if game was loaded from a ".sav" file, said file will be overwritten.
     * @param saveName the name of the file used to save the game. named after the bork file used.
     * @throws IOException if something goes wrong with the reading and writing of files.
     */
    void store(String saveName) throws IOException {
        String filename = saveName + SAVE_FILE_EXTENSION;
        PrintWriter w = new PrintWriter(new FileWriter(filename));
        w.println(SAVE_FILE_VERSION);
        dungeon.storeState(w);
        w.println(ADVENTURER_MARKER);
        w.println(CURRENT_ROOM_LEADER + adventurersCurrentRoom.getTitle());
        if (inventory.size() > 0) {
            w.print(INVENTORY_LEADER);
            for (int i=0; i<inventory.size()-1; i++) {
                w.print(inventory.get(i).getPrimaryName() + ",");
            }
            w.println(inventory.get(inventory.size()-1).getPrimaryName());
        }
        w.close();
    }
    /**
     * if starting a new game, starts the character off in the designated entry of the dungeon
     * method called in the construction of a dungeon of a new game
     * @param dungeon the entire Dungeon class being read from the bork file
     */
    void initialize(Dungeon dungeon) {
        this.dungeon = dungeon;
        adventurersCurrentRoom = dungeon.getEntry();
    }
    /**
     * gets the names from all the items in the players current inventory
     * @returns ArrayList<String> returns all the items in the inventory as an ArrayList of Strings,
     */
    ArrayList<String> getInventoryNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (Item item : inventory) {
            names.add(item.getPrimaryName());
        }
        return names;
    }
    /**
     * adds items to player's inventory
     * @param item an Item to be put into the player's inventory
     */
    void addToInventory(Item item) /* throws TooHeavyException */ {
        inventory.add(item);
    }
    /**
     * removes items from player's inventory
     * @param item the Item that is to be removed from the inventory
     */
    void removeFromInventory(Item item) {
        inventory.remove(item);
    }
    /**
     * returns items in inventory and the room
     * @param name String of item being called, checking to see if it is in room or inventory
     * @return an item
     */
    Item getItemInVicinityNamed(String name) throws Item.NoItemException {

        // First, check inventory.
        for (Item item : inventory) {
            if (item.goesBy(name)) {
                return item;
            }
        }

        // Next, check room contents.
        for (Item item : adventurersCurrentRoom.getContents()) {
            if (item.goesBy(name)) {
                return item;
            }
        }

        throw new Item.NoItemException();
    }
    /**
     * gets an item from the inventory that goes by a certain name
     * @param name String for the item's name
     * @return item being called
     */
    Item getItemFromInventoryNamed(String name) throws Item.NoItemException {

        for (Item item : inventory) {
            if (item.goesBy(name)) {
                return item;
            }
        }
        throw new Item.NoItemException();
    }
    /**
     * gets the adventurer's (player's) current room
     * used in loading a sav game and whenever you need to know what room you are in
     * @return room you are currently in
     */
    Room getAdventurersCurrentRoom() {
        return adventurersCurrentRoom;
    }
    /**
     * sets the adventurer's (player's) current room
     * used in the "loading" a ".sav" file
     * @param room Room 
     */
    void setAdventurersCurrentRoom(Room room) {
        adventurersCurrentRoom = room;
    }
    /**
     * returns the dungeon of the game
     * @return the dungeon of the game
     */
    Dungeon getDungeon() {
        return dungeon;
    }
    /**
     * sets the adventurers current/starting health 
     * @param def Integer for the health
     */
    void setHealth(int def){
        this.health = def;
    }
    /**
     * get adventurers current health, probably should make it a sort of fraction for later like "2/5"
     */
    int getHealth(){
        return health;
    }
    /**
     * used to heal the player character, if a negative number is put in, can decrease it
     * @param amount integer to change health by.
     */
    void increaseHealth(int amount){
        this.health = health + amount;
    }
}
