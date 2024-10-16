package tena.health.care.screens.orders.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tena.health.care.R
import tena.health.care.models.CartItem
import tena.health.care.models.Order
import tena.health.care.screens.checkout.CheckOutScreen

data class Total(val tax:Double = 10.00, val deliveryCharge:Double = 20.00, val subTotal:Double = 0.00,
                 val total:Double = 0.00)

class OrderDetailsAdapter(context: Context, activity:FragmentActivity /*,checkOutScreen:CheckOutScreen*/ , order: Order) :
    RecyclerView.Adapter<OrderDetailsAdapter.ItemViewHolder>() {

    var context: Context
    var activity: FragmentActivity
    var order: Order

    init {

        this.context = context
        this.activity = activity
        this.order = order

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rc_order_details, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(order = order)
    }

    override fun getItemCount(): Int = 1

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rcOrderedProductList: RecyclerView = itemView.findViewById(R.id.rcOrderedProductList)
        val tvSubtotal: TextView = itemView.findViewById(R.id.tvSubtotal)
        val tvDevliveryCharge: TextView = itemView.findViewById(R.id.tvDevliveryCharge)
        val tvTax: TextView = itemView.findViewById(R.id.tvTax)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotal)
        val tvSelectedPayment: TextView = itemView.findViewById(R.id.tvSelectedPayment)

        fun bind(order: Order) {
            tvSubtotal.text = "${order.subTotal}"
            tvTax.text = "${order.tax}"
            tvDevliveryCharge.text = "${order.deliveryCharge}"
            tvTotal.text = "${order.total}"
            tvSelectedPayment.text = "${order.selectedPayment}"
            rcOrderedProductList.layoutManager = LinearLayoutManager(context)
            rcOrderedProductList.adapter = OrderProductsAdapter(context, activity,order.cardItems)
        }

    }

}