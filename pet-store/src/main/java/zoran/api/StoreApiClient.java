package zoran.api;

import zoran.models.Order;
import java.util.Map;

public class StoreApiClient extends BaseApiClient {
    
    public Map<String, Integer> getInventory() {
        return get("/store/inventory")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getMap("");
    }
    
    public Order placeOrder(Order order) {
        return post(order, "/store/order")
                .then()
                .statusCode(200)
                .extract()
                .as(Order.class);
    }
    
    public Order getOrderById(Long orderId) {
        return get("/store/order/{orderId}", orderId)
                .then()
                .statusCode(200)
                .extract()
                .as(Order.class);
    }
    
    public void deleteOrder(Long orderId) {
        delete("/store/order/{orderId}", orderId)
                .then()
                .statusCode(200);
    }
}
