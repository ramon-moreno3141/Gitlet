# Gitlet
<strong>FILES WORKED ON: gitlet/Repository.java, gitlet/Main.java, gitlet/Commit.java</strong>

Gitlet is a version control system written in java that mimics some of the basic features of the popular Git system. It allows you to save the contents of entire 
directories of files through commits. You can also restore versions of files, view your backup history through a log, create branches, and merge branches 
together. 

<strong>Commands:</strong>
<ul>
<li><strong>init:</strong> Creates a new Gitlet version-control system in the current directory.</li>
  <ul>
    <li><strong>Usage:</strong> java gitlet.Main init</li>
  </ul>
<li><strong>add:</strong> Stages the file so it can be tracked in the next commit.</li>
  <ul>
    <li><strong>Usage:</strong> java gitlet.Main add [file name]</li>
  </ul>
<li><strong>commit:</strong> Saves a snapshot of certain files in the current commit and staging area so they can be restored at a later time, creating a new commit. Commits are saved with a commit message. To indicate a multiword message, put the message in quotation marks.</li>
  <ul>
    <li><strong>Usage:</strong> java gitlet.Main commit [message]</li>
  </ul>
<li><strong>rm:</strong> If currently staged, the file is unstaged. If the file is tracked in the current commit, it will no longer be tracked in future commits. The file is also removed from the working directory if the user has not already done so.</li>
  <ul>
    <li><strong>Usage:</strong> java gitlet.Main rm [file name]</li>
  </ul>
<li><strong>log:</strong> Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit, following the first parent commit links, ignoring any second parents found in merge commits.</li>
  <ul>
    <li><strong>Usage:</strong> java gitlet.Main log</li>
  </ul>
<li><strong>global-log:</strong> Like log, except displays information about all commits ever made in no particular order.</li>
  <ul>
    <li><strong>Usage:</strong> java gitlet.Main global-log</li>
  </ul>
<li><strong>find:</strong> Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such commits, it prints the ids out on separate lines. To indicate a multiword message, put the message in quotation marks.</li>
  <ul>
    <li><strong>Usage:</strong> java gitlet.Main find [commit message]</li>
  </ul>
<li><strong>status:</strong> Displays what branches currently exist, unstaged modifications, untracked files and marks the current branch with a *. Also displays what files have been staged or marked for untracking. </li>
  <ul>
    <li><strong>Usage:</strong> java gitlet.Main status</li>
  </ul>
<li><strong>checkout:</strong> Different functionality depending on what arguments are passed in. Users can abbreviate commit ids using only the first six digits.</li>
  <ul>
    <li><strong>Usage:</strong> java gitlet.Main checkout -- [file name]</li>
      <ul>
        <li>Takes the version of the file as it exists in the head commit, the front of the current branch, and puts it in the working directory, overwriting the version of the file that's already there if there is one. The new version of the file is not staged.</li>
      </ul>
    <li><strong>Usage:</strong> java gitlet.Main checkout [commit id] -- [file name]</li>
      <ul>
        <li>Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory, overwriting the version of the file that's already there if there is one. The new version of the file is not staged.</li>
      </ul>
    <li><strong>Usage:</strong> java gitlet.Main checkout [branch name]</li>
      <ul>
        <li>Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist. Also, at the end of this command, the given branch will now be considered the current branch (HEAD). Any files that are tracked in the current branch but are not present in the checked-out branch are deleted. The staging area is cleared, unless the checked-out branch is the current branch.</li>
      </ul>
  </ul>
<li><strong>branch:</strong> Creates a new branch with the given name, and points it at the current head node. This command does NOT immediately switch to the newly created branch (just as in real Git).</li>
  <ul>
    <li><strong>Usage:</strong> java gitlet.Main branch [branch name]</li>
  </ul>
<li><strong>rm-branch:</strong> Deletes the branch with the given name. Does not delete commits under the given branch.</li>
  <ul>
    <li><strong>Usage:</strong> java gitlet.Main rm-branch [branch name]</li>
  </ul>
<li><strong>reset:</strong> Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit. Also moves the current branch's head to that commit node. The [commit id] may be abbreviated as for checkout. The staging area is cleared. The command is essentially checkout of an arbitrary commit that also changes the current branch head.</li>
  <ul>
    <li><strong>Usage:</strong> java gitlet.Main reset [commit id]</li>
  </ul>
<li><strong>merge:</strong> Merges files from the given branch into the current branch. Users have a chance to resolve merge conflicts just as in real Git.</li>
  <ul>
    <li><strong>Usage:</strong> java gitlet.Main merge [branch name]</li>
  </ul>
</ul>
