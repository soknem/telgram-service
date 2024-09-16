package git.treacking.push.gittrack;

import git.treacking.push.gittrack.TelegramService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/webhook/lms")
public class WebhookController {

    private final TelegramService telegramService;

    @PostMapping(value = "/api", consumes = "application/json")
    public void handleApiWebhook(@RequestBody Map<String, Object> payload, @RequestHeader(value = "X-Gitlab-Token", required = false) String token) {
        processWebhookPayload(payload, "API");
    }

    @PostMapping(value = "/admin", consumes = "application/json")
    public void handleAdminWebhook(@RequestBody Map<String, Object> payload, @RequestHeader(value = "X-Gitlab-Token", required = false) String token) {
        processWebhookPayload(payload, "ADMIN");
    }

    @PostMapping(value = "/portal", consumes = "application/json")
    public void handlePortalWebhook(@RequestBody Map<String, Object> payload, @RequestHeader(value = "X-Gitlab-Token", required = false) String token) {
        processWebhookPayload(payload, "PORTAL");
    }

    @PostMapping(value = "/csharp", consumes = "application/json")
    public void handleCsharpWebhook(@RequestBody Map<String, Object> payload, @RequestHeader(value = "X-Gitlab-Token",
            required = false) String token) {
        processWebhookPayload(payload, "PetManagement");
    }

    private void processWebhookPayload(Map<String, Object> payload, String projectName) {

        String ref = (String) payload.get("ref");
        String branch = ref.replace("refs/heads/", "");

        List<Map<String, Object>> commits = (List<Map<String, Object>>) payload.get("commits");

        for (Map<String, Object> commit : commits) {
            Map<String, String> author = (Map<String, String>) commit.get("author");
            String authorName = author.get("name");
            String message = (String) commit.get("message");
            String dateTime = (String) commit.get("timestamp");

            telegramService.sendGitPushInfo(projectName, authorName, message, branch, dateTime);
        }
    }
}
