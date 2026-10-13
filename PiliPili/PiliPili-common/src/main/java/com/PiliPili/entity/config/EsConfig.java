package com.PiliPili.entity.config;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

import javax.annotation.Resource;

public class EsConfig extends AbstractElasticsearchConfiguration implements DisposableBean {

    @Resource
    AppConfig appConfig;

    private RestHighLevelClient restHighLevelClient;

    @Override
    public void destroy() throws Exception {
        if (restHighLevelClient != null) {
            restHighLevelClient.close();
        }
    }

    @Override
    public RestHighLevelClient elasticsearchClient() {
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder().connectedTo(appConfig.getEsHostPort()).build();
        restHighLevelClient = RestClients.create(clientConfiguration).rest();
        return restHighLevelClient;
    }
}
