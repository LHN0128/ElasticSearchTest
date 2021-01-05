package com.example.controller;


import com.example.service.ContentService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class ContentController {
    @Autowired
    private ContentService contentService;

    /**
      *  @Author Liu Haonan
      *  @Date 2020/10/11 16:02
      *  @Description 抓取参数的数据到elasticsearch中
      */
    @GetMapping("/parse/{keyword}")
    public Boolean parse(@PathVariable("keyword") String keyword) throws Exception{
        return contentService.parseContent(keyword);
    }
    /**
      *  @Author Liu Haonan
      *  @Date 2020/10/11 16:46
      *  @Description 调用业务接口实现elasticsearch精准匹配的分页查询
      */
    @GetMapping("/search/{keyword}/{pageNo}/{pageSize}")
    public List<Map<String,Object>> searchByKeywords(@PathVariable("keyword") String keyword,
                                                     @PathVariable("pageNo")int pageNo,
                                                     @PathVariable("pageSize")int pageSize) throws IOException {
        return contentService.searchPage(keyword,pageNo,pageSize);
    }


}
