package ru.maltsev.primeworker.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import ru.maltsev.primeworker.config.properties.GoogleProps;

import java.io.FileInputStream;
import java.util.List;

@Configuration
public class GoogleSheetsConfig {

    private final String credentialsFilePath;
    private final String appName;

    public GoogleSheetsConfig(GoogleProps props, @Value("${spring.application.name}") String appName) {
        this.credentialsFilePath = props.getCredentialsFilePath();
        this.appName = appName;
    }

    @Bean
    @Lazy
    public Sheets sheetsService() throws Exception {
        GoogleCredentials credentials =
                GoogleCredentials.fromStream(new FileInputStream(credentialsFilePath))
                        .createScoped(List.of("https://www.googleapis.com/auth/spreadsheets"));

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName(appName)
                .build();
    }
}
