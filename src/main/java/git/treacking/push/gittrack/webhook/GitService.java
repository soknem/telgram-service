package git.treacking.push.gittrack.webhook;

public interface GitService {

    void sendGitPushInfo(String repoName, String projectName, String author, String message, String branch,
                         String dateTime);
}
