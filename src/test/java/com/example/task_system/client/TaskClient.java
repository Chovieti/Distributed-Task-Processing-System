package com.example.task_system.client;

import com.example.task_system.dto.CreateTaskRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import java.util.UUID;

public class TaskClient {
    public CreateTaskRequest createRequest(String payload) {
        return new CreateTaskRequest(payload, UUID.randomUUID().toString());
    }
    public CreateTaskRequest createRequest(String payload, String key) {
        return new CreateTaskRequest(payload, key);
    }

    public String createTask(CreateTaskRequest request) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/tasks")
                .then()
                .statusCode(201)
                .extract()
                .asString()
                .replace("\"", "");
    }
}
