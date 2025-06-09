package com.rose.back.infra.S3;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class ImageUrlExtractor {

    public List<String> extractImageUrls(String html) {
        Document doc = Jsoup.parse(html);
        Elements images = doc.select("img");

        return images.stream()
                .map(img -> img.attr("src"))
                .filter(url -> url != null && !url.isBlank())
                .toList();
    }
}
