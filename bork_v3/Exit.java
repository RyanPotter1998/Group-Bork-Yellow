
 

import java.util.Scanner;

public class Exit {

    class NoExitException extends Exception {}
    /**
     * direction that the Exit lies in the Room relative to the player
     * Usually denoted by a single Letter
     * u,d,n,e,s,w pertaing to cardinal directions as well as up and down
     */
    private String dir;
    /**
     * 2 variables that pertain to the room being left and entered, respectively
     */
    private Room src, dest;
    /**
     * constructor for the Exit class
     * @param dir String for the direction that the Exit will be in the Room in relation to the player
     * @param src Room being left through the exit. Exit is added to this Room's exit ArrayList
     * @param dest Room being entered through the exit
     * @see Room.addExit()
     * 
     */
    Exit(String dir, Room src, Room dest) {
        init();
        this.dir = dir;
        this.src = src;
        this.dest = dest;
        src.addExit(this);
    }

    /** Given a Scanner object positioned at the beginning of an "exit" file
        entry, read and return an Exit object representing it. 
        @param d The dungeon that contains this exit (so that Room objects 
        may be obtained.)
        @throws NoExitException The reader object is not positioned at the
        start of an exit entry. A side effect of this is the reader's cursor
        is now positioned one line past where it was.
        @throws IllegalDungeonFormatException A structural problem with the
        dungeon file itself, detected when trying to read this room.
     */
    Exit(Scanner s, Dungeon d) throws NoExitException,
        Dungeon.IllegalDungeonFormatException {

        init();
        String srcTitle = s.nextLine();
        if (srcTitle.equals(Dungeon.TOP_LEVEL_DELIM)) {
            throw new NoExitException();
        }
        src = d.getRoom(srcTitle);
        dir = s.nextLine();
        dest = d.getRoom(s.nextLine());
        
        // I'm an Exit object. Great. Add me as an exit to my source Room too,
        // though.
        src.addExit(this);

        // throw away delimiter
        if (!s.nextLine().equals(Dungeon.SECOND_LEVEL_DELIM)) {
            throw new Dungeon.IllegalDungeonFormatException("No '" +
                Dungeon.SECOND_LEVEL_DELIM + "' after exit.");
        }
    }

    /**
     * Common object initialization tasks.
     * does absolutely nothing, it is THE most worthless piece in this entire program
     * it takes up space on my screen and in my computer
     * it probably cries itself to sleep at night because its so worthless
     * it wants to tell itself its special, but it knows it really isnt, as nobody calls him in the entirety of this code
     * stupid init
     * @param nothing because nobody calls it
     * @returns nothing because he is useless
     * @see other useless things, such as:
     * 
     */
    private void init() {
    }
    /**
     * returns a string that tells you direction this Exit is heading
     * @return String "You can go " + dir + " to " + dest.getTitle() + "."
     */
    String describe() {
        return "You can go " + dir + " to " + dest.getTitle() + ".";
    }
    /**
     * gets the direction of this Exit
     * @return String of direction
     */
    String getDir() { return dir; }
    /**
     * gets the room being left through exit
     * @return Room Source
     */
    Room getSrc() { return src; }
    /**
     * gets the room player is heading to through exit
     * @return Room Destination
     */
    Room getDest() { return dest; }
}
