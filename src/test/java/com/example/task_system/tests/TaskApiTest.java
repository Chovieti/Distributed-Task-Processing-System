package com.example.task_system.tests;

import com.example.task_system.base.BaseTest;
import com.example.task_system.client.TaskClient;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;

public class TaskApiTest extends BaseTest {
    private final TaskClient taskClient = new TaskClient();

    // Обычное создание задачи
    @Test
    void createTaskTest() {
        taskClient.createTask(taskClient.createRequest("test payload"));
    }

    // Создание и проверки её существование с базовыми значениями
    @Test
    void createAndGetTaskTest() {
        String payload = "important task payload";
        String taskId = taskClient.createTask(taskClient.createRequest(payload));

        given()
                .when()
                    .get("/tasks/" + taskId)
                .then()
                    .statusCode(200)
                    .body("payload", Matchers.equalTo(payload))
                    .body("status", Matchers.equalTo("PENDING"))
                    .body("retryCount", Matchers.equalTo(0))
                    .body("id", Matchers.equalTo(taskId));
    }

    // Создание и проверки существования задачи с пустым payload
    @Test
    void createTaskWithEmptyPayload() {
        String taskId = taskClient.createTask(taskClient.createRequest(""));
        given()
                .when()
                .get("/tasks/" + taskId)
                .then()
                .statusCode(200)
                .body("payload", Matchers.equalTo(""));
    }

    // Создание двух задач с одинаковым idempotencyKey, но при получении они должны быть одинаковыми.
    // Проверка защиты на дубликаты
    @Test
    void createTaskShouldBeIdempotent() {
        String key = "fixed-key";
        String firstTaskId = taskClient.createTask(taskClient.createRequest("payload-1", key));
        String secondTaskId = taskClient.createTask(taskClient.createRequest("payload-2", key));
        assert firstTaskId.equals(secondTaskId);
        given()
                .when()
                .get("/tasks/" + firstTaskId)
                .then()
                .statusCode(200)
                .body("payload", Matchers.equalTo("payload-1"))
                .body("idempotencyKey", Matchers.equalTo(key));
    }

    // Так как у приложения пока нету обработчика ошибок, то все ошибки, которые не берет на себя спринг становятся 500
    // TODO исправить 500 ошибки

    // Поиск несуществующего таска
    @Test
    void getTaskShouldReturn500WhenTaskNotFound() {
        String randomId = UUID.randomUUID().toString();
        given()
                .when()
                .get("/tasks/" + randomId)
                .then()
                .statusCode(500);
    }

    // Поиск таска по некорректному формату uuid
    @Test
    void getTaskShoultReturn500WhenInvalidUuidFormat() {
        String invalidId = "notAUuid";
        given()
                .when()
                .get("/tasks/" + invalidId)
                .then()
                .statusCode(400);
    }

    // Проверка изменения состояния таска(пока сделанно костыльно, так как в работу воркера я не вмешиваюсь)
    @Test
    void taskShouldBeProcessed() {
        String taskId = taskClient.createTask(taskClient.createRequest("processed test"));
        awaitTaskProcessed(taskId);
    }

    // Helper
    private void awaitTaskProcessed(String taskId) {
        int maxAttempts = 30;
        int delayMs = 1000;

        for (int i = 0; i < maxAttempts; i++) {
            String status = given()
                    .when()
                    .get("/tasks/" + taskId)
                    .then()
                    .statusCode(200)
                    .extract()
                    .path("status");
            if (!status.equals("PENDING")) return;

            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        throw new AssertionError("Task was not processed in time: " + taskId);
    }
}
