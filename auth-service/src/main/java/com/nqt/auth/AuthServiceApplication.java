package com.nqt.auth;

//import net.devh.boot.grpc.server.security.authentication.GrpcServerSecurityAutoConfiguration;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerSecurityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = { GrpcServerSecurityAutoConfiguration.class })
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}