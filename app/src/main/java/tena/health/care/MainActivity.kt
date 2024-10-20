package tena.health.care

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.onesignal.OneSignal
import tena.health.care.data.preference.SharedPreferencesHelper
import tena.health.care.interfaces.ActivityActionListener
import tena.health.care.models.CartItem
import tena.health.care.screens.cart.CartScreen
import tena.health.care.screens.splash.SplashScreen
import tena.health.care.utils.Cart_Screen
import tena.health.care.utils.Splash_Screen
import tena.health.care.utils.prefs

class MainActivity : AppCompatActivity(), ActivityActionListener {

    lateinit var db: FirebaseFirestore
    lateinit var cartRef: CollectionReference
    private lateinit var btnTop: RelativeLayout
    private lateinit var btnCart: RelativeLayout
    private lateinit var tvCartQty: TextView
    var currentCartVisibility = false
    val cartItems = mutableListOf<CartItem?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = SharedPreferencesHelper(this)
        db = FirebaseFirestore.getInstance()

        btnTop = findViewById(R.id.btnTop)
        btnCart = findViewById(R.id.btnCart)
        tvCartQty = findViewById(R.id.tvCartQty)

        btnTop.setOnClickListener {
            //homeScrollView.scrollTo(0,0)
        }

        btnCart.setOnClickListener {
            loadFragment(CartScreen(), Cart_Screen)
        }


        if (savedInstanceState == null) {
            loadFragment(SplashScreen(), Splash_Screen)
            //loadFragment(TestScreen())
        }

//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val token = task.result
//                // Store the token in Firestore for later use
//                val userId = FirebaseAuth.getInstance().currentUser?.uid
//                if(userId!=null) {
//                    val db = FirebaseFirestore.getInstance()
//                    db.collection("users").document(userId).update("fcmToken", token)
//                }
//            }
//        }

        OneSignal.getDeviceState()?.userId?.let { playerId ->
            Log.d("PlayerID", "OneSignal Player ID: $playerId")
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if(userId!=null) {
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(userId).update("fcmToken", playerId)
            }
        }

        loadCart()

    }

    override fun loadCart() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.e("Test","MainActivity - loadCart - userId - $userId")
        if(userId != null) {
            cartRef = db.collection("users").document(userId?:"").collection("cart")

            cartRef.addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e)
                    return@addSnapshotListener
                }

                cartItems.clear()
                if (snapshots != null) {
                    for (document in snapshots.documents) {
                        val cartItem = document.toObject(CartItem::class.java)
                        val findCartItem = cartItems.find { it?.productId == cartItem?.productId }
                        if (findCartItem != null) {
                            findCartItem.quantity += 1
                        } else {
                            cartItems.add(cartItem)
                        }
                    }
                    tvCartQty.text = "${cartItems.size}"
                    // Now cartItems holds the updated cart items
                    Log.e("Test", "Real-time cart items: $cartItems")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("Test","onResume() - MainActivity")
    }

    private fun loadFragment(fragment: Fragment,tag:String) {
        if(tag == Splash_Screen) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.commit()
        } else {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed() // If no fragments are in the back stack, exit the activity
        }
    }

    override fun showOrHideCart(status: Boolean) {
        currentCartVisibility = status
        if(currentCartVisibility){
            if(cartItems.size>0) {
                btnCart.visibility = View.VISIBLE
            } else {
                btnCart.visibility = View.GONE
            }
        } else {
            btnCart.visibility = View.GONE
        }
    }
}
