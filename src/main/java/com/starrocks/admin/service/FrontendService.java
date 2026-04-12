package com.starrocks.admin.service;

import com.starrocks.admin.client.StarRocksHttpClient;
import com.starrocks.admin.model.dto.response.FrontendResponse;
import com.starrocks.admin.model.entity.Cluster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FrontendService {

    private final StarRocksHttpClient starRocksHttpClient;

    public List<FrontendResponse> listFrontends(Cluster cluster) {
        return starRocksHttpClient.getFrontends(cluster);
    }
}
