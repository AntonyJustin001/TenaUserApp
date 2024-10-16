package tena.health.care.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import tena.health.care.R
import tena.health.care.models.Product
import tena.health.care.screens.productDetailsScreen.ProductDetailsScreen
import tena.health.care.utils.loadImageFromUrl
import tena.health.care.utils.loadScreen

class PopularProductAdapter(val context: Context,val activity: FragmentActivity, private val items: List<Product>) :
    RecyclerView.Adapter<PopularProductAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rc_item_popular_products, parent, false)
        return ItemViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val tvProductTitle: TextView = itemView.findViewById(R.id.tvProductTitle)
        private val tvProductDescription: TextView = itemView.findViewById(R.id.tvProductDescription)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val layoutAdd: LinearLayout = itemView.findViewById(R.id.layoutAdd)
        private val productHolder: CardView = itemView.findViewById(R.id.productHolder)

        fun bind(product: Product) {
            tvProductTitle.text = product.productTitle
            tvProductDescription.text = product.productDescription
            tvProductPrice.text = "INR ${product.price}"
            loadImageFromUrl(context, product.imageUrl, imageView)
            layoutAdd.setOnClickListener {
                Log.e("Test","Layout Add Clicked")
            }
            productHolder.setOnClickListener {
                Log.e("Test","Layout Product Clicked")
                loadScreen(activity, ProductDetailsScreen(),"productId",product.id)
            }
        }
    }
}