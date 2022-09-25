package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;

/** Repository class for gitlet. Controls gitlet's commands.
 * @author Ramon Moreno
 */
public class Repository implements Serializable {

    /** Initializes a new gitlet repository in the current directory. */
    public Repository() {
        Commit initialCommit = new Commit(null, null, "initial commit", true);
        File gitletDir = new File(".gitlet");
        gitletDir.mkdir();
        File commitDir = new File(".gitlet/Commits");
        commitDir.mkdir();
        File blobDir = new File(".gitlet/Blobs");
        blobDir.mkdir();

        File commitFile = new File(".gitlet/Commits/"
                + initialCommit.getMyUID());
        Utils.writeContents(commitFile, Utils.serialize(initialCommit));

        _currentBranch = "master";
        _stagedForAdd = new HashMap<>();
        _branchHeads = new HashMap<>();
        _stagedForRM = new HashSet<>();
        _branchHeads.put("master", initialCommit.getMyUID());
    }

    /** Adds the file in the working directory by the name of FILENAME to the
     * staging area. */
    public void add(String fileName) {
        File thisFile = new File(fileName);

        if (!thisFile.exists()) {
            throw error("File does not exist.");
        }

        String fileHashCode = Utils.sha1(Utils.readContentsAsString(thisFile));

        Commit latestCommit = uidToACommit(headCommitUID());
        //commitFiles maps file names to file hashcodes
        HashMap<String, String> commitFiles = latestCommit.getMyFiles();
        File blobFile = new File(".gitlet/Blobs/" + fileHashCode);

        if (commitFiles.size() == 0 || !commitFiles.containsKey(fileName)
                || !fileHashCode.equals(commitFiles.get(fileName))) {
            if (_stagedForAdd.containsKey(fileName)) {
                File blob = new File(".gitlet/Blobs/"
                        + _stagedForAdd.get(fileName));
                blob.delete();
            }

            _stagedForAdd.put(fileName, fileHashCode);
            String thisFileContents = readContentsAsString(thisFile);
            writeContents(blobFile, thisFileContents);
        } else if (_stagedForAdd.containsKey(fileName)) {
            File blob = new File(".gitlet/Blobs/"
                    + _stagedForAdd.get(fileName));
            blob.delete();
            _stagedForAdd.remove(fileName);
        }
        _stagedForRM.remove(fileName);
    }

    /** Creates a commit or snapshot of the files in the staging area with
     * the message MESSAGE. */
    public void commit(String message) {
        if (message.trim().equals("")) {
            throw error("Please enter a commit message.");
        }
        Commit latestCommit = uidToACommit(headCommitUID());
        HashMap<String, String> filesTracked = latestCommit.getMyFiles();

        if (_stagedForAdd.size() == 0 && _stagedForRM.size() == 0) {
            throw error("No changes added to the commit.");
        } else {
            for (String name : _stagedForAdd.keySet()) {
                filesTracked.put(name, _stagedForAdd.get(name));
            }
            for (String name : _stagedForRM) {
                filesTracked.remove(name);
            }
        }
        String[] myParent = {latestCommit.getMyUID()};
        Commit newCommit = new Commit(filesTracked, myParent, message, false);
        String newCommitID = newCommit.getMyUID();
        File newCommitFile = new File(".gitlet/Commits/" + newCommitID);
        writeObject(newCommitFile, newCommit);

        _stagedForAdd = new HashMap<>();
        _stagedForRM = new HashSet<>();
        _branchHeads.put(_currentBranch, newCommit.getMyUID());
    }

