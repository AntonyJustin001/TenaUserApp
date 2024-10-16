package tena.health.care.screens.cart

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import tena.health.care.models.CartItem

class CartManager(db: FirebaseFirestore, userId: String) {

    private val cartRef = db.collection("users").document(userId).collection("cart")

    fun addToCart(productId: String, name: String,productImageUrl:String, quantity: Int, price: Double) {
        val cartItem = CartItem(productId, name, productImageUrl, quantity, price)

        cartRef.document(productId).set(cartItem)
            .addOnSuccessListener {
                Log.e("Firestore", "Item added to cart successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding item to cart", e)
            }
    }

    fun getCartItems() {
        cartRef.get()
            .addOnSuccessListener { documents ->
                val cartItems = mutableListOf<CartItem>()
                for (document in documents) {
                    val cartItem = document.toObject(CartItem::class.java)
                    cartItems.add(cartItem)
                }
                // Now cartItems holds all the items in the cart for this user
                Log.e("Firestore", "Cart items: $cartItems")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting cart items", e)
            }
    }

    fun updateCartItemQuantity(productId: String, newQuantity: Int) {
        cartRef.document(productId).update("quantity", newQuantity)
            .addOnSuccessListener {
                Log.e("Firestore", "Item quantity updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating item quantity", e)
            }
    }

    fun removeFromCart(productId: String) {
        cartRef.document(productId).delete()
            .addOnSuccessListener {
                Log.e("Firestore", "Item removed from cart successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error removing item from cart", e)
            }
    }
}
