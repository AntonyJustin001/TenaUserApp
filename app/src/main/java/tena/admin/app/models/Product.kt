package tena.admin.app.models

data class Product(
    val id: String = "",
    val productTitle: String = "",
    val productDescription: String = "",
    val price: Int = 0,
    val imageUrl: String = "",
    val productStock: Int = 0
)