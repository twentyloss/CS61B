package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;
import static java.util.Locale.ENGLISH;

/** Represents a gitlet commit object.
 *
 *
 *  @author Xinyi Lin
 */
public class Commit implements Serializable, Comparable<Commit> {
    /**
     * meesage: the user-defined message at commit time.
     * commitDate: the timestamp of commit.
     * parent: the parent commit of this commit.
     * secondParent: the second parent of commit if the commit is made by merging operation.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private Date commitDate;
    private String parent;
    private String secondParent;
    private Map<String, String> fileMap;
    private String sha1Code;

    public Commit(String msg, Date d, String p, String sp, Map<String, String> m) {
        message = msg;
        commitDate = d;
        parent = p;
        secondParent = sp;
        fileMap = m;
        sha1();
    }


    public static Commit initialCommit() {
        return new Commit("initial commit", new Date(0), "null", "null", new TreeMap<>());
    }

    public Map<String, String> getFileMap() {
        return this.fileMap;
    }

    public void commit() {
        File hashDir = Repository.getHashDir(sha1Code);
        File c = join(hashDir, sha1Code);
        writeObject(c, this);
        File head = join(Repository.REFS, Repository.getCurrBranch());
        writeContents(head, sha1Code);
        updateBranchCommitList();
    }

    public void reset() {
        File head = join(Repository.REFS, Repository.getCurrBranch());
        writeContents(head, sha1Code);
    }

    public void log() {
        System.out.println(this);
        if (!commitDate.equals(new Date(0))) {
            Commit p = fromFile(parent);
            p.log();
        }
    }

    public static Commit generateCommit(String msg, Map<String, String> map, String parent) {
        return new Commit(msg, new Date(), parent, "null", map);
    }

    public static Commit generateCommit(String msg, Map<String, String> map,
                                        String parent, String secondParent) {
        return new Commit(msg, new Date(), parent, secondParent, map);
    }

    public static Commit fromFile(String filename) {
        File hashDir = Repository.getHashDir(filename);
        File f = join(hashDir, filename);
        if (f.exists()) {
            Commit commit = readObject(f, Commit.class);
            commit.sha1();
            return commit;
        }
        return null;
    }


    private void sha1() {
        sha1Code = Utils.sha1("commit", this.message, this.commitDate.toString(),
                  this.parent, this.secondParent, fileMap.toString());
    }

    public String toString() {
        String date = String.format(ENGLISH, "%ta %tb %td %tH:%tM:%tS %tY %tz",
                              commitDate, commitDate, commitDate, commitDate,
                              commitDate, commitDate, commitDate, commitDate);
        String merge = "";
        if (!secondParent.equals("null")) {
            merge = "Merge: " + parent.substring(0, 7) + " " + secondParent.substring(0, 7) + "\n";
        }
        return "===\n"
                + "commit " + sha1Code + "\n"
                + merge
                + "Date: " + date + "\n"
                + message + "\n";
    }

    public String getMessage() {
        return message;
    }

    public String getSha1Code() {
        return sha1Code;
    }

    private void updateBranchCommitList() {
        File commitList = join(Repository.REFS, Repository.getCurrBranch() + "CommitList");
        LinkedList<String> commits = readObject(commitList, LinkedList.class);
        commits.addFirst(sha1Code);
        writeObject(commitList, commits);
    }

    public Date getCommitDate() {
        return this.commitDate;
    }

    public int compareTo(Commit c) {
        return this.commitDate.compareTo(c.getCommitDate());
    }

    public boolean equals(Commit c) {
        return this.sha1Code.equals(c.getSha1Code());
    }

    public String getSecondParent() {
        return this.secondParent;
    }
}
