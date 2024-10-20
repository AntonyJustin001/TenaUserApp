package tena.health.care.screens.orders

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import tena.health.care.R
import tena.health.care.adapter.CartListAdapter
import tena.health.care.interfaces.ActivityActionListener
import tena.health.care.models.Order
import tena.health.care.screens.orders.adapter.OrdersListAdapter

class OrderListScreen : Fragment() {

    private lateinit var backBtnHolder: LinearLayout
    private lateinit var rcOrders: RecyclerView
    lateinit var db: FirebaseFirestore
    lateinit var orderRef: Query
    private var activityActionListener: ActivityActionListener? = null
    private lateinit var progressBar: LottieAnimationView
    private lateinit var tvEmptyOrder: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ActivityActionListener) {
            activityActionListener = context
        } else {
            throw RuntimeException("$context must implement ActivityActionListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        rcOrders = view.findViewById(R.id.rcOrders)
        rcOrders.layoutManager = LinearLayoutManager(context)

        backBtnHolder = view.findViewById(R.id.backBtnHolder)
        backBtnHolder.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        tvEmptyOrder = view.findViewById(R.id.tvEmptyOrders)
        activityActionListener?.showOrHideCart(false)
        progressBar = view.findViewById(R.id.progressBar)

        val userId = FirebaseAuth.getInstance().currentUser?.uid?:""

        orderRef = db.collection("order")
            .whereEqualTo("userId", userId)

        progressBar.visibility = View.VISIBLE
        orderRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                progressBar.visibility = View.GONE
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val orders = mutableListOf<Order?>()
                for (document in snapshots.documents) {
                    val order = document.toObject(Order::class.java)
                    orders.add(order)
                }
                // Now orders holds the filtered list of orders
                Log.e("Firestore", "Real-time orders for user: $orders")
                if (isAdded) {
                    if (orders.isNotEmpty()) {
                        rcOrders.visibility = View.VISIBLE
                        tvEmptyOrder.visibility = View.GONE
                        rcOrders.adapter = OrdersListAdapter(requireContext(), requireActivity(), orders)
                    } else {
                        rcOrders.visibility = View.GONE
                        tvEmptyOrder.visibility = View.VISIBLE
                    }
                }
                progressBar.visibility = View.GONE
            }
        }

//        orderRef = db.collection("order")
//            .whereEqualTo("userId", userId)
//        progressBar.visibility  = View.VISIBLE
//        orderRef.addSnapshotListener { snapshots, e ->
//            if (e != null) {
//                progressBar.visibility  = View.GONE
//                Log.w("Firestore", "Listen failed.", e)
//                return@addSnapshotListener
//            }
//
//            if (snapshots != null) {
//                val orders = mutableListOf<Order?>()
//                for (document in snapshots.documents) {
//                    val order = document.toObject(Order::class.java)
//                    orders.add(order)
//                }
//                // Now cartItems holds the updated cart items
//                Log.e("Firestore", "CartScreen - Real-time orders: $orders")
//                if(isAdded) {
//                    if(orders.size>0){
//                        rcOrders.visibility = View.VISIBLE
//                        tvEmptyOrder.visibility = View.GONE
//                        rcOrders.adapter = OrdersListAdapter(requireContext(),requireActivity(),orders)
//                    } else {
//                        rcOrders.visibility = View.GONE
//                        tvEmptyOrder.visibility = View.VISIBLE
//                    }
//                }
//                progressBar.visibility  = View.GONE
//            }
//        }

    }

}