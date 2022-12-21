package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.HttpCode;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public final class UrlController {


    public static boolean validateUrl(String urlString) {
        try {
            new URL(urlString).toURI();
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
        return true;
    }


    public static String normalizeUrl(String urlString) throws MalformedURLException {
        URL url = new URL(urlString);
        String port = url.getPort() == -1 ? "" : ":" + url.getPort();
        return url.getProtocol() + "://" + url.getHost() + port;
    }

    public static Handler displayUrl = ctx -> {

        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        ctx.attribute("url", url);

        ctx.render("check.html");
    };

    public static Handler checkUrl = ctx -> {

//        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
//
//        Url url = new QUrl()
//                .id.equalTo(id)
//                .findOne();
//
//        ctx.attribute("url", url);
//
//        ctx.render("check.html");
    };
    public static Handler createUrl = ctx -> {

        String body = ctx.formParam("name");
        System.out.println("entered URL is " + body);

        if (!validateUrl(body)) {
            System.out.println("Not valid URL");
            ctx.status(HttpCode.BAD_REQUEST);
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }
        String urlHost = new URL(body).getHost();
        System.out.println("HOST is " + urlHost);
        if (checkExistence(urlHost)) {
            ctx.status(HttpCode.BAD_REQUEST);
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.redirect("/");
            return;
        }
        String urlForDB = normalizeUrl(body);
        Url url = new Url(urlForDB);


        System.out.println("We are ready to save");
        url.save();
        System.out.println("URL is saved");
        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };

    public static Handler listUrls = ctx -> {

        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int rowsPerPage = 10;
        int offset = (page - 1) * rowsPerPage;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(offset)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        ctx.attribute("urls", urls);
        ctx.attribute("page", page);
        ctx.render("show.html");
    };

    private static boolean checkExistence(String urlHost) {
        System.out.println("Checking existing in DB");
        return new QUrl()
                .name.contains(urlHost)
                .exists();
    }
}
