package com.ecom.common.bean;

import lombok.Data;

import java.util.List;

@Data
public class OrderBean {
    private Long id;
    private Long user_id;
    private Long address_id;
    private String stage;
    private String payment_status;
    private Double total_amount;
    private String stripe_session_id;
    private String stripe_checkout_url;
    private String created_at;
    private String updated_at;
    private List<OrderProduct> products;

    @Data
    static public class OrderProduct {
        private Long id;
        private Long order_id;
        private Long product_id;

        // Detail
        private ProductBean productDetail;
    }

}
