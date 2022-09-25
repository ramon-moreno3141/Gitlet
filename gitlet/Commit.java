package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/** Commit class for commits.
 * @author Ramon Moreno*/
public class Commit implements Serializable {
    /** A Commit constructor which creates a commit with an
     * initial message MESSAGE, a HashMap of tracked files (name --> hashcode) FILES, an array of
     * parent commit HashCodes PARENTS, and a boolean INITIAL that is set
     * to true iff this is the initial commit of a Gitlet repository.
     */
    public Commit(HashMap<String, String> files,
                  String[] parents, String message, boolean initial) {
        _myMessage = message;
        _myFiles = files;
        _parents = parents;

        Date currentDate;
        if (initial) {
            timeOfCommit = "Wed Dec 31 16:00:00 1969 -0800";
        } else {
            currentDate = new Date();
            timeOfCommit = FORMAT.format(currentDate) + " -0800";
        }
        _myUID = hashThisCommit();
    }

    /** Turns a commit into a hashcode.
     * @return String */
    public String hashThisCommit() {
        String myFiles;
        String[] myParents;

        if (_myFiles == null) {
            myFiles = "";
        } else {
            myFiles = _myFiles.toString();
        }

        if (_parents == null) {
            myParents = new String[] {};
        } else {
            myParents = _parents;
        }
        return Utils.sha1(_myMessage, myFiles,
                Arrays.toString(myParents), timeOfCommit);
    }

    /** Returns this commit's message. */
    public String getMyMessage() {
        return _myMessage;
    }

    /** Gets the time of the commit.
     * @return String */
    public String getTimeOfCommit() {
        return timeOfCommit;
    }
    /** Gets the commit's files.
     * @return HashMap*/
    public HashMap<String, String> getMyFiles() {
        if (_myFiles == null) {
            return new HashMap<>();
        }
        return _myFiles;
    }
    /** Gets the commit's first parent.
     * @return String */
    public String parent1UID() {
        if (_parents != null) {
            return _parents[0];
        }
        return null;
    }
    /** Gets the Array of all parent hashcodes.
     * @return String[] */
    public String[] getAllParents() {
        return _parents;
    }
    /** Gets my UID.
     * @return String */
    public String getMyUID() {
        return _myUID;
    }
    /** My message. */
    private String _myMessage;
    /** My files. */
    private HashMap<String, String> _myFiles;
    /** My parents. */
    private String[] _parents;
    /** My time of commit. */
    private String timeOfCommit;
    /** My UID. */
    private String _myUID;
    /** Date Formatter. */
    private static final SimpleDateFormat FORMAT =
            new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy");

}
