package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.HttpCode;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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


        PagedList<UrlCheck> pagedChecks = new QUrlCheck()
                .setFirstRow(0)
                .setMaxRows(1000)
                .url.name.equalTo(url.getName())
                .orderBy()
                .id.asc()
                .findPagedList();

        List<UrlCheck> checks = pagedChecks.getList();

        ctx.attribute("checks", checks);
        ctx.attribute("url", url);

        ctx.render("check.html");
    };

    public static Handler checkUrl = ctx -> {

        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        HttpResponse<String> responseGet = Unirest
                .post(url.getName())
                .asString();

        String content = responseGet.getBody();

        Document body = Jsoup.parse(content);

        String h1 = body.selectFirst("h1") != null
                ? Objects.requireNonNull(body.selectFirst("h1")).text()
                : null;
        String description = body.selectFirst("meta[name=description]") != null
                ? Objects.requireNonNull(body.selectFirst("meta[name=description]")).attr("content")
                : null;


        UrlCheck urlCheck = new UrlCheck(responseGet.getStatus(), body.title(), h1, description, url);

        urlCheck.save();

        ctx.sessionAttribute("flash", "Страница успешно проверена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls/" + id);
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
