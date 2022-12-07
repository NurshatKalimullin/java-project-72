package hexlet.code;

import io.javalin.Javalin;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static io.javalin.apibuilder.ApiBuilder.*;

public class App {

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "3000");
        return Integer.valueOf(port);
    }


    private static TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");

        templateEngine.addTemplateResolver(templateResolver);
        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());

        return templateEngine;
    }

    private static void addRoutes(Javalin app) {
//        app.get("/", RootController.welcome);
//        app.get("/about", RootController.about);
//
//        app.routes(() -> {
//            path("articles", () -> {
//                get(ArticleController.listArticles);
//                post(ArticleController.createArticle);
//                get("new", ArticleController.newArticle);
//                path("{id}", () -> {
//                    get(ArticleController.showArticle);
//                });
//            });
//        });
    }

    public static Javalin getApp() {
        // Создаём приложение
        Javalin app = Javalin.create(config -> {
            // Включаем логгирование
            config.enableDevLogging();
            // Подключаем настроенный шаблонизатор к фреймворку
            JavalinThymeleaf.configure(getTemplateEngine());
        });

        // Добавляем маршруты в приложение
        addRoutes(app);

        // Обработчик before запускается перед каждым запросом
        // Устанавливаем атрибут ctx для запросов
        app.before(ctx -> {
            ctx.attribute("ctx", ctx);
        });

        return app;
    }

    public static void main(String[] args) {
        //Javalin app = getApp();
        Javalin app = Javalin.create(/*config*/)
                .get("/", ctx -> ctx.result("Hello World"))
                .start(7070);
        //app.start(getPort());
    }
}
