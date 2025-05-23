package org.fortishop.notificationservice;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.fortishop.notificationservice.domain.Notification;
import org.fortishop.notificationservice.domain.NotificationSetting;
import org.fortishop.notificationservice.domain.NotificationStatus;
import org.fortishop.notificationservice.domain.NotificationTemplate;
import org.fortishop.notificationservice.domain.NotificationType;
import org.fortishop.notificationservice.dto.request.NotificationReadRequest;
import org.fortishop.notificationservice.dto.request.NotificationSettingRequest;
import org.fortishop.notificationservice.dto.request.NotificationTemplateRequest;
import org.fortishop.notificationservice.repository.NotificationRepository;
import org.fortishop.notificationservice.repository.NotificationSettingRepository;
import org.fortishop.notificationservice.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.config.location=classpath:/application-test.yml",
                "spring.profiles.active=test"
        }
)
@Testcontainers
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NotificationServiceIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    NotificationSettingRepository settingRepository;

    @Autowired
    NotificationTemplateRepository templateRepository;

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("fortishop")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> zookeeper = new GenericContainer<>(DockerImageName.parse("bitnami/zookeeper:3.8.1"))
            .withExposedPorts(2181)
            .withEnv("ALLOW_ANONYMOUS_LOGIN", "yes")
            .withNetwork(Network.SHARED)
            .withNetworkAliases("zookeeper");

    @Container
    static GenericContainer<?> kafka = new GenericContainer<>(DockerImageName.parse("bitnami/kafka:3.6.0"))
            .withExposedPorts(9092, 9093)
            .withNetwork(Network.SHARED)
            .withNetworkAliases("kafka")
            .withCreateContainerCmdModifier(cmd -> cmd.withHostName("kafka").withHostConfig(
                    Objects.requireNonNull(cmd.getHostConfig())
                            .withPortBindings(
                                    new PortBinding(Ports.Binding.bindPort(9092), new ExposedPort(9092)),
                                    new PortBinding(Ports.Binding.bindPort(9093), new ExposedPort(9093))
                            )
            ))
            .withEnv("KAFKA_BROKER_ID", "1")
            .withEnv("ALLOW_PLAINTEXT_LISTENER", "yes")
            .withEnv("KAFKA_CFG_ZOOKEEPER_CONNECT", "zookeeper:2181")
            .withEnv("KAFKA_CFG_LISTENERS", "PLAINTEXT://0.0.0.0:9092,EXTERNAL://0.0.0.0:9093")
            .withEnv("KAFKA_CFG_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:9092,EXTERNAL://localhost:9093")
            .withEnv("KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP", "PLAINTEXT:PLAINTEXT,EXTERNAL:PLAINTEXT")
            .withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "PLAINTEXT")
            .withEnv("KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE", "true")
            .waitingFor(Wait.forLogMessage(".*\\[KafkaServer id=\\d+] started.*\\n", 1));

    @Container
    static GenericContainer<?> kafkaUi = new GenericContainer<>(DockerImageName.parse("provectuslabs/kafka-ui:latest"))
            .withExposedPorts(8080)
            .withEnv("KAFKA_CLUSTERS_0_NAME", "fortishop-cluster")
            .withEnv("KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS", "PLAINTEXT://kafka:9092")
            .withEnv("KAFKA_CLUSTERS_0_ZOOKEEPER", "zookeeper:2181")
            .withNetwork(Network.SHARED)
            .withNetworkAliases("kafka-ui");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        mysql.start();
        zookeeper.start();
        kafka.start();
        kafkaUi.start();

        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.kafka.bootstrap-servers", () -> kafka.getHost() + ":" + kafka.getMappedPort(9093));
    }

    private static boolean topicCreated = false;

    @BeforeAll
    void initKafkaTopics() throws Exception {
        System.out.println("Kafka UI is available at: http://" + kafkaUi.getHost() + ":" + kafkaUi.getMappedPort(8080));
        String bootstrapServers = kafka.getHost() + ":" + kafka.getMappedPort(9093);
        List<String> topics = List.of(
                "payment.completed",
                "payment.failed",
                "point.changed",
                "delivery.started",
                "delivery.completed"
        );
        if (!topicCreated) {
            for (String topic : topics) {
                createTopicIfNotExists(topic, bootstrapServers);
            }
            topicCreated = true;
        }
    }

    private static void createTopicIfNotExists(String topic, String bootstrapServers) {
        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        try (AdminClient admin = AdminClient.create(config)) {
            Set<String> existingTopics = admin.listTopics().names().get(3, TimeUnit.SECONDS);
            if (!existingTopics.contains(topic)) {
                admin.createTopics(List.of(new NewTopic(topic, 1, (short) 1)))
                        .all().get(3, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            throw new RuntimeException("Kafka 토픽 생성 실패: " + topic, e);
        }
    }

    @BeforeEach
    void cleanDb() {
        notificationRepository.deleteAll();
        settingRepository.deleteAll();
    }

    protected String getBaseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    @DisplayName("알림 목록 조회 - 성공")
    void getNotifications_success() {
        notificationRepository.save(new Notification(1L, NotificationType.ORDER, "테스트 메시지"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-member-id", "1");

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl("/api/notifications"), HttpMethod.GET,
                new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("테스트 메시지");
    }

    @Test
    @DisplayName("알림 읽음 처리 - 성공")
    void markAsRead_success() {
        Notification saved = notificationRepository.save(new Notification(1L, NotificationType.ORDER, "메시지"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-member-id", "1");

        NotificationReadRequest request = new NotificationReadRequest(List.of(saved.getId()));

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl("/api/notifications/read"), HttpMethod.POST,
                new HttpEntity<>(request, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Notification updated = notificationRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(NotificationStatus.READ);
    }

    @Test
    @DisplayName("알림 삭제 - 성공")
    void deleteNotification_success() {
        Notification saved = notificationRepository.save(
                new Notification(1L, NotificationType.ORDER, "삭제 테스트"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-member-id", "1");

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl("/api/notifications/" + saved.getId()), HttpMethod.DELETE,
                new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(notificationRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("알림 수신 설정 조회 - 기본 설정")
    void getSettings_default() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-member-id", "1");

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl("/api/notifications/settings"), HttpMethod.GET,
                new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("orderEnabled");
    }

    @Test
    @DisplayName("알림 수신 설정 변경 - 성공")
    void updateSettings_success() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-member-id", "1");

        NotificationSettingRequest request = new NotificationSettingRequest(NotificationType.ORDER, false);

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl("/api/notifications/settings"), HttpMethod.PUT,
                new HttpEntity<>(request, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        NotificationSetting setting = settingRepository.findById(1L).orElseThrow();
        assertThat(setting.isOrderEnabled()).isFalse();
    }

    @Test
    @DisplayName("관리자 템플릿 등록 - 성공")
    void createTemplate_success() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-member-role", "ROLE_ADMIN");

        NotificationTemplateRequest request = new NotificationTemplateRequest(NotificationType.POINT, "적립 알림",
                "포인트가 적립되었습니다.");

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl("/api/notifications/templates"), HttpMethod.POST,
                new HttpEntity<>(request, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("관리자 템플릿 수정 - 성공")
    void updateTemplate_success() {
        NotificationTemplate saved = new NotificationTemplate(null, NotificationType.ORDER, "제목", "내용",
                LocalDateTime.now());
        saved = templateRepository.save(saved);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-member-role", "ROLE_ADMIN");

        NotificationTemplateRequest request = new NotificationTemplateRequest(NotificationType.ORDER, "수정 제목",
                "수정 메시지");

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl("/api/notifications/templates/" + saved.getId()), HttpMethod.PUT,
                new HttpEntity<>(request, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("관리자 템플릿 삭제 - 성공")
    void deleteTemplate_success() {
        NotificationTemplate saved = templateRepository.save(
                new NotificationTemplate(null, NotificationType.SYSTEM, "삭제 제목", "삭제 메시지", LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-member-role", "ROLE_ADMIN");

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl("/api/notifications/templates/" + saved.getId()), HttpMethod.DELETE,
                new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("관리자 템플릿 목록 조회")
    void getAllTemplates_success() {
        templateRepository.save(
                new NotificationTemplate(null, NotificationType.ORDER, "목록 제목", "내용", LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-member-role", "ROLE_ADMIN");

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl("/api/notifications/templates"), HttpMethod.GET,
                new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("목록 제목");
    }

    @Test
    @DisplayName("관리자 알림 재전송 - 성공")
    void resendNotification_success() {
        Notification saved = notificationRepository.save(
                new Notification(1L, NotificationType.ORDER, "재전송 테스트"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-member-role", "ROLE_ADMIN");

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl("/api/notifications/resend/" + saved.getId()), HttpMethod.POST,
                new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("재전송 테스트");
    }

    @Test
    @DisplayName("관리자 알림 조건 검색 - memberId, type, status")
    void searchNotification_success() {
        Notification saved = notificationRepository.save(
                new Notification(2L, NotificationType.POINT, "검색용"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-member-role", "ROLE_ADMIN");

        ResponseEntity<String> response = restTemplate.exchange(
                getBaseUrl("/api/notifications/search?memberId=2&type=POINT&status=UNREAD"), HttpMethod.GET,
                new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("검색용");
    }
}
