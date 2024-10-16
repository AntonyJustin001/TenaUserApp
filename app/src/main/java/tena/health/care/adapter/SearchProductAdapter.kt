package tena.health.care.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import tena.health.care.R
import tena.health.care.models.Product
import tena.health.care.screens.productDetailsScreen.ProductDetailsScreen
import tena.health.care.utils.loadScreen

class SearchProductAdapter(val context: Context, activity: FragmentActivity, private val items: List<Product>) :
    RecyclerView.Adapter<SearchProductAdapter.ItemViewHolder>() {

    var activity: FragmentActivity

    init {
        this.activity = activity
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rc_item_searched_products, parent, false)
        return ItemViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val tvProductTitle: TextView = itemView.findViewById(R.id.tvProductTitle)
        private val layoutSearch: LinearLayout = itemView.findViewById(R.id.layoutSearch)

        fun bind(product: Product) {
            tvProductTitle.text = product.productTitle
            layoutSearch.setOnClickListener {
                Log.e("Test","Selected Product - ${product}")
                loadScreen(activity, ProductDetailsScreen(),"productId",product.id)
            }
        }
    }
}