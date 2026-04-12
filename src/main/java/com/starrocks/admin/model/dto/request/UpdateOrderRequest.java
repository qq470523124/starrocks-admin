package com.starrocks.admin.model.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class UpdateOrderRequest {
    private List<FunctionOrder> functions;

    @Data
    public static class FunctionOrder {
        private Long id;
        private Integer displayOrder;
        private Integer categoryOrder;
    }
}
