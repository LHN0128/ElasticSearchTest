package com.example.springdataes;

import com.example.pojo.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class SpringdataesApplicationTests {
	public static final String index_name = "test-springdata-es";

	@Autowired
	private RestHighLevelClient restHighLevelClient;

	/**
	 *  @Author Liu Haonan
	 *  @Date 2020/10/10 14:51
	 *  @Description 测试索引的创建
	 */
	@Test
	public void testCreateIndex() throws IOException {
//       1、创建索引请求
		CreateIndexRequest request = new CreateIndexRequest(index_name);
//       2、执行创建请求
		CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
		System.out.println(createIndexResponse.index());
	}
	/**
	 *  @Author Liu Haonan
	 *  @Date 2020/10/10 15:08
	 *  @Description 获取索引库，测试判断索引是否存在
	 */
	@Test
	public void testIndexExist() throws IOException {
		GetIndexRequest request = new GetIndexRequest(index_name);
		boolean exists = restHighLevelClient.indices().exists(request,RequestOptions.DEFAULT);

	}
	/**
	 *  @Author Liu Haonan
	 *  @Date 2020/10/10 15:20
	 *  @Description 测试删除索引库
	 */
	@Test
	public void testDeleteIndex() throws IOException {
		DeleteIndexRequest deleteRequest = new DeleteIndexRequest(index_name);
		AcknowledgedResponse deleteResponse = restHighLevelClient.indices().delete(deleteRequest,RequestOptions.DEFAULT);

	}
	/**
	 *  @Author Liu Haonan
	 *  @Date 2020/10/10 15:21
	 *  @Description 测试添加文档
	 */
	@Test
	public void testAddDocuments() throws IOException {
		//创建对象
		User user  =  new User("zhangsan",12);
		//创建到指定索引库的请求
		IndexRequest request = new IndexRequest(index_name);
		request.id("1");
		request.timeout();
		//传入参数
		ObjectMapper objectMapper = new ObjectMapper();
		String s = objectMapper.writeValueAsString(user);
		//将json参数放入请求
		request.source(s, XContentType.JSON);
		//发送请求
		IndexResponse indexResponse = restHighLevelClient.index(request, RequestOptions.DEFAULT);
		System.out.println(indexResponse.toString());//
		System.out.println(indexResponse.status());
	}
	/**
	 *  @Author Liu Haonan
	 *  @Date 2020/10/10 15:52
	 *  @Description 测试获取文档，判断是否存在
	 */
	@Test
	public void testDocIsExist() throws IOException {
		GetRequest getRequest = new GetRequest(index_name,"1");
//        //不获取_source的上下文
//        getRequest.fetchSourceContext(new FetchSourceContext(false));
//        getRequest.storedFields("_none_");
		//获取请求的文档对象
		GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
		System.out.println(getResponse.getSourceAsString());
	}
	/**
	 *  @Author Liu Haonan
	 *  @Date 2020/10/10 16:23
	 *  @Description 测试更新文档
	 */
	@Test
	public void testUpdateDoc() throws IOException {
		User user = new User("lisi",13);
		UpdateRequest updateRequest = new UpdateRequest(index_name,"1");
		//设置请求超时时间
		updateRequest.timeout("1s");
		//修改文档内容
		updateRequest.doc(new ObjectMapper().writeValueAsString(user), XContentType.JSON);
		restHighLevelClient.update(updateRequest,RequestOptions.DEFAULT);
	}
	/**
	 *  @Author Liu Haonan
	 *  @Date 2020/10/10 16:35
	 *  @Description 测试删除请求
	 */
	@Test
	public void testDeleteDoc() throws IOException {
		DeleteRequest deleteRequest = new DeleteRequest(index_name,"1");
		deleteRequest.timeout("1s");
		DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
		System.out.println(deleteResponse.status());
	}
	/**
	 *  @Author Liu Haonan
	 *  @Date 2020/10/10 16:44
	 *  @Description 测试批量插入数据,使用bulkrequest
	 *                批量更新、删除类似
	 */
	@Test
	public void testAddBulkDocs() throws IOException {
		BulkRequest bulkRequest = new BulkRequest();
		bulkRequest.timeout("10s");
		List<User> userList = new ArrayList<>();
		userList.add(new User("zhangsan",1));
		userList.add(new User("lisi",2));
		userList.add(new User("wangwu",3));
		for (int i = 0; i < userList.size(); i++) {
			bulkRequest.add(new IndexRequest(index_name).id(""+(i+1)).//可以不设置id让其生成随机id
					source(new ObjectMapper().writeValueAsString(userList.get(i)),XContentType.JSON));
		}
		BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
		RestStatus status = bulkResponse.status();
		System.out.println(status);

	}
	/**
	 *  @Author Liu Haonan
	 *  @Date 2020/10/10 17:00
	 *  @Description 测试按照条件搜索---精确匹配或查询所有
	 */
	@Test
	public void testSearchTerm() throws IOException {
		//创建搜索请求对象
		SearchRequest searchRequest = new SearchRequest(index_name);
		//构造搜索条件
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        //设置查询条件--精确匹配
//        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "zhangsan");
		//设置查询条件--查询所有
		MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
		searchSourceBuilder.query(matchAllQueryBuilder);
		searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		searchRequest.source(searchSourceBuilder);//构建请求完成
		//执行请求
		SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		//遍历查询结果
		System.out.println("================");
		for (SearchHit hit : searchResponse.getHits().getHits()) {
			System.out.println(hit.getSourceAsMap());
		}
	}

}
