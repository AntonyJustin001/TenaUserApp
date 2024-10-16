package tena.health.care.models

data class Order(
    val orderId: String = "",
    val cardItems: List<CartItem?> = emptyList(),
    val selectedPayment: String = "",
    val orderPlacedDate: String = "",
    val subTotal: String = "",
    val deliveryCharge: String = "",
    val tax: String = "",
    val total: String = "",
    val shippingAddress: String = "",
)