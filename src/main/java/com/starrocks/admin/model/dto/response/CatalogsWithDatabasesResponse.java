package com.starrocks.admin.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CatalogsWithDatabasesResponse {
    private List<CatalogWithDatabases> catalogs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CatalogWithDatabases {
        private String catalog;
        private List<String> databases;
    }
}
