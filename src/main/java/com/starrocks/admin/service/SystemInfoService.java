package com.starrocks.admin.service;

import com.starrocks.admin.client.StarRocksHttpClient;
import com.starrocks.admin.model.dto.response.RuntimeInfoResponse;
import com.starrocks.admin.model.entity.Cluster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemInfoService {

    private final StarRocksHttpClient starRocksHttpClient;

    public RuntimeInfoResponse getRuntimeInfo(Cluster cluster) {
        return starRocksHttpClient.getRuntimeInfo(cluster);
    }
}
