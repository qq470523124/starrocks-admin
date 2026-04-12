package com.starrocks.admin.service;

import com.starrocks.admin.client.StarRocksHttpClient;
import com.starrocks.admin.model.dto.response.*;
import com.starrocks.admin.model.entity.Cluster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackendService {

    private final StarRocksHttpClient starRocksHttpClient;

    public java.util.List<BackendResponse> listBackends(Cluster cluster) {
        return starRocksHttpClient.getBackends(cluster);
    }

    public void dropBackend(Cluster cluster, String host, String port) {
        starRocksHttpClient.dropBackend(cluster, host, port);
    }
}
