package com.fu.flix.service.impl;

import com.fu.flix.configuration.AppConf;
import com.google.firebase.FirebaseApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import javax.transaction.Transactional;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@Transactional
class FCMInitializerServiceImplTest {
    @Mock
    AppConf appConf;
    FCMInitializerServiceImpl underTest;

    @BeforeEach
    void setUp() throws IOException {
        appConf = new AppConf();
        appConf.setFirebaseConfig("flix-cb844-firebase-adminsdk-297yq-b523f2743e.json");

        underTest = new FCMInitializerServiceImpl(appConf);
    }

    @Test
    void test_initialize() throws IOException {
        underTest.initialize();
        underTest.initialize();
    }
}