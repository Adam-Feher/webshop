package com.codecool.shop.controller;

import com.codecool.shop.MailSender;
import com.codecool.shop.dao.OrderDao;
import com.codecool.shop.dao.implementation.OrderDaoMem;
import com.codecool.shop.model.Address;
import com.codecool.shop.model.Order;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.time.LocalDateTime;


@WebServlet(urlPatterns = {"/webshop/checkout"})
public class OrderController extends HttpServlet {

    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        OrderDao shoppingCartDataStore = OrderDaoMem.getInstance();
        StringBuilder rawData = new StringBuilder();
        BufferedReader reader = req.getReader();
        String input;

        while ((input = reader.readLine()) != null) {
            rawData.append(input);
        }
        Order order = gson.fromJson(rawData.toString(), Order.class);
        shoppingCartDataStore.add(order);
        System.out.println(order);
        logOrder(rawData.toString(), order.getId());
        // sendMail(order);
    }

    /*private void sendMail(Order order) {
        String subject = "Information about order number " + order.getId();
        Address address = order.getUser().getShippingAddress();
        String content = "<h2>Dear " + order.getUser().getName() + "!</h2>" +
                "<p>Thank you for the purchase, we have received your order.</p>" +
                "<p>The items will arrive at the following address: </p>" +
                "" + address.getZipcode() + " " + address.getCountry() + ", " + address.getAddress() + "" +
                "<p>Payment identifier: " + order.getPaymentId() + "</p>" +
                "<br><p>We hope you have a nice day!<br>Team Codeberg</p>";
        MailSender mailSender = new MailSender(order.getUser().getEmail(), subject, content);
        mailSender.start();
    }*/

    private void logOrder(String order, int id) {
        LocalDateTime currentDT = LocalDateTime.now();
        String filename = "./orders/order" + id + "_" + currentDT.getYear() + currentDT.getMonthValue()
                + currentDT.getDayOfMonth() + ".json";
        try {
            PrintWriter writer = new PrintWriter(filename, "UTF-8");
            writer.println(order);
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}