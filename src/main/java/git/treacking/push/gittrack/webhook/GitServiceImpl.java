package git.treacking.push.gittrack.webhook;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class GitServiceImpl implements GitService {

    @Value("${telegram.bot.khotixs-token}")
    private String botToken1;

    @Value("${telegram.bot.khotixs-id}")
    private String chatId1;

    @Value("${telegram.bot.khotixs-topic}")
    private String messageThreadId1;

    @Value("${telegram.bot.flutter-token}")
    private String botToken2;

    @Value("${telegram.bot.flutter-id}")
    private String chatId2;

    @Value("${telegram.bot.flutter-topic}")
    private String messageThreadId2;

    private final RestTemplate restTemplate;


    @Override
    public void sendGitPushInfo(String repoName, String projectName, String author, String message, String branch,
                                String dateTime) {
        String botToken;
        String topic = null;
        String chatId = switch (projectName) {
            case "khotixs" -> {
                topic = messageThreadId1;
                botToken = botToken1;
                yield chatId1;
            }
            case "flutter" -> {
                topic = messageThreadId2;
                botToken = botToken2;
                yield chatId2;
            }
            default -> throw new IllegalArgumentException("Unknown project name: " + projectName);
        };

        // Format dateTime to dd/MM/yyyy HH:mm:ss
        String formattedDateTime = formatDateTime(dateTime);

        // Adjust message format to use Markdown and clickable links
        String telegramMessage = String.format(""" 
                🎉 *NEW ACTION!* 🎉 
                ----------------------------------------
                
                • *Repository* : `%s` 
                • *Author*        : `%s` 
                • *Message*     : `%s` 
                • *Branch*        : `%s` 
                • *DateTime*   : `%s` 
                ----------------------------------------
                """, repoName, author, message, branch, formattedDateTime);


        String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);
        System.out.println(url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Add message_thread_id if provided
        String requestJson;
        if (topic != null) {
            requestJson = String.format("{\"chat_id\":\"%s\",\"text\":\"%s\",\"message_thread_id\":%s,\"parse_mode\":\"Markdown\"}",
                    chatId, telegramMessage, topic);
        } else {
            requestJson = String.format("{\"chat_id\":\"%s\",\"text\":\"%s\",\"parse_mode\":\"Markdown\"}",
                    chatId, telegramMessage);
        }

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to send message to Telegram");
        }
    }


    private String formatDateTime(String dateTime) {
        try {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            return offsetDateTime.format(formatter);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Failed to parse date time: " + dateTime, e);
        }
    }
}
