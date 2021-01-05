package com.example.service;

import com.example.pojo.Content;
import com.example.utils.HTMLParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
  *  @Author Liu Haonan
  *  @Date 2020/10/11 15:48
  *  @Description 业务类
  */
@Service
public class ContentService {
    public static final String index_name = "jd_goods_index";

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     *  @Author Liu Haonan
     *  @Date 2020/10/11 15:30
     *  @Description 调用工具类解析数据，放到es索引库中
     *                将list数据批量放入es
     */
    public Boolean parseContent(String keywords) throws Exception {
        List<Content> contentList = new HTMLParser().parseJD(keywords);
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2m");//设置超时时间为2min
        for (Content content : contentList) {
            bulkRequest.add(new IndexRequest(index_name).
                    source(objectMapper.writeValueAsString(content), XContentType.JSON));
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);

        return !bulkResponse.hasFailures();
    }
    /**
      *  @Author Liu Haonan
      *  @Date 2020/10/11 16:40
      *  @Description ES构造按照关键词的查询进行分页查询
      */
    public List<Map<String,Object>> searchPage(String keyword, int pageNo, int pageSize) throws IOException {
        if(pageNo<1){//分页初始化
            pageNo = 1;
        }
        SearchRequest searchRequest = new SearchRequest("jd_goods_index");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        TermQueryBuilder matchAllQueryBuilder = QueryBuilders.termQuery("title",keyword);//构造精准查询条件
        sourceBuilder.query(matchAllQueryBuilder);//添加查询条件
        //完成条件设置
        searchRequest.source(sourceBuilder);
        //分页设置
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);
        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");//设置高亮的字段
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
//        highlightBuilder.requireFieldMatch(false);//关闭相同的多个高亮显示
        sourceBuilder.highlighter(highlightBuilder);//完成设置高亮

//        查询
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //解析结果
        ArrayList<Map<String, Object>> mapList = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();//原来的结果

            //解析高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");//获取原来的字段
            if(title!=null){
                Text[] fragments = title.fragments();//获取到title中每一个字
                String newTitle = "";
                for (Text fragment : fragments) {
                    newTitle+=fragment;
                }
                sourceAsMap.put("title",newTitle);//替换掉原来的title字段
            }

            mapList.add(hit.getSourceAsMap());
        }
        return mapList;
    }
}
