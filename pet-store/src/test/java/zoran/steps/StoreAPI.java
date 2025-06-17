package zoran.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import zoran.api.StoreApiClient;
import zoran.models.Order;
import zoran.utils.OrderGenerator;

import java.time.OffsetDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class StoreAPI {
    private final StoreApiClient storeApi = new StoreApiClient();
    private Order testOrder;
    private Order createdOrder;
    private Map<String, Integer> inventory;
    private Exception lastException;
    private Order lastRetrievedOrder;

    @Given("I have an order with the following details:")
    public void createOrder(io.cucumber.datatable.DataTable dataTable) {
        testOrder = Order.builder()
                .id(OrderGenerator.generateRandomId())
                .petId(Long.parseLong(dataTable.cell(1, 0)))
                .quantity(Integer.parseInt(dataTable.cell(1, 1)))
                .shipDate(OffsetDateTime.now().plusDays(1))
                .status(dataTable.cell(1, 2))
                .complete(Boolean.parseBoolean(dataTable.cell(1, 3)))
                .build();
    }

    @When("I place the order")
    public void placeOrder() {
        createdOrder = storeApi.placeOrder(testOrder);
    }

    @Then("the order should be placed successfully")
    public void verifyOrderPlaced() {
        assertThat(createdOrder.getId(), notNullValue());
        assertThat(createdOrder.getPetId(), equalTo(testOrder.getPetId()));
        assertThat(createdOrder.getQuantity(), equalTo(testOrder.getQuantity()));
        assertThat(createdOrder.getStatus(), equalTo(testOrder.getStatus()));
    }

    @When("I get the order by ID")
    public void getOrderById() {
        createdOrder = storeApi.getOrderById(testOrder.getId());
    }

    @Then("the order details should be returned correctly")
    public void verifyOrderDetails() {
        assertThat(createdOrder, notNullValue());
        assertThat(createdOrder.getId(), equalTo(testOrder.getId()));
    }

    @When("I delete the order")
    public void deleteOrder() {
        storeApi.deleteOrder(testOrder.getId());
    }

    @Then("the order should not be found")
    public void verifyOrderNotFound() {
        try {
            storeApi.getOrderById(testOrder.getId());
            throw new AssertionError("Expected order to be deleted but it was found");
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("404"));
        }
    }

    @When("I get the store inventory")
    public void getStoreInventory() {
        inventory = storeApi.getInventory();
    }

    @Then("the inventory should be returned")
    public void verifyInventory() {
        assertThat(inventory, notNullValue());
        assertThat(inventory.isEmpty(), is(false));
    }

    @And("the inventory should contain available pets")
    public void theInventoryShouldContainAvailablePets() {
        Map<String, Integer> inventory = storeApi.getInventory();
        assertNotNull(inventory, "Inventory should not be null");
        assertTrue(inventory.getOrDefault("available", 0) > 0, 
                  "Inventory should contain available pets");
    }

    @Given("an order with ID {int} does not exist")
    public void anOrderWithIDDoesNotExist(int orderId) {
        try {
            storeApi.deleteOrder((long) orderId);
        } catch (Exception e) {
            // Order doesn't exist, which is what we want
        }
        
        // Verify the order doesn't exist
        try {
            storeApi.getOrderById((long) orderId);
            fail("Order with ID " + orderId + " should not exist");
        } catch (Exception e) {
            // Expected - order should not exist
            assertTrue(e.getMessage().contains("Order not found") || 
                      e.getMessage().contains("404"));
        }
    }

    @When("I attempt to retrieve the order with ID {int}")
    public void iAttemptToRetrieveTheOrderWithID(int orderId) {
        try {
            lastRetrievedOrder = storeApi.getOrderById((long) orderId);
            lastException = null;
        } catch (Exception e) {
            lastRetrievedOrder = null;
            lastException = e;
        }
    }

    @When("I attempt to delete the order with ID {int}")
    public void iAttemptToDeleteTheOrderWithID(int orderId) {
        try {
            storeApi.deleteOrder((long) orderId);
            lastException = null;
        } catch (Exception e) {
            lastException = e;
        }
    }
}
