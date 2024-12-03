package git.treacking.push.gittrack.webhook;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/git-webhook")
public class GitController {

    private final GitService gitservice;

    @PostMapping(value = "/khotixs", consumes = "application/json")
    public void handleKhotixsWebhook(@RequestBody Map<String, Object> payload, @RequestHeader(value = "X-Gitlab-Token",
            required = false) String token) {
        processWebhookPayload(payload, "khotixs");
    }

    @PostMapping(value = "/flutter", consumes = "application/json")
    public void handleFlutterWebhook(@RequestBody Map<String, Object> payload, @RequestHeader(value = "X-Gitlab-Token",
            required = false) String token) {
        processWebhookPayload(payload, "flutter");
    }

    private void processWebhookPayload(Map<String, Object> payload, String projectName) {

        String ref = (String) payload.get("ref");
        String branch = ref.replace("refs/heads/", "");

        Map<String, Object> repository = (Map<String, Object>) payload.get("repository");
        String repoName = (String) repository.get("name");

        List<Map<String, Object>> commits = (List<Map<String, Object>>) payload.get("commits");

        for (Map<String, Object> commit : commits) {
            Map<String, String> author = (Map<String, String>) commit.get("author");
            String authorName = author.get("name");
            String message = (String) commit.get("message");
            String dateTime = (String) commit.get("timestamp");

            gitservice.sendGitPushInfo(repoName,projectName, authorName, message, branch, dateTime);
        }
    }
}
