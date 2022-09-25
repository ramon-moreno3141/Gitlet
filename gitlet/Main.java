package gitlet;

import java.util.Arrays;
import java.util.HashSet;
import java.io.File;

/** Driver class for Gitlet, the tiny version-control system.
 *  @author Ramon Moreno
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        try {
            if (args.length == 0) {
                throw new GitletException("Please enter a command.");
            } else if (!isValid(args[0])) {
                throw new GitletException("No command with that name exists.");
            } else if (isInitialized()) {
                File myRepo = new File(".gitlet/myRepo");
                myRepository = Utils.readObject(myRepo, Repository.class);
                completeAction(args[0],
                        Arrays.copyOfRange(args, 1, args.length));
                Utils.writeObject(myRepo, myRepository);
            } else if (args[0].equals("init")) {
                myRepository = new Repository();
                File myRepo = new File(".gitlet/myRepo");
                Utils.writeObject(myRepo, myRepository);
                System.out.println("Gitlet repository initialized.");
            } else {
                throw new GitletException("Not in an "
                        + "initialized Gitlet directory.");
            }
        } catch (GitletException exception) {
            System.out.println(exception.getMessage());
            System.exit(0);
        }
    }


    /** Completes Action COMMAND ARGUMENTS. */
    private static void completeAction(String command, String[] arguments) {
        switch (command) {
        case "init":
            throw new GitletException("A Gitlet version-control system "
                    + "already exists in the current directory.");
        case "add":
            myRepository.add(arguments[0]);
            break;
        case "commit":
            myRepository.commit(arguments[0]);
            break;
        case "log":
            myRepository.log();
            break;
        case "checkout":
            myRepository.checkout(arguments);
            break;
        case "rm":
            myRepository.rm(arguments[0]);
            break;
        case "global-log":
            myRepository.globalLog();
            break;
        case "find":
            myRepository.find(arguments[0]);
            break;
        case "status":
            myRepository.status();
            break;
        case "branch":
            myRepository.branch(arguments[0]);
            break;
        case "rm-branch":
            myRepository.rmBranch(arguments[0]);
            break;
        case "reset":
            myRepository.reset(arguments[0]);
            break;
        case "merge":
            String splitPoint = myRepository.findSplitPoint(arguments[0]);
            myRepository.merge(arguments[0], splitPoint);
            break;
        default:
            break;
        }
    }

    /** Returns true if there is a .gitlet directory inside this directory. */
    private static boolean isInitialized() {
        File tempGitlet = new File(".gitlet");
        return tempGitlet.exists();
    }
    /** Returns true if COMMAND is a valid Gitlet command. */
    private static boolean isValid(String command) {
        return validCommands.contains(command);
    }

    /** An array of Strings of all the valid gitlet commands. */
    private static String[] validCommandsArray = new String[] {"init", "add",
        "commit", "rm", "log", "global-log", "find", "status", "checkout",
        "branch", "rm-branch", "reset", "merge"};

    /** A String HashSet of all the valid Gitlet commands. */
    private static HashSet<String> validCommands =
            new HashSet<>(Arrays.asList(validCommandsArray));

    /** A Gitlet repository. */
    private static Repository myRepository;

}