    /** Removes the file with name FILENAME from the working
     * directory and untracks it if it is tracked. It also
     * removes it from the staging area if it is staged. */
    public void rm(String fileName) {
        boolean staged = false;
        if (_stagedForAdd.containsKey(fileName)) {
            staged = true;
            File blob = new File(".gitlet/Blobs/"
                    + _stagedForAdd.get(fileName));
            blob.delete();
            _stagedForAdd.remove(fileName);
        }

        Commit latestCommit = uidToACommit(headCommitUID());
        HashMap<String, String> commitFiles = latestCommit.getMyFiles();
        boolean tracked = commitFiles.containsKey(fileName);

        if (!staged && !tracked) {
            throw error("No reason to remove the file.");
        }

        if (tracked) {
            _stagedForRM.add(fileName);
            File thisFile = new File(fileName);
            restrictedDelete(thisFile);
        }
    }

    /** Displays information about each commit backwards along the commit
     * tree until the initial commit, following the first parent commit
     * links. */
    public void log() {
        String headCommit = headCommitUID();
        while (headCommit != null) {
            Commit currentCommit = uidToACommit(headCommit);
            String[] parents = currentCommit.getAllParents();
            if (parents != null && parents.length == 2) {
                printM(headCommit);
            } else {
                print(headCommit);
            }
            headCommit = currentCommit.parent1UID();
        }
    }

    /** Prints the log out for merge commits with the uid ID. */
    public void printM(String id) {
        Commit currentCommit = uidToACommit(id);
        String[] parents = currentCommit.getAllParents();
        System.out.println("===");
        System.out.println("commit " + id);
        System.out.println("Merge: " + parents[0].substring(0, 7) + " "
                + parents[1].substring(0, 7));
        System.out.println("Date: " + currentCommit.getTimeOfCommit());
        System.out.println(currentCommit.getMyMessage());
        System.out.println();
    }

    /** Prints out a commit with the id ID. */
    public void print(String id) {
        Commit currentCommit = uidToACommit(id);
        System.out.println("===");
        System.out.println("commit " + id);
        System.out.println("Date: " + currentCommit.getTimeOfCommit());
        System.out.println(currentCommit.getMyMessage());
        System.out.println();
    }

    /** Prints out all commits ever made. */
    public void globalLog() {
        File allCommits = new File(".gitlet/Commits");
        List<String> commitHashes = plainFilenamesIn(allCommits);
        for (String commitID : commitHashes) {
            print(commitID);
        }
    }

    /** Prints out the HashCodes of all commits with the log message
     *  MESSAGE. */
    public void find(String message) {
        File allCommits = new File(".gitlet/Commits");
        List<String> commitHashes = plainFilenamesIn(allCommits);
        boolean found = false;
        for (String commitID : commitHashes) {
            Commit currentCommit = uidToACommit(commitID);
            if (currentCommit.getMyMessage().equals(message)) {
                System.out.println(commitID);
                found = true;
            }
        }
        if (!found) {
            throw error("Found no commit with that message.");
        }
    }

