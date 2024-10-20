package tena.health.care.screens.cart

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import tena.health.care.R
import tena.health.care.adapter.CartListAdapter
import tena.health.care.interfaces.ActivityActionListener
import tena.health.care.models.CartItem
import tena.health.care.screens.checkout.CheckOutScreen
import tena.health.care.utils.loadScreen

class CartScreen : Fragment() {

    private lateinit var backBtnHolder: LinearLayout
    private lateinit var btnPlaceOrder: LinearLayout
    private lateinit var rcCartList: RecyclerView
    private lateinit var tvEmptyCart: TextView
    lateinit var db: FirebaseFirestore
    lateinit var cartRef: CollectionReference
    private var activityActionListener: ActivityActionListener? = null

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
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        rcCartList = view.findViewById(R.id.rcCart)
        rcCartList.layoutManager = LinearLayoutManager(context)

        backBtnHolder = view.findViewById(R.id.backBtnHolder)
        backBtnHolder.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        btnPlaceOrder = view.findViewById(R.id.btnPlaceOrder)
        btnPlaceOrder.setOnClickListener {
            loadScreen(requireActivity(), CheckOutScreen())
        }

        tvEmptyCart = view.findViewById(R.id.tvEmptyCart)
        activityActionListener?.showOrHideCart(false)

        val userId = FirebaseAuth.getInstance().currentUser?.uid // Assuming you're using FirebaseAuth
        cartRef = db.collection("users").document(userId?:"").collection("cart")

        cartRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val cartItems = mutableListOf<CartItem?>()
                for (document in snapshots.documents) {
                    val cartItem = document.toObject(CartItem::class.java)
                    cartItems.add(cartItem)
                }
                // Now cartItems holds the updated cart items
                Log.e("Firestore", "CartScreen - Real-time cart items: $cartItems")
                if(isAdded) {
                    if(cartItems.size>0){
                        rcCartList.visibility = View.VISIBLE
                        btnPlaceOrder.visibility = View.VISIBLE
                        tvEmptyCart.visibility = View.GONE
                        rcCartList.adapter = CartListAdapter(requireContext(),requireActivity(),cartItems.toList())
                    } else {
                        rcCartList.visibility = View.GONE
                        btnPlaceOrder.visibility = View.GONE
                        tvEmptyCart.visibility = View.VISIBLE
                    }
                }
            }
        }

    }

}