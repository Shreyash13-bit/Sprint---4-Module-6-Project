package com.orderservice.sprint4.service;

import com.orderservice.sprint4.dto.OrderDetailsRequestDTO;
import com.orderservice.sprint4.dto.OrderDetailsResponseDTO;
import com.orderservice.sprint4.dto.OrderItemRequestDTO;
import com.orderservice.sprint4.dto.OrderItemResponseDTO;
import com.orderservice.sprint4.model.Order;
import com.orderservice.sprint4.model.OrderInvoice;
import com.orderservice.sprint4.model.OrderItem;
import com.orderservice.sprint4.repository.OrderInvoiceRepository;
import com.orderservice.sprint4.repository.OrderItemRepository;
import com.orderservice.sprint4.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService{
//    @Value("${user.service.user.validation.url}")
//    private String USER_SERVICE_USER_VALIDATION_URL;
//
//    @Value("${product.service.product.validation.url}")
//    private String PRODUCT_SERVICE_VALIDATION_URL;

    @Autowired
    private RestTemplate restTemplate;


    @Autowired
    private OrderInvoiceRepository orderInvoiceRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;


    @Override
    @Transactional
    public String createOrderTransaction(OrderDetailsRequestDTO dto) {
        try {

            validateUser(dto.getUserId());

            for (OrderItemRequestDTO itemDto : dto.getOrderItemRequestDTOS()) {
                validateProduct(itemDto.getProductId());
            }



            Order order = new Order();
            order.setUserId(dto.getUserId());
            order.setOrderDate(dto.getOrderDate());
            order.setOrderStatus(dto.getOrderStatus());
            order.setPromoDiscount(dto.getPromoDiscount());
            order.setOrderTotal(dto.getOrderTotal());

            Order savedOrder = orderRepository.save(order);
            System.out.println(savedOrder.toString());


            List<OrderItem> orderItems = new ArrayList<>();
            for (OrderItemRequestDTO itemDto : dto.getOrderItemRequestDTOS()) {
                OrderItem item = new OrderItem();
                item.setOrder(savedOrder);
                item.setProductId(itemDto.getProductId());
                item.setSku(itemDto.getSku());
                item.setQuantity(itemDto.getQuantity());
                item.setUnitPrice(itemDto.getUnitPrice());
                item.setDiscount(itemDto.getDiscount());
                item.setFinalPrice(itemDto.getFinalPrice());
                item.setSize(itemDto.getSize());
                item.setStatus(itemDto.getStatus());
                item.setSellerId(itemDto.getSellerId());
                orderItems.add(item);
            }
            List<OrderItem> savedOrderItems = orderItemRepository.saveAll(orderItems);

            savedOrderItems.stream().toString();




            OrderInvoice invoice = new OrderInvoice();
            invoice.setOrder(savedOrder);
            invoice.setInvoiceDate(LocalDateTime.now());
            invoice.setInvoiceAmount(savedOrder.getOrderTotal());
            invoice.setPaymentMode(dto.getPaymentMode());


            String invoiceNumber = generateInvoiceNumber(dto.getUserId(), dto.getPaymentMode());
            invoice.setInvoiceNumber(invoiceNumber);

            orderInvoiceRepository.save(invoice);

            return invoiceNumber;

        } catch (Exception e) {
            throw new RuntimeException("Transaction failed: " + e.getMessage(), e);
        }
    }


//    public OrderDetailsResponseDTO getOrderDetails(Integer orderId) {
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new RuntimeException("Order not found"));
//
//        OrderDetailsResponseDTO response = new OrderDetailsResponseDTO();
//        response.setOrderId(order.getOrderId());
//        response.setUserId(order.getUserId());
//        response.setOrderDate(order.getOrderDate());
//        response.setOrderStatus(order.getOrderStatus());
//        response.setPromoDiscount(order.getPromoDiscount());
//        response.setOrderTotal(order.getOrderTotal());
//
//        // Map invoice
//        if (order.getOrderInvoice() != null) {
//            response.setInvoiceNumber(order.getOrderInvoice().getInvoiceNumber());
//            response.setInvoiceDate(order.getOrderInvoice().getInvoiceDate());
//            response.setInvoiceAmount(order.getOrderInvoice().getInvoiceAmount());
//            response.setPaymentMode(order.getOrderInvoice().getPaymentMode());
//        }
//
//        // Map order items
//        List<OrderItemResponseDTO> items = order.getOrderItems().stream().map(item -> {
//            OrderItemResponseDTO dto = new OrderItemResponseDTO();
//            dto.setOrderItemId(item.getOrderItemId());
//            dto.setProductId(item.getProductId());
//            dto.setSku(item.getSku());
//            dto.setQuantity(item.getQuantity());
//            dto.setUnitPrice(item.getUnitPrice());
//            dto.setDiscount(item.getDiscount());
//            dto.setFinalPrice(item.getFinalPrice());
//            dto.setSize(item.getSize());
//            dto.setStatus(item.getStatus());
//            dto.setSellerId(item.getSellerId());
//            return dto;
//        }).toList();
//
//        response.setOrderItems(items);
//        return response;
//    }


    @Override
    public OrderDetailsResponseDTO getOrderDetails(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderDetailsResponseDTO response = new OrderDetailsResponseDTO();
        response.setOrderId(order.getOrderId());
        response.setUserId(order.getUserId());
        response.setOrderDate(order.getOrderDate());
        response.setOrderStatus(order.getOrderStatus());
        response.setPromoDiscount(order.getPromoDiscount());
        response.setOrderTotal(order.getOrderTotal());

        if (order.getOrderInvoice() != null) {
            response.setInvoiceNumber(order.getOrderInvoice().getInvoiceNumber());
            response.setInvoiceDate(order.getOrderInvoice().getInvoiceDate());
            response.setInvoiceAmount(order.getOrderInvoice().getInvoiceAmount());
            response.setPaymentMode(order.getOrderInvoice().getPaymentMode());
        }

        List<OrderItemResponseDTO> itemDTOs = order.getOrderItems().stream().map(item -> {
            OrderItemResponseDTO dto = new OrderItemResponseDTO();
            dto.setOrderItemId(item.getOrderItemId());
            dto.setProductId(item.getProductId());
            dto.setSku(item.getSku());
            dto.setQuantity(item.getQuantity());
            dto.setUnitPrice(item.getUnitPrice());
            dto.setDiscount(item.getDiscount());
            dto.setFinalPrice(item.getFinalPrice());
            dto.setSize(item.getSize());
            dto.setStatus(item.getStatus());
            dto.setSellerId(item.getSellerId());
            return dto;
        }).toList();

        response.setOrderItems(itemDTOs);
        return response;
    }


    private void validateUser(Integer userId) {
        return;
//        try {
//            String url = USER_SERVICE_USER_VALIDATION_URL + userId;
//            restTemplate.getForEntity(url, String.class); // If 404, it will throw an exception
//        } catch (HttpClientErrorException.NotFound ex) {
//            throw new RuntimeException("User with ID " + userId + " does not exist");
//        } catch (Exception ex) {
//            throw new RuntimeException("Failed to verify user: " + ex.getMessage(), ex);
//        }
    }

    private void validateProduct(Integer productId){
        return;
//        try{
//            String url = PRODUCT_SERVICE_VALIDATION_URL + productId;
//            restTemplate.getForEntity(url,String.class);
//        }catch( HttpClientErrorException.NotFound ex){
//            throw new RuntimeException("Product with ID " + productId + " does not exist");
//        }catch (Exception ex){
//            throw new RuntimeException("Failed to verify product: "+ex.getMessage(),ex);
//        }
    }


    private String generateInvoiceNumber(Integer userId, String paymentMode) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        return "INV-" + timestamp + "-" + userId + "-" + paymentMode.toUpperCase();
    }
}
