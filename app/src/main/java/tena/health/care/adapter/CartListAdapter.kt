package tena.health.care.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tena.health.care.R
import tena.health.care.models.CartItem
import tena.health.care.models.Total

class CartListAdapter(context: Context, activity:FragmentActivity, cartList: List<CartItem?>) :
    RecyclerView.Adapter<CartListAdapter.ItemViewHolder>() {

    var context: Context
    var activity: FragmentActivity
    var cartList = mutableListOf<CartItem?>()
    var totalPrice: Total = Total()

    init {
        this.context = context
        this.activity = activity
        this.cartList = cartList.toMutableList()

        var subTotal = 0.0
        var Total = 0.0

        //Calculate Total
        this.cartList.forEach {
            subTotal = subTotal + (it?.quantity!! * it.price)
        }
        totalPrice = totalPrice.copy(subTotal = subTotal)
        //Total = totalPrice.subTotal + totalPrice.tax + totalPrice.deliveryCharge
        Total = totalPrice.subTotal
        totalPrice = totalPrice.copy(total = Total)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rc_item_cart_list, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(cartList, totalPrice)
    }

    override fun getItemCount(): Int = 1

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rcProductList: RecyclerView = itemView.findViewById(R.id.rcProductList)
        val tvSubtotal: TextView = itemView.findViewById(R.id.tvSubtotal)
        val tvDevliveryCharge: TextView = itemView.findViewById(R.id.tvDevliveryCharge)
        val tvTax: TextView = itemView.findViewById(R.id.tvTax)
        val tvTotal: TextView = itemView.findViewById(R.id.tvTotal)

        fun bind(cartList: List<CartItem?>, total: Total) {
            tvSubtotal.text = "${total.subTotal}"
            tvTax.text = "${total.tax}"
            tvDevliveryCharge.text = "${total.deliveryCharge}"
            tvTotal.text = "${total.total}"

            rcProductList.layoutManager = LinearLayoutManager(context)
            rcProductList.adapter = CartItemAdapter(context, activity,cartList.toList())
        }

    }

}