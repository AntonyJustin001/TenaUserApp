package tena.admin.app.models

import tena.health.care.models.CartItem

data class Order(
    val orderId: String = "",
    val customerName: String = "",
    val customerMobile: String = "",
    val cardItems: List<CartItem?> = emptyList(),
    val selectedPayment: String = "",
    val orderPlacedDate: String = "",
    val subTotal: String = "",
    val deliveryCharge: String = "",
    val tax: String = "",
    val total: String = "",
    val shippingAddress: String = "",
)