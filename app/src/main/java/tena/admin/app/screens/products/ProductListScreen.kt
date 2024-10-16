package tena.admin.app.screens.products

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import tena.admin.app.R
import tena.admin.app.ProductsListAdapter
import tena.admin.app.models.Product
import tena.admin.app.utils.loadScreen

class ProductListScreen : Fragment(),ProductDeleteBottomSheet.OnButtonClickListener {

    private lateinit var rcproducts: RecyclerView
    private lateinit var tvEmptyproductList: TextView
    private lateinit var etSearch: EditText
    private lateinit var fragment: Fragment
    private lateinit var ivBack: ImageView
    private lateinit var ivAddproduct: ImageView


    private lateinit var progressBar: LottieAnimationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        etSearch = view.findViewById(R.id.etSearch)
        rcproducts = view.findViewById(R.id.rvproduct)
        tvEmptyproductList = view.findViewById(R.id.tvEmptyproductList)
        fragment = this

        ivBack = view.findViewById(R.id.ivBack)
        ivBack.setOnClickListener {
            // Alternatively, using parentFragmentManager
            parentFragmentManager.popBackStack()
        }

        ivAddproduct = view.findViewById(R.id.ivAddproduct)
        ivAddproduct.setOnClickListener {
            loadScreen(requireActivity(), ProductDetailAddEdit(""),"Type","Add")
        }

        loadproductList()


        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This method is called before the text is changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // This method is called when the text is being changed
            }

            override fun afterTextChanged(s: Editable?) {
                // This method is called after the text has been changed
                s?.let {
                    val words = it.split(" ")
                    val lastWord = if (words.isNotEmpty()) words.last() else ""
                    Log.e("Test","lastWord - $lastWord")
                    searchproducts(lastWord) { searchedproducts ->
                        if(searchedproducts.size>0) {
                            tvEmptyproductList.visibility = View.GONE
                            rcproducts.visibility = View.VISIBLE
                            rcproducts.layoutManager = LinearLayoutManager(context)
                            rcproducts.adapter = ProductsListAdapter(requireContext(),requireActivity(),parentFragmentManager, fragment, searchedproducts)
                        } else {
                            tvEmptyproductList.visibility = View.VISIBLE
                            rcproducts.visibility = View.GONE
                        }
                    }
//                    if(lastWord!="") {
//                    }
                }
            }
        })

    }

    override fun onResume() {
        super.onResume()
        Log.e("Test","onResume Called")
    }

    fun getAllproducts(onProductsRetrieved: (List<Product>) -> Unit) {
        progressBar.visibility = View.VISIBLE
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
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error getting products: $e")
                progressBar.visibility = View.GONE
            }
    }

    fun searchproducts(searchWord: String, onResult: (List<Product>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val productsRef = db.collection("product")

        // Simple search based on exact match
        productsRef.whereGreaterThanOrEqualTo("productTitle", searchWord)
            .whereLessThanOrEqualTo("productTitle", searchWord + '\uf8ff')
            .get()
            .addOnSuccessListener { documents ->
                val products = mutableListOf<Product>()
                for (document in documents) {
                    val product = document.toObject(Product::class.java)
                    products.add(product)
                }
                onResult(products)
            }
            .addOnFailureListener { exception ->
                // Handle the error
                Log.e("Firestore", "Error getting documents: ", exception)
                Snackbar.make(requireView(), "Something went wrong try again", Snackbar.LENGTH_LONG).show()
                onResult(emptyList())
            }
    }

    fun deleteproduct(productId:String) {
        progressBar.visibility = View.VISIBLE
        val collectionName = "product"

        val db = FirebaseFirestore.getInstance()
        db.collection(collectionName)
            .whereEqualTo("id", productId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // For each matching document, delete it
                    db.collection(collectionName)
                        .document(productId)
                        .delete()
                        .addOnSuccessListener {
                            progressBar.visibility = View.GONE
                            Snackbar.make(requireView(), "product successfully deleted!", Snackbar.LENGTH_LONG).show()
                            loadproductList()
                        }
                        .addOnFailureListener { e ->
                            progressBar.visibility = View.GONE
                            Snackbar.make(requireView(), "Something went wrong try again", Snackbar.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = View.GONE
                Snackbar.make(requireView(), "Something went wrong try again", Snackbar.LENGTH_LONG).show()
            }
    }

    fun loadproductList() {
        Log.e("Test","LoadproductList Called()")
        getAllproducts { products ->
            products.forEach {
                Log.e("products","product - $it")
            }

            if(products.size>0) {
                tvEmptyproductList.visibility = View.GONE
                rcproducts.visibility = View.VISIBLE
                rcproducts.layoutManager = LinearLayoutManager(context)
                rcproducts.adapter = ProductsListAdapter(requireContext(),requireActivity(),parentFragmentManager,fragment, products)
            } else {
                tvEmptyproductList.visibility = View.VISIBLE
                rcproducts.visibility = View.GONE
            }

        }
    }

    override fun onButtonClicked(product : Product) {
        deleteproduct(product.id)
    }

}