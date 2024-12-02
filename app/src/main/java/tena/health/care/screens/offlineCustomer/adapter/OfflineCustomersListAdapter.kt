package tena.health.care.screens.offlineCustomer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import tena.health.care.R
import tena.health.care.models.OfflineCustomer

class OfflineCustomersListAdapter(val context: Context, val activity: FragmentActivity, val parentFragmentManager: FragmentManager, val fragment:Fragment,
                                  private val items: List<OfflineCustomer>) :
    RecyclerView.Adapter<OfflineCustomersListAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rc_item_offline_customer, parent, false)
        return ItemViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ItemViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvEmailId: TextView = itemView.findViewById(R.id.tvEmailId)
        private val tvMobile: TextView = itemView.findViewById(R.id.tvMobile)
        private val tvDOB: TextView = itemView.findViewById(R.id.tvDOB)
        private val tvSoldProduct: TextView = itemView.findViewById(R.id.tvSoldProduct)
        private val tvAddedDate: TextView = itemView.findViewById(R.id.tvAddedDate)
        private val customerHolder: LinearLayout = itemView.findViewById(R.id.customerHolder)

        fun bind(customer: OfflineCustomer) {

            tvName.text = customer.name
            tvEmailId.text = customer.emailId
            tvMobile.text = customer.mobileNo
            tvDOB.text = customer.dateOfBirth
            tvSoldProduct.text = customer.soldProduct
            tvAddedDate.text = customer.addedDate

            customerHolder.setOnClickListener {
                //Log.e("Test","Order Id ${order.orderId}")
                //loadScreen(activity, OfflineCustomerDetailsScreen(customer.userId))
            }

        }
    }
}