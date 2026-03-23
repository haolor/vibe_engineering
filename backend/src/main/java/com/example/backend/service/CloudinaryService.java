package com.example.backend.service;

import com.example.backend.dto.ImageUploadResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Service
public class CloudinaryService {
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final String folder;

    public CloudinaryService(
            ObjectMapper objectMapper,
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret,
            @Value("${cloudinary.folder:vibe_engineering}") String folder
    ) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.folder = folder;
    }

    public ImageUploadResponse uploadImage(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File anh khong hop le");
            }
            if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
                throw new IllegalArgumentException("Thieu cau hinh Cloudinary");
            }

            String mimeType = file.getContentType() == null ? "image/jpeg" : file.getContentType();
            String dataUri = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(file.getBytes());
            long timestamp = Instant.now().getEpochSecond();
            String signature = sha1Hex("folder=" + folder + "&timestamp=" + timestamp + apiSecret);

            String payload = "file=" + urlEncode(dataUri)
                    + "&api_key=" + urlEncode(apiKey)
                    + "&timestamp=" + timestamp
                    + "&folder=" + urlEncode(folder)
                    + "&signature=" + urlEncode(signature);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Cloudinary HTTP " + response.statusCode() + ": " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            ImageUploadResponse out = new ImageUploadResponse();
            out.setSecureUrl(root.path("secure_url").asText());
            out.setPublicId(root.path("public_id").asText());
            return out;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Upload anh that bai", e);
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String sha1Hex(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
