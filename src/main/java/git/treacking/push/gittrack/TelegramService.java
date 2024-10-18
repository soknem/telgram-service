package git.treacking.push.gittrack;

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
public class TelegramService {

    @Value("${telegram.bot.token1}")
    private String botToken1;

    @Value("${telegram.bot.id1}")
    private String chatId1;

    @Value("${telegram.bot.token2}")
    private String botToken2;

    @Value("${telegram.bot.id2}")
    private String chatId2;

    @Value("${telegram.bot.token3}")
    private String botToken3;

    @Value("${telegram.bot.id3}")
    private String chatId3;

    @Value("${telegram.bot.token4}")
    private String botToken4;

    @Value("${telegram.bot.id4}")
    private String chatId4;

    @Value("${telegram.bot.topic41}")
    private String messageThreadId;

    @Value("${telegram.bot.token5}")
    private String botToken5;

    @Value("${telegram.bot.id5}")
    private String chatId5;

    @Value("${telegram.bot.topic5}")
    private String messageThreadId5;

    private final RestTemplate restTemplate;


    public void sendGitPushInfo(String repoName, String projectName, String author, String message, String branch,
                                String dateTime) {
        String botToken;
        String topic = null;
        String chatId = switch (projectName) {
            case "API" -> {
                botToken = botToken1;
                yield chatId1;
            }
            case "ADMIN" -> {
                botToken = botToken2;
                yield chatId2;
            }
            case "PORTAL" -> {
                botToken = botToken3;
                yield chatId3;
            }
            case "PetManagement" -> {
                topic = messageThreadId;
                botToken = botToken4;
                yield chatId4;
            }
            case "Testing" -> {
                topic = messageThreadId5;
                botToken = botToken5;
                yield chatId5;
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
