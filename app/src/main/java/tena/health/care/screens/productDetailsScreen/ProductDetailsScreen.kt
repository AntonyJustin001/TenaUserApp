package tena.health.care.screens.productDetailsScreen

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import tena.health.care.R
import tena.health.care.dialog.CustomDialog
import tena.health.care.interfaces.ActivityActionListener
import tena.health.care.models.HomeSlider
import tena.health.care.models.Product
import tena.health.care.screens.cart.CartManager
import tena.health.care.utils.loadImageFromUrl

class ProductDetailsScreen : Fragment() {

    private lateinit var backBtnHolder: LinearLayout
    private lateinit var ivProductImage: ImageView
    private lateinit var tvProductTitle: TextView
    private lateinit var tvProductDescription: TextView
    private lateinit var tvProductPrice: TextView
    private lateinit var tvProductSize: TextView
    private lateinit var layoutAdd:LinearLayout
    private lateinit var layoutRemove:LinearLayout
    private lateinit var tvProductQuantity:TextView
    private lateinit var btnCart: LinearLayout
    lateinit var customDialog: CustomDialog

    private var productImageUrl  = ""
    private var productQuantity  = 1
    private var productPrice  = 0.0

    private val _homeItems = MutableLiveData<MutableSet<Pair<HomeSlider?, Product?>>>().apply { value = mutableSetOf() }
    val homeItems: LiveData<MutableSet<Pair<HomeSlider?, Product?>>> = _homeItems

    lateinit var db:FirebaseFirestore
    lateinit var cartManager:CartManager

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
        return inflater.inflate(R.layout.fragment_product_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productId = arguments?.getString("productId")
        db = FirebaseFirestore.getInstance()
        ivProductImage = view.findViewById(R.id.ivProductImage)
        tvProductTitle = view.findViewById(R.id.tvProductTitle)
        tvProductDescription = view.findViewById(R.id.tvProductDescription)
        tvProductPrice = view.findViewById(R.id.tvProductPrice)
        tvProductQuantity = view.findViewById(R.id.tvProductQuantity)
        tvProductSize = view.findViewById(R.id.tvProductSize)
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Assuming you're using FirebaseAuth
        cartManager = CartManager(db,userId?:"")
        activityActionListener?.showOrHideCart(false)
        backBtnHolder = view.findViewById(R.id.backBtnHolder)
        backBtnHolder.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        btnCart = view.findViewById(R.id.btnCart)
        btnCart.setOnClickListener {
            cartManager.addToCart(productId?:"", name = tvProductTitle.text.toString(),productImageUrl, tvProductQuantity.text.toString().toInt(),
                productPrice)
            //Show Dialog
            customDialog = CustomDialog(
                resources.getDrawable(R.drawable.ic_success),
                resources.getString(R.string.congratulations),
                "Your Product Successfully Added to Cart",
                false, 3000
            ) {
                activityActionListener?.showOrHideCart(true)
                requireActivity().supportFragmentManager.popBackStack()
            }
            customDialog.show(childFragmentManager, "CustomDialog")
        }

        tvProductQuantity.text = "${productQuantity}"
        layoutAdd = view.findViewById(R.id.layoutAdd)
        layoutAdd.setOnClickListener {
            productQuantity = productQuantity + 1
            tvProductQuantity.text = "${productQuantity}"
        }

        layoutRemove = view.findViewById(R.id.layoutRemove)
        layoutRemove.setOnClickListener {
            if(productQuantity>1) {
                productQuantity = productQuantity - 1
                tvProductQuantity.text = "${productQuantity}"
            }
        }

        // Reference to the specific product document
        val productRef = db.collection("product").document(productId?:"")
        productRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val productTitle = snapshot.getString("productTitle")
                val productDescription = snapshot.getString("productDescription")
                val price = snapshot.getDouble("price")
                val currency = snapshot.getString("currency")
                val productSize = snapshot.getString("productSize")
                productImageUrl = snapshot.getString("imageUrl")?:""
                loadImageFromUrl(requireContext(), productImageUrl?:"", ivProductImage)
                tvProductTitle.text = productTitle?:""
                tvProductDescription.text = productDescription?:""
                productPrice = price?:0.0
                tvProductPrice.text = "${currency}  $productPrice"
                tvProductSize.text = "${productSize?:""}"

                // Update your UI with the product data
                Log.e("Firestore", "productTitle: $productTitle, productDescription: $productDescription" +
                        " price: $price imageUrl: $productImageUrl")
            } else {
                Log.e("Firestore", "Product data is null")
            }
        }

    }

}