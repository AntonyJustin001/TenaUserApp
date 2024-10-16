package tena.health.care.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import tena.health.care.R
import tena.health.care.models.CartItem
import tena.health.care.screens.cart.CartManager
import tena.health.care.utils.loadImageFromUrl

class CartItemAdapter(val context: Context, activity: FragmentActivity, private val items: List<CartItem?>) :
    RecyclerView.Adapter<CartItemAdapter.ItemViewHolder>() {

    var activity: FragmentActivity
    var db: FirebaseFirestore
    var cartManager: CartManager
    private var productQuantity  = 0

    init {
        this.activity = activity

        db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        cartManager = CartManager(db,userId?:"")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rc_item_cart, parent, false)
        return ItemViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position]?: CartItem())
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val tvProductTitle: TextView = itemView.findViewById(R.id.tvProductTitle)
        private val removeCart: LinearLayout = itemView.findViewById(R.id.removeCart)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val itemRemove: LinearLayout = itemView.findViewById(R.id.itemRemove)
        private val tvProductQuantity: TextView = itemView.findViewById(R.id.tvProductQuantity)
        private val itemAdd: LinearLayout = itemView.findViewById(R.id.itemAdd)

        fun bind(product: CartItem) {
            loadImageFromUrl(context,product.productImageUrl,ivProductImage)
            tvProductTitle.text = product.name
            tvProductPrice.text = "${product.price}"
            tvProductQuantity.text = "${product.quantity}"
            removeCart.setOnClickListener {
                Log.e("Test","RemoveCart Clicked")
                cartManager.removeFromCart(product.productId)
            }
            itemRemove.setOnClickListener {
                Log.e("Test","itemRemove Clicked")
                if(tvProductQuantity.text.toString().toInt() > 1) {
                    productQuantity = tvProductQuantity.text.toString().toInt() - 1
                    cartManager.updateCartItemQuantity(product.productId,productQuantity)
                }
            }
            itemAdd.setOnClickListener {
                Log.e("Test","itemAdd Clicked")
                productQuantity = tvProductQuantity.text.toString().toInt() + 1
                cartManager.updateCartItemQuantity(product.productId,productQuantity)
            }
        }
    }
}