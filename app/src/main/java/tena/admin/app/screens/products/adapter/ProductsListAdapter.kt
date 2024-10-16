package tena.admin.app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import tena.admin.app.models.Product
import tena.admin.app.screens.products.ProductDeleteBottomSheet
import tena.admin.app.screens.products.ProductDetailAddEdit
import tena.admin.app.screens.products.ProductDetailsScreen
import tena.admin.app.utils.loadImageFromUrl
import tena.admin.app.utils.loadScreen

class ProductsListAdapter(val context: Context, val activity: FragmentActivity, val parentFragmentManager: FragmentManager, val fragment:Fragment,
                          private val items: List<Product>) :
    RecyclerView.Adapter<ProductsListAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rc_item_product, parent, false)
        return ItemViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val productProfile: ImageView = itemView.findViewById(R.id.ivproductPic)
        private val productTitle: TextView = itemView.findViewById(R.id.tvproductName)
        private val productDescription: TextView = itemView.findViewById(R.id.tvproductSection)
        private val productDelete: ImageView = itemView.findViewById(R.id.ivDeleteproduct)
        private val productEdit: ImageView = itemView.findViewById(R.id.ivEditproduct)
        private val productHolder: MaterialCardView = itemView.findViewById(R.id.productHolder)

        fun bind(product: Product) {
            loadImageFromUrl(context,product.imageUrl,productProfile)
            productTitle.text = product.productTitle
            productDescription.text = product.productDescription
            productDelete.setOnClickListener {
                deleteJobDialog(product)
            }
            productEdit.setOnClickListener {
                loadScreen(activity, ProductDetailAddEdit(product.id),"Type","Edit")
            }
            productHolder.setOnClickListener {
                loadScreen(activity, ProductDetailsScreen(product.id))
            }
        }

        fun deleteJobDialog(product: Product) {
            val bottomSheetFragment = ProductDeleteBottomSheet(product)
            bottomSheetFragment.setTargetFragment(fragment,0)
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }
    }
}