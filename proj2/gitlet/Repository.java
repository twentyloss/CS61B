package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *
 *
 *  @author Xinyi Lin
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    public static final File REFS = join(GITLET_DIR, "refs");
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    public static final File STAGING_DIR = join(GITLET_DIR, "staging");
    public static final File COMMIT_MAP = join(STAGING_DIR, "commitMap");
    public static final File STAGING_MAP = join(STAGING_DIR, "stageMap");
    public static final String DEFAULT_BRANCH = "master";
    private static String currHeadRef = null;
    private static String currBranch = null;
    private static Map<String, String> stageMap = null;
    private static Map<String, String> commitMap = null;

    public static void init() {
        if (GITLET_DIR.exists()) {
            exitWithMessage("A Gitlet version-control system already exists"
                               + " in the current directory.");
        }
        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        STAGING_DIR.mkdir();
        REFS.mkdir();
        try {
            HEAD.createNewFile();
        } catch (IOException e) {
            exitWithMessage("Fail to create HEAD file.");
        }
        addBranch(DEFAULT_BRANCH);
        currBranch = DEFAULT_BRANCH;
        currHeadRef = REFS.getAbsolutePath() + "/" + currBranch;
        writeContents(HEAD, currHeadRef);
        Commit initial = Commit.initialCommit();
        initial.commit();
        commitMap = new TreeMap<>();
        resetStageArea();
    }

    public static void add(String filename) {
        isRepoCheck();

        File f = join(CWD, filename);
        if (!f.exists()) {
            exitWithMessage("File does not exist.");
        }

        String content = readContentsAsString(f);
        String blobSha1 = sha1Blob(content);

        if (blobSha1.equals(commitMap.get(filename))) {
            if (stageMap.containsKey(filename)) {
                String v = stageMap.remove(filename);
                File stagedBlob = join(STAGING_DIR, v);
                stagedBlob.delete();
            } else {
                return;
            }
        } else if (!blobSha1.equals(stageMap.get(filename))) {
            String oldSha1 = stageMap.put(filename, blobSha1);
            if (oldSha1 != null) {
                join(STAGING_DIR, oldSha1).delete();
            }
            writeContents(join(STAGING_DIR, blobSha1), content);
        }
        saveStageMap();
    }

    public static void commit(String message) {
        isRepoCheck();
        if (!STAGING_MAP.exists() || stageMap.isEmpty()) {
            exitWithMessage("No changes added to the commit.");
        }

        for (String key: stageMap.keySet()) {
            String sha1Code = stageMap.get(key);
            if (sha1Code.isEmpty()) {
                commitMap.remove(key);
            } else {
                File hashDir = getHashDir(sha1Code);
                commitMap.put(key, sha1Code);
                join(STAGING_DIR, sha1Code).renameTo(join(hashDir, sha1Code));
            }
        }
        String parent = getStringContent(currHeadRef);
        Commit c = Commit.generateCommit(message, commitMap, parent);
        c.commit();
        resetStageArea();
    }

    public static void remove(String filename) {
        isRepoCheck();
        File f = join(CWD, filename);
        boolean checkFlag = false;

        if (stageMap.containsKey(filename)) {
            File stageFile = join(STAGING_DIR, stageMap.remove(filename));
            if (stageFile.isFile()) {
                stageFile.delete();
            }
            checkFlag = true;
        }

        if (commitMap.containsKey(filename)) {
            restrictedDelete(f);
            stageMap.put(filename, "");
            checkFlag = true;

        }
        if (checkFlag) {
            saveStageMap();
        } else {
            exitWithMessage("No reason to remove the file.");
        }
    }

    public static void log() {
        isRepoCheck();
        String hc = getStringContent(currHeadRef);
        Commit headCommit = Commit.fromFile(hc);
        headCommit.log();
    }

    public static void globalLog() {
        List<String> files = plainFilenamesIn(REFS);
        for (String filename: files) {
            if (!filename.endsWith("CommitList")) {
                continue;
            }
            File f = new File(REFS, filename);
            LinkedList<String> commitList = readObject(f, LinkedList.class);
            for (String c : commitList) {
                if (c.length() != UID_LENGTH) {
                    continue;
                }
                Commit commit = Commit.fromFile(c);
                System.out.println(commit);
            }
        }
    }

    public static void find(String message) {
        List<String> files = plainFilenamesIn(REFS);
        int findCount = 0;
        for (String filename: files) {
            if (!filename.endsWith("CommitList")) {
                continue;
            }
            File f = new File(REFS, filename);
            LinkedList<String> commitList = readObject(f, LinkedList.class);
            for (String c : commitList) {
                if (c.startsWith("Parent Branch")) {
                    break;
                }
                Commit commit = Commit.fromFile(c);
                if (commit.getMessage().equals(message)) {
                    System.out.println(commit.getSha1Code());
                    findCount += 1;
                }
            }
        }                                            
        if (findCount == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void status() {
        isRepoCheck();
        System.out.println("=== Branches ===");
        List<String> files = plainFilenamesIn(REFS);
        for (String filename: files) {
            if (!filename.endsWith("CommitList")) {
                System.out.println(filename.equals(currBranch) ? "*" + filename : filename);
            }
        }
        System.out.println();

        LinkedList<String> staged = new LinkedList<>();
        LinkedList<String> removed = new LinkedList<>();
        for (String key: stageMap.keySet()) {
            if (stageMap.get(key).isEmpty()) {
                removed.addLast(key);
            } else {
                staged.addLast(key);
            }
        }
        System.out.println("=== Staged Files ===");
        for (String file: staged) {
            System.out.println(file);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        for (String file: removed) {
            System.out.println(file);
        }
        System.out.println();

        List<String> workingDir = plainFilenamesIn(CWD);
        LinkedList<String> modified = new LinkedList<>();
        LinkedList<String> untracked = new LinkedList<>();
        Set<String> trackedFileSet = getTrackedFileSet();
        for (String file: workingDir) {
            if (!trackedFileSet.remove(file)) {
                untracked.addLast(file);
                continue;
            }
            File f = join(CWD, file);
            if (stageMap.containsKey(file)) {
                String stageSha1 = stageMap.get(file);
                if (stageSha1.isEmpty()) {
                    untracked.addLast(file);
                    continue;
                }
                String workingSha1 = sha1Blob(readContentsAsString(f));
                if (!stageSha1.equals(workingSha1)) {
                    modified.addLast(file + " (modified)");
                }
            } else if (commitMap.containsKey(file)) {
                String workingSha1 = sha1Blob(readContentsAsString(f));
                if (!commitMap.get(file).equals(workingSha1)) {
                    modified.addLast(file + " (modified)");
                }
            }
        }
        for (String deleted: trackedFileSet) {
            modified.addLast(deleted + " (deleted)");
        }

        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String f: modified) {
            System.out.println(f);
        }
        System.out.println();

        System.out.println("=== Untracked Files ===");
        for (String f: untracked) {
            System.out.println(f);
        }
        System.out.println();
    }



    public static void addBranch(String b) {
        isRepoCheck();
        File branch = join(REFS, b);
        if (branch.exists()) {
            System.out.println("A branch with that name already exists.");
        } else {
            try {
                branch.createNewFile();
            } catch (IOException e) {
                exitWithMessage("Fail to create file" + b);
            }
            String h = readContentsAsString(HEAD);
            LinkedList<String> commits = new LinkedList<>();
            if (!h.isEmpty()) {
                File currHead = new File(h);
                writeContents(branch, readContentsAsString(currHead));
                commits.addFirst(currBranch + "," + readContentsAsString(currHead));
            }
            File commitList = join(REFS, b + "CommitList");
            try {
                commitList.createNewFile();
            } catch (IOException e) {
                exitWithMessage("Fail to create commit list file.");
            }
            writeObject(commitList, commits);
        }
    }

    public static void checkoutBranch(String b) {
        isRepoCheck();
        File f = join(REFS, b);
        if (!f.exists()) {
            exitWithMessage("No such branch exists.");
        } else if (currBranch.equals(b)) {
            exitWithMessage("No need to checkout the current branch.");
        }
        Commit headCommit = Commit.fromFile(readContentsAsString(f));
        Map<String, String> headMap = headCommit.getFileMap();
        List<String> workingFiles =  getWorkingFilesWithCheck(headMap);

        for (String filename: headMap.keySet()) {
            File blob = getBlobFile(headMap.get(filename));
            String content = readContentsAsString(blob);
            File headFile = join(CWD, filename);
            writeContents(headFile, content);
            workingFiles.remove(filename);
        }

        for (String file: workingFiles) {
            restrictedDelete(join(CWD, file));
        }

        currHeadRef = f.getAbsolutePath();
        writeContents(HEAD, currHeadRef);
        currBranch = b;
        commitMap = headMap;
        resetStageArea();
    }

    public static void checkoutFile(String filename) {
        isRepoCheck();
        String blobSha1 = commitMap.get(filename);
        if (blobSha1 == null) {
            exitWithMessage("File does not exist in that commit.");
        }
        writeBLobIntoCWD(filename, blobSha1);
    /*
    *   not sure whether it's required to remove the staged version
        String stagedSha1 = stageMap.remove(filename);
        if (stagedSha1 != null && stagedSha1.isEmpty()) {
            join(STAGING_DIR, stagedSha1).delete();
        }
        saveStageMap();

    */
    }

    public static void checkoutFileByCommitId(String commitId, String filename) {
        isRepoCheck();
        Commit c = checkCommitId(commitId);
        String blobSha1 = c.getFileMap().get(filename);
        if (blobSha1 == null) {
            exitWithMessage("File does not exist in that commit.");
        }
        writeBLobIntoCWD(filename, blobSha1);

    }

    private static void writeBLobIntoCWD(String filename, String blobSha1) {
        if (blobSha1 == null) {
            exitWithMessage("File does not exist in that commit.");
        }
        String content = readContentsAsString(getBlobFile(blobSha1));
        writeContents(join(CWD, filename), content);
    }

    public static void removeBranch(String b) {
        isRepoCheck();
        File f = join(REFS, b);
        if (!f.exists()) {
            exitWithMessage("A branch with that name does not exist.");
        } else if (currBranch.equals(b)) {
            exitWithMessage("Cannot remove the current branch.");
        }
        f.delete();
    }

    public static void reset(String commitId) {
        isRepoCheck();
        Commit c = checkCommitId(commitId);
        Set<String> trackedFileSet = getTrackedFileSet();
        Map<String, String> resetMap = c.getFileMap();
        getWorkingFilesWithCheck(resetMap);

        //write content of blobs in reset commit into CWD.
        for (String filename: resetMap.keySet()) {
            String resetSha1Code = resetMap.get(filename);
            if (!resetSha1Code.equals(commitMap.get(filename))) {
                writeBLobIntoCWD(filename, resetSha1Code);
            }
            trackedFileSet.remove(filename);

        }

        //delete all tracked files that are not tracked by reset commit.
        // will not delete files that are untracked before reset.
        for (String filename: trackedFileSet) {
            restrictedDelete(join(CWD, filename));
        }
        c.reset();
        commitMap = resetMap;
        resetStageArea();
    }

    public static void merge(String branch) {
        isRepoCheck();
        //branch check
        File f = join(REFS, branch);
        if (!f.exists()) {
            exitWithMessage("A branch with that name does not exist.");
        } else if (currBranch.equals(branch)) {
            exitWithMessage("Cannot merge a branch with itself.");
        }
        // check whether there's addition or removal in current stage
        for (String stagedFile: stageMap.keySet()) {
            if (!commitMap.containsKey(stagedFile) || stageMap.get(stagedFile).isEmpty()) {
                exitWithMessage("You have uncommitted changes.");
            }
        }
        //get the common ancestor first.
        Commit splitPoint = getSplitPoint(currBranch, branch);
        Map<String, String> splitPointMap = splitPoint.getFileMap();
        Commit givenBranchCommit = Commit.fromFile(readContentsAsString(join(REFS, branch)));
        Map<String, String> givenBranchMap = givenBranchCommit.getFileMap();

        // split point check
        if (splitPoint.equals(givenBranchCommit)) {
            exitWithMessage("Given branch is an ancestor of the current branch.");
        } else if (splitPoint.getSha1Code().equals(getCurrentCommitId())) {
            checkoutBranch(branch);
            exitWithMessage("Current branch fast-forwarded.");
        }

        boolean conflict = false;
        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(splitPointMap.keySet());
        allFiles.addAll(givenBranchMap.keySet());
        allFiles.addAll(commitMap.keySet());
        allFiles.addAll(stageMap.keySet());

        for (String currentFile : allFiles) {
            String splitBlob = splitPointMap.get(currentFile);
            String givenBlob = givenBranchMap.get(currentFile);
            String currBlob = stageMap.containsKey(currentFile)
                                ? (stageMap.get(currentFile).isEmpty()
                                        ? null : stageMap.get(currentFile))
                                : commitMap.get(currentFile);
            if (splitBlob != null) {
                // if not modified in given branch, or two branches modified at the same way,
                // remain current state
                if (splitBlob.equals(givenBlob) || givenBlob == currBlob
                        || givenBlob.equals(currBlob)) {
                    continue;
                } else if (splitBlob.equals(currBlob)) {
                    //if modified by given branch and not in current branch
                    if (givenBlob != null) { // not removed in given branch
                        checkoutFileByCommitId(givenBranchCommit.getSha1Code(), currentFile);
                        add(currentFile);
                    } else { // removed in given branch
                        remove(currentFile);
                    }
                } else {
                    //when the contents in two branches are all different with the split point,
                    // it means conflict
                    updateConflictFile(currentFile, currBlob, givenBlob);
                    conflict = true;
                }
            } else { // if the file is not present in split point.
                if (givenBlob == null || givenBlob.equals(currBlob)) {
                    continue;
                } else if (currBlob == null) {
                    checkoutFileByCommitId(givenBranchCommit.getSha1Code(), currentFile);
                    add(currentFile);
                } else {
                    updateConflictFile(currentFile, currBlob, givenBlob);
                    conflict = true;
                }
            }
        }
        commit("Merged " + branch + " into " + currBranch + ".");
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    private static void updateConflictFile(String filename, String currBlob, String givenBlob) {
        String g, c;
        if (givenBlob == null || givenBlob.isEmpty()) {
            g = "";
        } else {
            g = readContentsAsString(getBlobFile(givenBlob));
        }
        if (currBlob == null || currBlob.isEmpty()) {
            c = "";
        } else {
            c = readContentsAsString(getBlobFile(currBlob));
        }
        String formattedContent = getFormattedConflictContent(c, g);
        writeContents(join(CWD, filename), formattedContent);
        add(filename);
    }

    private static String getFormattedConflictContent(String head, String given) {
        StringBuilder s = new StringBuilder();
        s.append("<<<<<<< HEAD\n");
        s.append(head);
        s.append("=======\n");
        s.append(given);
        s.append(">>>>>>>\n");
        return s.toString();
    }



    private static void isRepoCheck() {
        if (!GITLET_DIR.exists()) {
            exitWithMessage("Not in an initialized Gitlet directory.");
        }
        currHeadRef = readContentsAsString(HEAD);
        currBranch = currHeadRef.substring(currHeadRef.lastIndexOf('/') + 1);
        if (STAGING_MAP.exists()) {
            stageMap = readObject(STAGING_MAP, TreeMap.class);
        } else {
            stageMap = new TreeMap<>();
        }
        if (COMMIT_MAP.exists()) {
            commitMap = readObject(COMMIT_MAP, TreeMap.class);
        }
    }

    private static String sha1Blob(String content) {
        return sha1("blob", content);
    }

    private static void saveStageMap() {
        writeObject(STAGING_MAP, (Serializable) stageMap);
    }

    private static String getStringContent(String filename) {
        return readContentsAsString(new File(filename));
    }

    public static File getHashDir(String s) {
        String originHash = Integer.toHexString(s.substring(0, 4).hashCode() % 256);
        String hash = originHash.substring(Math.max(originHash.length() - 2, 0));
        File hashDir = join(OBJECTS_DIR, hash);
        hashDir.mkdir();
        return hashDir;
    }

    private static LinkedList<String> getWorkingFilesWithCheck(Map<String, String> checkoutMap) {
        LinkedList<String> returnList = new LinkedList<>();
        List<String> fileList = plainFilenamesIn(CWD);
        Set<String> trackedFileSet = getTrackedFileSet();
        for (String file: fileList) {
            if (!trackedFileSet.contains(file) && checkoutMap.containsKey(file)) {
                exitWithMessage("There is an untracked file in the way; "
                        + "delete, or add and commit it first.");
            }
        }
        returnList.addAll(fileList);
        return returnList;
    }

    private static File getBlobFile(String s) {
        return join(getHashDir(s), s);
    }

    private static void resetStageArea() {
        List<String> files = plainFilenamesIn(STAGING_DIR);
        for (String file: files) {
            join(STAGING_DIR, file).delete();
        }
        writeObject(COMMIT_MAP, (Serializable) commitMap);
    }

    private static Commit checkCommitId(String commitId) {
        int idLength = commitId.length();
        if (idLength > 40 || idLength < 6) {
            exitWithMessage("No commit with that id exists.");
        }
        File hashDir = getHashDir(commitId);
        List<String> objects = plainFilenamesIn(hashDir);
        for (String obj: objects) {
            if (obj.substring(0, idLength).equals(commitId)) {
                try {
                    Commit c = readObject(join(hashDir, obj), Commit.class);
                    return c;
                } catch (IllegalArgumentException e) {
                    exitWithMessage("No commit with that id exists.");
                }
            }
        }
        exitWithMessage("No commit with that id exists.");
        return null;
    }


    private static Set<String> getTrackedFileSet() {
        Set<String> trackedFileSet = new TreeSet<>();
        trackedFileSet.addAll(commitMap.keySet());
        for (String file: stageMap.keySet()) {
            if (!stageMap.get(file).isEmpty()) {
                trackedFileSet.add(file);
            } else {
                trackedFileSet.remove(file);
            }
        }
        return trackedFileSet;
    }

    private static Commit getSplitPoint(String b1, String b2) {
        // splitPoint: 0th element is parent branch name,
        // 1st element is the commit id of split point
        Commit splitPoint;
        if (b1.equals("master")) {
            String[] b2Parent = getParentInfo(b2);
            while (!b2Parent[0].equals("master")) {
                b2Parent = getParentInfo(b2Parent[0]);
            }
            splitPoint = Commit.fromFile(b2Parent[1]);
        } else if (b2.equals("master")) {
            String[] b1Parent = getParentInfo(b1);
            while (!b1Parent[0].equals("master")) {
                b1Parent = getParentInfo(b1Parent[0]);
            }
            splitPoint = Commit.fromFile(b1Parent[1]);
        } else {
            LinkedList<String[]> parentListOfb1 = new LinkedList<>();
            Set<String> b1ParentsSet = new HashSet<>();
            LinkedList<String[]> parentListOfb2 = new LinkedList<>();
            while (parentListOfb1.isEmpty() || !parentListOfb1.getFirst()[0].equals("master")) {
                String[] temp = getParentInfo(b1);
                parentListOfb1.addFirst(temp);
                b1ParentsSet.add(temp[0]);
            }
            while (parentListOfb2.isEmpty()
                    || b1ParentsSet.contains(parentListOfb2.getFirst()[0])) {
                parentListOfb2.addFirst(getParentInfo(b2));
            }
            //get the split point of two branches at their common parent branch
            String[] temp1 = parentListOfb2.getFirst();
            String[] temp2 = null;
            for (String[] parent: parentListOfb1) {
                if (parent[0].equals(temp1[0])) {
                    temp2 = parent;
                }
            }
            Commit commit1 = Commit.fromFile(temp1[1]);
            Commit commit2 = Commit.fromFile(temp2[1]);
            if (commit1.compareTo(commit2) <= 0) {
                splitPoint = commit1;
            } else {
                splitPoint = commit2;
            }

        }
        return splitPoint;
    }

    /* Return a 2-element String array of parent branch information
    *  First element is parent branch name.
    *  Second element is Split commit id.
    */
    private static String[] getParentInfo(String b) {
        if (b.equals("master")) {
            System.out.println("Cannot find parent of master branch.");
            return null;
        }
        File f = join(REFS, b + "Commitlist");
        LinkedList<String> commits = readObject(f, LinkedList.class);
        String[] parentInfo = commits.getLast().split(",");
        return parentInfo;
    }

    private static void exitWithMessage(String s) {
        System.out.println(s);
        System.exit(0);
    }

    private static String getCurrentCommitId() {
        return readContentsAsString(new File(currHeadRef));
    }

    public static String getCurrBranch() {
        return currBranch;
    }

    public static Map<String, String> getCommitMap() {
        return commitMap;
    }
}
