package tena.admin.app.screens.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import tena.admin.app.R
import tena.admin.app.models.Order
import tena.admin.app.models.Product
import tena.admin.app.models.User
import tena.admin.app.screens.customers.CustomersListScreen
import tena.admin.app.screens.notification.NotificationCreate
import tena.admin.app.screens.orders.OrdersListScreen
import tena.admin.app.screens.products.ProductListScreen
import tena.admin.app.screens.teachers.TeacherListScreen
import tena.admin.app.screens.videos.ShowAllRecordedVideos
import tena.admin.app.utils.loadScreen

class HomeScreen : Fragment() {

    private lateinit var cvproducts: MaterialCardView
    private lateinit var cvOrders: MaterialCardView
    private lateinit var cvCustomers: MaterialCardView
    private lateinit var cvTeachers: MaterialCardView

    private lateinit var tvProductsCount: TextView
    private lateinit var tvCustomersCount: TextView
    private lateinit var tvOrderCount: TextView
    private lateinit var tvNotificationCount: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cvproducts = view.findViewById(R.id.cvproducts)
        cvOrders = view.findViewById(R.id.cvOrders)
        cvCustomers = view.findViewById(R.id.cvCustomers)
        tvProductsCount = view.findViewById(R.id.tvProductsCount)
        tvOrderCount = view.findViewById(R.id.tvOrderCount)
        tvCustomersCount = view.findViewById(R.id.tvCustomersCount)
        tvNotificationCount = view.findViewById(R.id.tvTeachersCount)
        cvTeachers =  view.findViewById(R.id.cvTeachers)

        cvproducts.setOnClickListener {
            loadScreen(requireActivity(), ProductListScreen())
        }
        cvOrders.setOnClickListener {
            loadScreen(requireActivity(), OrdersListScreen())
        }
        cvCustomers.setOnClickListener {
            loadScreen(requireActivity(),CustomersListScreen())
        }
        cvTeachers.setOnClickListener {
            loadScreen(requireActivity(), NotificationCreate())
        }

        getAllProducts { products ->
            products.forEach {
                Log.e("Products","Product - $it")
            }
            tvProductsCount.text = "${products.size}"
        }

        getAllOrders { orders ->
            orders.forEach {
                Log.e("orders","order - $it")
            }
            tvOrderCount.text = "${orders.size}"
        }

        getAllCustomers { customers ->
            customers.forEach {
                Log.e("customers","customer - $it")
            }
            tvCustomersCount.text = "${customers.size}"
        }

    }

    fun getAllProducts(onProductsRetrieved: (List<Product>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("product")
            .get()
            .addOnSuccessListener { result ->
                val productList = mutableListOf<Product>()
                for (document in result) {
                    val product = document.toObject(Product::class.java)
                    productList.add(product)
                }
                onProductsRetrieved(productList)
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error getting products: $e")
            }
    }

    fun getAllOrders(onProductsRetrieved: (List<Order>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("order")
            .get()
            .addOnSuccessListener { result ->
                val orders = mutableListOf<Order>()
                for (document in result) {
                    val order = document.toObject(Order::class.java)
                    orders.add(order)
                }
                onProductsRetrieved(orders)
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error getting orders: $e")
            }
    }

    fun getAllCustomers(onProductsRetrieved: (List<User>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val users = mutableListOf<User>()
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    users.add(user)
                }
                onProductsRetrieved(users)
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error getting users: $e")
            }
    }

}