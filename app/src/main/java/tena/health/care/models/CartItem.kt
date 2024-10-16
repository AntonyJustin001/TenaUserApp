package tena.health.care.models

data class CartItem(
    val productId: String = "",
    val name: String = "",
    val productImageUrl: String = "",
    var quantity: Int = 0,
    var price: Double = 0.0
)