    /** Prints out information about tracked files, modified files,
     *  deleted files, and untracked files in the terminal. */
    public void status() {
        System.out.println("=== Branches ===");
        TreeSet<String> sortedBranches = new TreeSet<>(_branchHeads.keySet());
        for (String branch : sortedBranches) {
            if (branch.equals(_currentBranch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        TreeSet<String> sortedStage = new TreeSet<>(_stagedForAdd.keySet());
        for (String fileName : sortedStage) {
            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        TreeSet<String> sortedRemoved = new TreeSet<>(_stagedForRM);
        for (String fileName : sortedRemoved) {
            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        Commit latestCommit = uidToACommit(headCommitUID());
        HashMap<String, String> commitFiles = latestCommit.getMyFiles();
        List<String> workingFiles =
                plainFilenamesIn(System.getProperty("user.dir"));
        for (String fileName : workingFiles) {
            boolean staged = _stagedForAdd.containsKey(fileName);
            boolean tracked = commitFiles.containsKey(fileName);
            if (tracked && isDifferent(fileName, commitFiles) && !staged) {
                System.out.println(fileName + " (modified)");
            } else if (staged && isDifferent(fileName, _stagedForAdd)) {
                System.out.println(fileName + " (modified)");
            }
        }

        for (String fileName : sortedStage) {
            if (!exists(fileName)) {
                System.out.println(fileName + " (deleted)");
            }
        }

        TreeSet<String> trackedFiles = new TreeSet<>(commitFiles.keySet());
        for (String fileName : trackedFiles) {
            if (!_stagedForRM.contains(fileName) && !exists(fileName)) {
                System.out.println(fileName + " (deleted)");
            }
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        ArrayList<String> untrackedFiles = untrackedFiles();
        for (String fileName : untrackedFiles) {
            System.out.println(fileName);
        }
    }

    /** Takes in an array of Strings ARGS and either checks
     * out the file from the
     * previous commit or a specified commit and overwrites
     * the existing version
     * of the file in the working directory. Can also checkout
     * all files from the
     * head commit of another branch and overwrite those in the working
     * directory. */
    public void checkout(String[] args) {
        if (args.length == 2 && args[0].equals("--")) {
            String fileName = args[1];
            writeToFile(fileName, headCommitUID());
        } else if (args.length == 3 && args[1].equals("--")) {
            String commitID = args[0];
            String fileName = args[2];
            if (commitID.length() < UID_LENGTH) {
                commitID = fullSizeID(commitID);
            }
            writeToFile(fileName, commitID);
        } else if (args.length == 1) {
            String branchName = args[0];
            if (!_branchHeads.containsKey(branchName)) {
                throw error("No such branch exists.");
            } else if (branchName.equals(_currentBranch)) {
                throw error("No need to checkout the current branch.");
            } else {
                String commitID = _branchHeads.get(branchName);
                Commit headCommit = uidToACommit(commitID);
                HashMap<String, String> headFiles = headCommit.getMyFiles();
                ArrayList<String> untrackedFiles = untrackedFiles();

                for (String fileName : headFiles.keySet()) {
                    if (untrackedFiles.contains(fileName)) {
                        throw error("There is an untracked file in the way; "
                                + "delete it or add it first.");
                    }
                }

                Commit latestCommit = uidToACommit(headCommitUID());
                HashMap<String, String> previousFiles =
                        latestCommit.getMyFiles();

                for (String fileName : headFiles.keySet()) {
                    File checkoutFile = new File(".gitlet/Blobs/"
                            + headFiles.get(fileName));
                    String fileContents = readContentsAsString(checkoutFile);
                    File workingFile = new File(fileName);
                    writeContents(workingFile, fileContents);
                }

                for (String fileName : previousFiles.keySet()) {
                    if (!headFiles.containsKey(fileName)) {
                        File fileToDelete = new File(fileName);
                        restrictedDelete(fileToDelete);
                    }
                }

                _currentBranch = branchName;
                _stagedForRM = new HashSet<>();
                _stagedForAdd = new HashMap<>();
            }
        } else {
            throw error("Incorrect operands.");
        }
    }

    /** Creates a new branch pointer with the name BRANCHNAME. */
    public void branch(String branchName) {
        if (_branchHeads.containsKey(branchName)) {
            throw error("A branch with that name already exists.");
        } else {
            _branchHeads.put(branchName, headCommitUID());
        }
    }

    /** Removes the branch pointer with the name BRANCHNAME, but
     *  it does not delete the commits on that branch. */
    public void rmBranch(String branchName) {
        if (!_branchHeads.containsKey(branchName)) {
            throw error("A branch with that name does not exist.");
        } else if (branchName.equals(_currentBranch)) {
            throw error("Cannot remove the current branch.");
        } else {
            _branchHeads.remove(branchName);
        }
    }

    /** Will reset the working directory to the versions of the files
     * in the commit with commit id COMMITID. */
    public void reset(String commitID) {
        String id = commitID;
        if (id.length() < UID_LENGTH) {
            id = fullSizeID(id);
        }
        Commit desiredCommit = uidToACommit(id);
        HashMap<String, String> resetFiles = desiredCommit.getMyFiles();
        ArrayList<String> untrackedFiles = untrackedFiles();
        for (String fileName : resetFiles.keySet()) {
            if (untrackedFiles.contains(fileName)) {
                throw error("There is an untracked file "
                        + "in the way; delete it or add it first.");
            }
        }

        for (String fileName : resetFiles.keySet()) {
            File checkoutFile = new File(".gitlet/Blobs/"
                    + resetFiles.get(fileName));
            String fileContents = readContentsAsString(checkoutFile);
            File workingFile = new File(fileName);
            writeContents(workingFile, fileContents);
        }

        Commit latestCommit = uidToACommit(headCommitUID());
        HashMap<String, String> trackedFiles = latestCommit.getMyFiles();
        for (String fileName : trackedFiles.keySet()) {
            if (!resetFiles.containsKey(fileName)) {
                File fileToDelete = new File(fileName);
                restrictedDelete(fileToDelete);
            }
        }
        _branchHeads.put(_currentBranch, commitID);
        _stagedForAdd = new HashMap<>();
        _stagedForRM = new HashSet<>();
    }

    /** Takes in a split point SPLITPOINTCOMMITID and a branch name
     * GIVENBRANCHNAME. This branch is merged into the current branch. */
    public void merge(String givenBranchName, String splitPointCommitID) {
        if (_stagedForAdd.size() != 0 || _stagedForRM.size() != 0) {
            throw error("You have uncommitted changes.");
        }

        String gBranch_HeadCommitID = _branchHeads.get(givenBranchName);
        if (gBranch_HeadCommitID == null) {
            throw error("A branch with that name does not exist.");
        }

        if (_currentBranch.equals(givenBranchName)) {
            throw error("Cannot merge a branch with itself.");
        }

        if (splitPointCommitID.equals(gBranch_HeadCommitID)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }

        if (splitPointCommitID.equals(headCommitUID())) {
            _branchHeads.put(_currentBranch, gBranch_HeadCommitID);
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        ArrayList<String> filesToCheckoutAndStage = new ArrayList<>();
        ArrayList<String[]> mergeConflictArguments = new ArrayList<>();
        ArrayList<String> filesToRemove = new ArrayList<>();
        ArrayList<String> untrackedFiles = untrackedFiles();

        boolean conflict = false;
        String cBHC_FileContents;
        String gBHC_FileContents;
        File cBHC_File;
        File gBHC_File;

        Commit givenBranch_HeadCommit = uidToACommit(gBranch_HeadCommitID);
        Commit splitPointCommit = uidToACommit(splitPointCommitID);
        Commit currentBranch_HeadCommit = uidToACommit(headCommitUID());

        HashMap<String, String> givenBranch_HeadCommitFiles = givenBranch_HeadCommit.getMyFiles();
        HashMap<String, String> splitPointCommitFiles = splitPointCommit.getMyFiles();
        HashMap<String, String> currentBranch_HeadCommitFiles = currentBranch_HeadCommit.getMyFiles();

        String gBHC_FileHashCode;
        String sPC_FileHashCode;
        String cBHC_FileHashCode;

        for (String fileName : splitPointCommitFiles.keySet()) {
            gBHC_FileHashCode = givenBranch_HeadCommitFiles.get(fileName);
            sPC_FileHashCode = splitPointCommitFiles.get(fileName);
            cBHC_FileHashCode = currentBranch_HeadCommitFiles.get(fileName);

            if (gBHC_FileHashCode != null && cBHC_FileHashCode != null) {

                if (modified(fileName, gBHC_FileHashCode, sPC_FileHashCode) &&
                        !modified(fileName, cBHC_FileHashCode, sPC_FileHashCode)) {

                    if (untrackedFiles.contains(fileName)) {
                        throw error("There is an untracked file in the way; delete it or add it first.");
                    }

                    filesToCheckoutAndStage.add(fileName);

                } else if (modified(fileName, gBHC_FileHashCode, sPC_FileHashCode) &&
                        modified(fileName, cBHC_FileHashCode, sPC_FileHashCode) &&
                        !gBHC_FileHashCode.equals(cBHC_FileHashCode)) {

                    if (untrackedFiles.contains(fileName)) {
                        throw error("There is an untracked file in the way; delete it or add it first.");
                    }

                    cBHC_File = new File(".gitlet/Blobs/" + cBHC_FileHashCode);
                    cBHC_FileContents = readContentsAsString(cBHC_File);

                    gBHC_File = new File(".gitlet/Blobs/" + gBHC_FileHashCode);
                    gBHC_FileContents = readContentsAsString(gBHC_File);

                    String[] setOfArgs = {fileName, cBHC_FileContents, gBHC_FileContents};
                    mergeConflictArguments.add(setOfArgs);

                }

            } else if (gBHC_FileHashCode == null && cBHC_FileHashCode != null &&
                    !modified(fileName, cBHC_FileHashCode, sPC_FileHashCode)) {

                if (untrackedFiles.contains(fileName)) {
                    throw error("There is an untracked file in the way; delete it or add it first.");
                }

                filesToRemove.add(fileName);

            } else if (gBHC_FileHashCode == null && cBHC_FileHashCode != null &&
                    modified(fileName, cBHC_FileHashCode, sPC_FileHashCode)) {

                if (untrackedFiles.contains(fileName)) {
                    throw error("There is an untracked file in the way; delete it or add it first.");
                }

                cBHC_File = new File(".gitlet/Blobs/" + cBHC_FileHashCode);
                cBHC_FileContents = readContentsAsString(cBHC_File);

                String[] setOfArgs = {fileName, cBHC_FileContents, ""};
                mergeConflictArguments.add(setOfArgs);

            } else if (cBHC_FileHashCode == null && gBHC_FileHashCode != null &&
                    modified(fileName, gBHC_FileHashCode, sPC_FileHashCode)) {

                if (untrackedFiles.contains(fileName)) {
                    throw error("There is an untracked file in the way; delete it or add it first.");
                }

                gBHC_File = new File(".gitlet/Blobs/" + gBHC_FileHashCode);
                gBHC_FileContents = readContentsAsString(gBHC_File);

                String[] setOfArgs = {fileName, "", gBHC_FileContents};
                mergeConflictArguments.add(setOfArgs);

            }

        }

        for (String fileName : givenBranch_HeadCommitFiles.keySet()) {
            gBHC_FileHashCode = givenBranch_HeadCommitFiles.get(fileName);
            sPC_FileHashCode = splitPointCommitFiles.get(fileName);
            cBHC_FileHashCode = currentBranch_HeadCommitFiles.get(fileName);

            if (sPC_FileHashCode == null && cBHC_FileHashCode == null) {

                if (untrackedFiles.contains(fileName)) {
                    throw error("There is an untracked file in the way; delete it or add it first.");
                }

                filesToCheckoutAndStage.add(fileName);

            } else if (sPC_FileHashCode == null && cBHC_FileHashCode != null &&
                    !cBHC_FileHashCode.equals(gBHC_FileHashCode)) {

                if (untrackedFiles.contains(fileName)) {
                    throw error("There is an untracked file in the way; delete it or add it first.");
                }

                cBHC_File = new File(".gitlet/Blobs/" + cBHC_FileHashCode);
                cBHC_FileContents = readContentsAsString(cBHC_File);

                gBHC_File = new File(".gitlet/Blobs/" + gBHC_FileHashCode);
                gBHC_FileContents = readContentsAsString(gBHC_File);

                String[] setOfArgs = {fileName, cBHC_FileContents, gBHC_FileContents};
                mergeConflictArguments.add(setOfArgs);

            }

        }

        String[] arguments = {gBranch_HeadCommitID, "--", ""};
        for (String fileName : filesToCheckoutAndStage) {
            arguments[2] = fileName;
            checkout(arguments);
            add(fileName);
        }

        for (String[] setOfArgs : mergeConflictArguments) {
            mergeConflict(setOfArgs[0], setOfArgs[1], setOfArgs[2]);
        }

        if (mergeConflictArguments.size() > 0) {
            conflict = true;
        }

        for (String fileName : filesToRemove) {
            rm(fileName);
        }

        String message = "Merged " + givenBranchName + " into " + _currentBranch + ".";
        String[] parents = {headCommitUID(), gBranch_HeadCommitID};
        mergeCommit(message, parents);

        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** Returns the commit id of the split point between GIVENBRANCHNAME
     * and the current branch. */
    public String findSplitPoint(String givenBranchName) {
        ArrayList<String> givenBranch_CommitIDs = new ArrayList<>();
        String gBranch_HeadCommitID = _branchHeads.get(givenBranchName);

        givenBranch_CommitIDs.add(gBranch_HeadCommitID);
        Commit currentCommit = uidToACommit(gBranch_HeadCommitID);
        String[] parents = currentCommit.getAllParents();

        while (parents != null) {
            givenBranch_CommitIDs.add(parents[0]);

            if (parents.length == 2) {
                givenBranch_CommitIDs.add(parents[1]);
            }

            currentCommit = uidToACommit(parents[0]);
            parents = currentCommit.getAllParents();
        }

        Collections.sort(givenBranch_CommitIDs);

        String currentBranch_CurrentCommitID = headCommitUID();
        int index = Collections.binarySearch(givenBranch_CommitIDs, currentBranch_CurrentCommitID);
        if (index >= 0) {
            return currentBranch_CurrentCommitID;
        }
        currentCommit = uidToACommit(currentBranch_CurrentCommitID);
        parents = currentCommit.getAllParents();

        while (parents != null) {
            for (String parent : parents) {
                index = Collections.binarySearch(givenBranch_CommitIDs, parent);
                if (index >= 0) {
                    return parent;
                }
            }

            currentCommit = uidToACommit(parents[0]);
            parents = currentCommit.getAllParents();
        }
        return "";
    }

    /** Overwrites files with merge conflicts with file contents from both branches.
     *  Takes in a file name FILENAME, current branch file contents CBHC_FILECONTENTS,
     *  and given branch file contents GBHC_FILECONTENTS. */
    public void mergeConflict(String fileName, String cBHC_FileContents, String gBHC_FileContents) {
        String fileContents = "<<<<<<< HEAD\n";

        fileContents += cBHC_FileContents;

        fileContents += "=======\n";

        fileContents += gBHC_FileContents;

        fileContents += ">>>>>>>";

        File workingFile = new File(fileName);
        writeContents(workingFile, fileContents);
        add(fileName);
    }

    /** Same as the regular commit method, but for merge commits.
     * Takes in a message MESSAGE and an array of Strings PARENTS
     * with the ids of parent commits */
    public void mergeCommit(String message, String[] parents) {
        if (message.trim().equals("")) {
            throw error("Please enter a commit message.");
        }
        Commit latestCommit = uidToACommit(headCommitUID());
        HashMap<String, String> filesTracked = latestCommit.getMyFiles();

        if (_stagedForAdd.size() == 0 && _stagedForRM.size() == 0) {
            throw error("No changes added to the commit.");
        } else {
            for (String name : _stagedForAdd.keySet()) {
                filesTracked.put(name, _stagedForAdd.get(name));
            }
            for (String name : _stagedForRM) {
                filesTracked.remove(name);
            }
        }
        Commit newCommit = new Commit(filesTracked, parents, message, false);
        String newCommitID = newCommit.getMyUID();
        File newCommitFile = new File(".gitlet/Commits/" + newCommitID);
        writeObject(newCommitFile, newCommit);

        _stagedForAdd = new HashMap<>();
        _stagedForRM = new HashSet<>();
        _branchHeads.put(_currentBranch, newCommit.getMyUID());
    }

    /** Returns an ArrayList of Strings of
     * file names corresponding to files
     * in the working directory that are untracked. */
    public ArrayList<String> untrackedFiles() {
        List<String> workingFiles =
                plainFilenamesIn(System.getProperty("user.dir"));
        Commit latestCommit = uidToACommit(headCommitUID());
        HashMap<String, String> trackedFiles = latestCommit.getMyFiles();
        ArrayList<String> untrackedFiles = new ArrayList<>();

        for (String file : workingFiles) {
            if (!_stagedForAdd.containsKey(file)
                    && !trackedFiles.containsKey(file)) {
                untrackedFiles.add(file);
            } else if (_stagedForRM.contains(file)) {
                untrackedFiles.add(file);
            }
        }

        return untrackedFiles;
    }

    /** Takes in a file name FILENAME of a file in the working directory
     * and overwrites it with a version of the file from the commit with
     * the id COMMITID. */
    public void writeToFile(String fileName, String commitID) {
        Commit desiredCommit = uidToACommit(commitID);
        HashMap<String, String> commitBlobs = desiredCommit.getMyFiles();

        if (commitBlobs.containsKey(fileName)) {
            File checkoutFile = new File(".gitlet/Blobs/"
                    + commitBlobs.get(fileName));
            String fileContents = readContentsAsString(checkoutFile);
            File workingFile = new File(fileName);
            writeContents(workingFile, fileContents);
        } else {
            throw error("File does not exist in that commit.");
        }
    }

    /** Checks if two versions of a file have different contents. Takes
     *  in two file hash codes, FILEHASHCODE1 and FILEHASHCODE2, and the name of the file FILENAME.
     *  @return boolean */
    public boolean modified(String fileName, String fileHashCode1, String fileHashCode2) {
        return !fileHashCode2.equals(fileHashCode1);
    }

    /** Takes in a file name FILENAME of a file in the working directory
     * and returns
     * true if it is different from the version of the file stored
     * in the HashMap
     * FILES. */
    public boolean isDifferent(String fileName, HashMap<String, String> files) {
        File workingFile = new File(fileName);
        String fileHashCode = sha1(readContentsAsString(workingFile));
        return !fileHashCode.equals(files.get(fileName));
    }

    /** Returns true if the file with name FILENAME
     * exists in the working directory. */
    public boolean exists(String fileName) {
        File thisFile = new File(fileName);
        return thisFile.exists();
    }

    /** Takes in a shortened commit UID and returns the full sized UID. Returns
     * an error if SHORTID is not valid. */
    public String fullSizeID(String shortID) {
        File commitsDir = new File(".gitlet/Commits");
        List<String> commitNames = plainFilenamesIn(commitsDir);
        for (String name : commitNames) {
            if (name.contains(shortID)) {
                return name;
            }
        }
        throw error("No commit with that id exists.");
    }

    /** Takes in a commit id, UID, and returns the corresponding commit. */
    public Commit uidToACommit(String uid) {
        File commitFile = new File(".gitlet/Commits/" + uid);
        if (commitFile.exists()) {
            return readObject(commitFile, Commit.class);
        } else {
            throw error("No commit with that id exists.");
        }
    }

    /** Returns the UID of the head commit in the current branch. */
    public String headCommitUID() {
        return _branchHeads.get(_currentBranch);
    }



    /** A String denoting the name of the current branch. */
    private String _currentBranch;

    /** A HashMap which maps the name of staged files to
     * their corresponding HashCode. */
    private HashMap<String, String> _stagedForAdd;

    /** A HashMap which maps the name of branches to the
     * HashCodes of their head commits. */
    private HashMap<String, String> _branchHeads;

    /** A String HashSet that contains the names of removed files. */
    private HashSet<String> _stagedForRM;
}
