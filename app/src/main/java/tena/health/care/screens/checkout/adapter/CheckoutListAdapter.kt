package tena.health.care.screens.checkout.adapter

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
import tena.health.care.screens.checkout.CheckOutScreen

data class Total(val tax:Double = 10.00, val deliveryCharge:Double = 20.00, val subTotal:Double = 0.00,
                 val total:Double = 0.00)

class CheckoutListAdapter(context: Context, activity:FragmentActivity,checkOutScreen:CheckOutScreen, cartList: List<CartItem?>) :
    RecyclerView.Adapter<CheckoutListAdapter.ItemViewHolder>() {

    var context: Context
    var activity: FragmentActivity
    var checkOutScreen: CheckOutScreen
    var cartList = mutableListOf<CartItem?>()
    var totalPrice:Total = Total()

    init {
        this.context = context
        this.activity = activity
        this.cartList = cartList.toMutableList()
        this.checkOutScreen = checkOutScreen

        var subTotal = 0.0
        var Total = 0.0

        //Calculate Total
        this.cartList.forEach {
            subTotal = subTotal + (it?.quantity!! * it.price)
        }
        totalPrice = totalPrice.copy(subTotal = subTotal)
        Total = totalPrice.subTotal + totalPrice.tax +
                totalPrice.deliveryCharge
        totalPrice = totalPrice.copy(total = Total)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rc_item_checkout_list, parent, false)
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
        val selectedPayment: RadioGroup = itemView.findViewById(R.id.selectedPayment)
        val selectedAddress: RadioGroup = itemView.findViewById(R.id.selectedAddress)

        fun bind(cartList: List<CartItem?>, total: Total) {
            tvSubtotal.text = "${total.subTotal}"
            tvTax.text = "${total.tax}"
            tvDevliveryCharge.text = "${total.deliveryCharge}"
            tvTotal.text = "${total.total}"

            selectedPayment.setOnCheckedChangeListener { group, checkedId ->
                val radioButton: RadioButton = itemView.findViewById(checkedId)
                checkOutScreen.selectedPayment = radioButton.text.toString()
                //Toast.makeText(context, "Selected: ${}", Toast.LENGTH_SHORT).show()
            }

            selectedAddress.setOnCheckedChangeListener { group, checkedId ->
                val radioButton: RadioButton = itemView.findViewById(checkedId)
                checkOutScreen.selectedAddress = radioButton.text.toString()
                //Toast.makeText(context, "Selected: ${}", Toast.LENGTH_SHORT).show()
            }

            rcProductList.layoutManager = LinearLayoutManager(context)
            rcProductList.adapter = CheckoutProductsAdapter(context, activity,cartList.toList())
        }

    }

}