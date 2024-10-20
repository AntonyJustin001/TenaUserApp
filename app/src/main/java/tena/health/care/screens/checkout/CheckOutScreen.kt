package tena.health.care.screens.checkout

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.razorpay.ExternalWalletListener
import com.razorpay.PaymentResultWithDataListener
import tena.health.care.R
import tena.health.care.dialog.CustomDialog
import tena.health.care.interfaces.ActivityActionListener
import tena.health.care.models.CartItem
import tena.health.care.models.Order
import tena.health.care.screens.cart.CartManager
import tena.health.care.screens.checkout.adapter.CheckoutListAdapter
import tena.health.care.screens.checkout.adapter.Total
import tena.health.care.screens.home.HomeScreen
import tena.health.care.utils.loadScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.razorpay.*
import org.json.JSONObject
import tena.health.care.models.User
import tena.health.care.utils.USER_DETAILS
import tena.health.care.utils.loadImageFromUrl
import tena.health.care.utils.prefs

class CheckOutScreen : Fragment(), PaymentResultWithDataListener, ExternalWalletListener,
    DialogInterface.OnClickListener {

    private lateinit var backBtnHolder: LinearLayout
    private lateinit var btnPlaceOrder: LinearLayout
    private lateinit var rcCheckoutList: RecyclerView
    lateinit var db: FirebaseFirestore
    lateinit var cartRef: CollectionReference
    lateinit var orderRef: CollectionReference
    var selectedPayment = ""
    var selectedAddress = ""
    val cartItems = mutableListOf<CartItem?>()
    private lateinit var progressBar: LottieAnimationView
    lateinit var customDialog: CustomDialog
    var latestOrderId = ""
    var userId = ""
    var user:User? = null
    lateinit var cartManager: CartManager
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
        return inflater.inflate(R.layout.fragment_checkout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = FirebaseAuth.getInstance().currentUser?.uid?:""
        Checkout.preload(requireContext())
        db = FirebaseFirestore.getInstance()
        rcCheckoutList = view.findViewById(R.id.rcCheckOut)
        rcCheckoutList.layoutManager = LinearLayoutManager(context)
        backBtnHolder = view.findViewById(R.id.backBtnHolder)
        backBtnHolder.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        progressBar = view.findViewById(R.id.progressBar)
        btnPlaceOrder = view.findViewById(R.id.btnPlaceOrder)
        btnPlaceOrder.setOnClickListener {
            if(selectedAddress!= "") {
                if(selectedPayment!= "") {
                    if(selectedPayment == "Razor Pay") {
                        startPayment()
                    } else {
                        placeOrder(
                            Order(
                                orderId = if(latestOrderId != "") (latestOrderId.toInt()+1).toString() else "0",
                                cardItems = cartItems.toList(),
                                selectedPayment = selectedPayment,
                                orderPlacedDate = getCurrentTime(),
                                total = TotalCalculation().subTotal.toString(),
                                subTotal = TotalCalculation().subTotal.toString(),
                                shippingAddress = selectedAddress,
                                userId = userId,
                                customerName = user?.name?:"",
                                customerMobile = user?.mobileNo?:"",
                                orderStatus = "Pending"
                            )
                        )
                    }
                } else {
                    Snackbar.make(requireView(), "Please Select Payment", Snackbar.LENGTH_LONG).show()
                }
            } else {
                Snackbar.make(requireView(), "Please Select Address", Snackbar.LENGTH_LONG).show()
            }
        }

        activityActionListener?.showOrHideCart(false)


        cartManager = CartManager(db, userId ?: "")
        cartRef = db.collection("users").document(userId ?: "").collection("cart")
        cartRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshots != null) {
                for (document in snapshots.documents) {
                    val cartItem = document.toObject(CartItem::class.java)
                    cartItems.add(cartItem)
                }
                // Now cartItems holds the updated cart items
                Log.e("Firestore", "CartScreen - Real-time cart items: $cartItems")
                if (isAdded) {
                    if (cartItems.size > 0) {
                        rcCheckoutList.adapter = CheckoutListAdapter(
                            requireContext(),
                            requireActivity(),
                            this,
                            cartItems.toList()
                        )
                    } else {
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                }
            }
        }

        orderRef = FirebaseFirestore.getInstance().collection("order")
        getLatestOrderId()


        val userRef = db.collection("users")
            .document(userId)
        userRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                user = snapshot.toObject(User::class.java)
            } else {
                Log.e("Firestore", "Product data is null")
            }
        }

    }

    fun placeOrder(
        order: Order
    ) {
        progressBar.visibility = View.VISIBLE
        orderRef.document(order.orderId).set(order)
            .addOnSuccessListener {
                progressBar.visibility = View.VISIBLE
                customDialog = CustomDialog(
                    resources.getDrawable(R.drawable.setting_icon),
                    resources.getString(R.string.congratulations),
                    "Your Order ${order.orderId} placed Sucessfully . It will take a Few Seconds Please Wait.",
                    true, 5000
                ) {
                    progressBar.visibility = View.GONE
                    order.cardItems.forEach {
                        cartManager.removeFromCart(it?.productId ?: "")
                    }
                    loadScreen(requireActivity(), HomeScreen())
                }
                customDialog.show(childFragmentManager, "CustomDialog")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding item to cart", e)
                progressBar.visibility = View.GONE
                customDialog = CustomDialog(
                    resources.getDrawable(R.drawable.ic_wrong),
                    resources.getString(R.string.error),
                    e.message.toString()
                )
                customDialog.show(childFragmentManager, "CustomDialog")
            }
    }

    private fun startPayment() {
        val co = Checkout()
        co.setKeyID("rzp_live_cNu44G1fcy0MXf")
        try {
            var options = JSONObject()
            options.put("name", "Tena Health Care")
            options.put("description", "Demoing Charges")
            //You can omit the image option to fetch the image from dashboard
            options.put("image", "https://firebasestorage.googleapis.com/v0/b/tenahealthcare-1e82e.appspot.com/o/AppLogo.jpg?alt=media&token=78b7fdb2-7e8c-42e8-84a0-f73e6e2c18e1")
            options.put("currency", "INR")
            options.put("amount", "1")
            options.put("send_sms_hash", true);

            val userDetailsRaw = Gson().fromJson(prefs.get(USER_DETAILS, ""), User::class.java)

            val prefill = JSONObject()
            prefill.put("email", if(userDetailsRaw.emailId=="") "test@gmail.com" else userDetailsRaw.emailId)
            prefill.put("contact", if(userDetailsRaw.mobileNo=="") "9876543210" else userDetailsRaw.mobileNo)

            options.put("prefill", prefill)

            co.open(activity, options)
        } catch (e: Exception) {
            Toast.makeText(activity, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    fun getCurrentTime(): String {
        val sdf = SimpleDateFormat(
            "dd-MM-yyyy HH:mm:ss a",
            Locale.getDefault()
        )
        return sdf.format(Date())
    }

    fun getLatestOrderId() {
        orderRef
            .orderBy(
                "orderId",
                Query.Direction.DESCENDING
            ) // Assuming orderId is your field for Order ID
            .limit(1) // Get the latest order
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val latestDoc = documents.documents[0]
                    latestOrderId = latestDoc.getString("orderId") ?: "0"
                } else {
                    println("No orders found")
                    latestOrderId = "0"
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting orders: ${exception.message}")
            }
    }

    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
        try {
            placeOrder(
                Order(
                    orderId = if(latestOrderId != "") (latestOrderId.toInt()+1).toString() else "0",
                    cardItems = cartItems.toList(),
                    selectedPayment = selectedPayment,
                    orderPlacedDate = getCurrentTime(),
                    total = TotalCalculation().subTotal.toString(),
                    subTotal = TotalCalculation().subTotal.toString(),
                    shippingAddress = selectedAddress,
                    userId = userId,
                    customerName = user?.name?:"",
                    customerMobile = user?.mobileNo?:"",
                    orderStatus = "Pending"
                )
            )
            Log.e("Test","Payment Successful : Payment ID: $p0\nPayment Data: ${p1?.data}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        try {
            Snackbar.make(requireView(), "\"Payment Failed : Payment Data: ${p2?.data}, Please Try Again Later!", Snackbar.LENGTH_LONG).show()
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onExternalWalletSelected(p0: String?, p1: PaymentData?) {
        try{
            Log.e("Test","External wallet was selected : Payment Data: ${p1?.data}")
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onClick(p0: DialogInterface?, p1: Int) {
        TODO("Not yet implemented")
    }

    fun TotalCalculation():Total {
        var totalPrice: Total = Total()

        var subTotal = 0.0
        var Total = 0.0

        //Calculate Total
        cartItems.forEach {
            subTotal = subTotal + (it?.quantity!! * it.price)
        }
        totalPrice = totalPrice.copy(subTotal = subTotal)
        Total = totalPrice.subTotal + totalPrice.tax +
                totalPrice.deliveryCharge
        totalPrice = totalPrice.copy(total = Total)
        return totalPrice
    }
}