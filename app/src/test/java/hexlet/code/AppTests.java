package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

class AppTests {


    private static Javalin app;
    private static String baseUrl;
    private static Transaction transaction;



    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }


    // При использовании БД запускать каждый тест в транзакции -
    // является хорошей практикой
    @BeforeEach
    void beforeEach() {
        transaction = DB.beginTransaction();
    }

    @AfterEach
    void afterEach() {
        transaction.rollback();
    }


    @Test
    void testRoot() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        assertThat(response.getStatus()).isEqualTo(200);
    }


    @Test
    void testAddUrl() {

        String actualUrl = "https://www.example.com";

        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("name", actualUrl)
                .asString();

        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");


        //check DB
        Url postedUrl = new QUrl()
                .name.equalTo(actualUrl)
                .findOne();

        assertThat(postedUrl).isNotNull();
        assertThat(postedUrl.getName()).isEqualTo(actualUrl);

        //Check the URL on the page
        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains(actualUrl);
        assertThat(content).doesNotContain("East of Eden");

    }

}
