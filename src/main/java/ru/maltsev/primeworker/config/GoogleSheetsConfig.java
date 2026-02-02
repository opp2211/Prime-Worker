package ru.maltsev.primeworker.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

@Configuration
public class GoogleSheetsConfig {

    @Value("${spring.application.name}")
    private String appName;

    @Bean
    public Sheets sheetsService() throws Exception {
        InputStream credentialsStream = getClass().getClassLoader().getResourceAsStream("secrets/google-credentials.json");

        GoogleCredentials credentials =
                GoogleCredentials.fromStream(Objects.requireNonNull(credentialsStream))
                        .createScoped(List.of("https://www.googleapis.com/auth/spreadsheets"));

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName(appName)
                .build();
    }
}
