package com.example.utils;

import com.example.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;


/**
  *  @Author Liu Haonan
  *  @Date 2020/10/11 14:03
  *  @Description 爬取页面内容工具类
  */
@Component
public class HTMLParser {
    /**
      *  @Author Liu Haonan
      *  @Date 2020/10/11 18:09
      *  @Description 解决ssl验证问题
      */
     static {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[] { new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            } }, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) {
             e.printStackTrace();
        }
    }
    /**
      *  @Author Liu Haonan
      *  @Date 2020/10/11 18:08
      *  @Description 爬取内容
      */
    public List<Content> parseJD(String keywords) throws IOException{
        //获取请求
        String url = "https://search.jd.com/Search?keyword="+keywords+"&enc=utf-8";
        //解析网页：document就是浏览器的dom对象！
        Document document = Jsoup.parse(new URL(url), 30000);
        //获取列表元素
        Element j_goodsList = document.getElementById("J_goodsList");

        List<Content> contentList = new ArrayList<>();
        ///获取列表项
//        System.out.println(document.html());//通过查看页面的所有内容能够查看到图片在哪个属性中
        Elements elements = j_goodsList.getElementsByTag("li");
        for (Element element : elements) {//往往由于图片是延迟加载，提升性能。因此不能直接获取img,要获取它的懒加载路径
            String img = element.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = element.getElementsByClass("p-price").eq(0).text();
            String name = element.getElementsByClass("p-name").eq(0).text();
            contentList.add(new Content(name,img,price));

        }
        return contentList;
    }
    public static void main(String[] args) throws IOException {

        new HTMLParser().parseJD("python").forEach(System.out::println);

    }
}
