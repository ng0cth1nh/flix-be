package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.fu.flix.service.FCMInitializer;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;


import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
@Slf4j
public class FCMInitializerImpl implements FCMInitializer {
    private final AppConf appConf;
    private FirebaseApp firebaseApp;
    public  FCMInitializerImpl(AppConf appConf){
        this.appConf= appConf;
    }

    @Override
    @PostConstruct
    public void initialize() throws IOException{
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(appConf.getFirebaseConfig()).getInputStream())).build();

            if (FirebaseApp.getApps().isEmpty()) {
                this.firebaseApp = FirebaseApp.initializeApp(options);
            } else {
                this.firebaseApp = FirebaseApp.getInstance();
            }
        } catch (IOException e) {
            log.error("Create FirebaseApp Error", e);
        }
    }
}
