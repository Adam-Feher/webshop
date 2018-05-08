package com.codecool.shop.dao.implementation;

import com.codecool.shop.dao.ModelAssembler;
import com.codecool.shop.dao.ShoppingCartDao;
import com.codecool.shop.dao.utils.QueryProcessor;
import com.codecool.shop.model.ShoppingCart;

import java.util.*;

public class ShoppingCartDaoPSQL implements ShoppingCartDao {

    ModelAssembler<ShoppingCart> assembler = rs -> {
        Map<Integer, Integer> orders = new HashMap<>();
        int[] productIds = (int[])(rs.getArray("product_ids").getArray());
        int[] quantities = (int[])(rs.getArray("quantities").getArray());
        for (int i = 0; i < productIds.length; i++) {
            orders.put(productIds[i], quantities[i]);
        }
        return new ShoppingCart(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getLong("payment_id"),
                orders
        );
    };

    @Override
    public void add(ShoppingCart shoppingCart) {
        int orderId = QueryProcessor.FetchOne(
                "INSERT INTO orders (user_id, payment_id) VALUES (?, ?) RETURNING id;",
                rs -> rs.getInt("id"),
                String.valueOf(shoppingCart.getUserId()),
                String.valueOf(shoppingCart.getPaymentId())
        );
        if (shoppingCart.getOrders().isEmpty()) return;

        StringBuilder sb = new StringBuilder("INSERT INTO product_orders (order_id, product_id, quantity) VALUES ");
        Map<Integer, Integer> orders = shoppingCart.getOrders();
        for (Integer productId : orders.keySet()) {
            sb.append("(")
                    .append(orderId).append(", ")
                    .append(productId).append(", ")
                    .append(orders.get(productId)) .append("), ");
        }
        sb.delete(-1, -3).append(";");
        QueryProcessor.ExecuteUpdate(sb.toString());
    }

    @Override
    public ShoppingCart find(int id) {

        return QueryProcessor.FetchOne(
                "SELECT o.id, user_id, payment_id, ARRAY_AGG(po.product_id), ARRAY_AGG(po.quantity)" +
                           "FROM orders AS o JOIN product_orders AS po ON o.id = po.order_id" +
                             "WHERE o.id = ?" +
                             "GROUP BY o.id, user_id, payment_id;", assembler, String.valueOf(id)
        );
    }

    @Override
    public void remove(int id) {
        QueryProcessor.ExecuteUpdate("DELETE FROM orders WHERE id = ?;", String.valueOf(id));
    }

    @Override
    public List<ShoppingCart> getAll(int userId) {

        return QueryProcessor.FetchAll(
                "SELECT o.id, user_id, payment_id, ARRAY_AGG(po.product_id), ARRAY_AGG(po.quantity)" +
                           "FROM orders AS o JOIN product_orders AS po ON o.id = po.order_id" +
                             "WHERE user_id = ?" +
                             "GROUP BY o.id, user_id, payment_id;", assembler, String.valueOf(userId)
        );
    }
}
