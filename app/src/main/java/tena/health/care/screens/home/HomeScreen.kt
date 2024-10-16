package tena.health.care.screens.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import tena.health.care.R
import tena.health.care.adapter.HomeAdapter
import tena.health.care.interfaces.ActivityActionListener
import tena.health.care.models.HomeSlider
import tena.health.care.models.Product
import tena.health.care.utils.isNetworkAvailable

class HomeScreen : Fragment() {

    private lateinit var homeScrollView: ScrollView
    private lateinit var rcHome: RecyclerView
    private lateinit var noNetworkContainer: RelativeLayout

    var productsList = mutableSetOf<Product>()
    var homeSliderList = mutableSetOf<HomeSlider>()

    private var activityActionListener: ActivityActionListener? = null

    //Products List
    val productListener = listenToProducts { products ->
        homeItems.value?.clear()
        Log.e("Test"," Product Listener - Products: ${products}")
        productsList = products.toMutableSet()
        fetchHomeItems()
//        for (product in products) {
//            println(" Product Listener - Product: ${product.productTitle}, Price: ${product.price}")
//            addProductItem(product)
//        }
    }

    //HomeSliders List
    val homeSliderListener = listenToHomeSliders { homeSliders ->
        homeItems.value?.clear()
        Log.e("Test"," HomeSlider Listener - homeSliders: ${homeSliders}")
        homeSliderList = homeSliders.toMutableSet()
        fetchHomeItems()
//        for (slider in homeSliders) {
//            println(" HomeSlider Listener - Id: ${slider.id}, Price: ${slider.imageUrl}")
//            addSliderItem(slider)
//        }
    }

    private val _homeItems = MutableLiveData<MutableSet<Pair<HomeSlider?, Product?>>>().apply { value = mutableSetOf() }
    val homeItems: LiveData<MutableSet<Pair<HomeSlider?, Product?>>> = _homeItems

    fun fetchHomeItems() {
        if(productsList.size>0 && homeSliderList.size>0) {
            val maxSize = maxOf(homeSliderList.size, productsList.size)
            //val combinedList = mutableListOf<Pair<HomeSlider?, Product?>>()
            for (i in 0 until maxSize) {
                val slider = if (i < homeSliderList.size) homeSliderList.toList()[i] else null
                val product = if (i < productsList.size) productsList.toList()[i] else null
                addHomeItems(Pair(slider, product))
                //combinedList.add()
            }
        }
    }

//    private val _productItems = MutableLiveData<MutableSet<Product>>().apply { value = mutableSetOf() }
//    val productItems: LiveData<MutableSet<Product>> = _productItems
//
//    private val _homeSliderItems = MutableLiveData<MutableSet<HomeSlider>>().apply { value = mutableSetOf() }
//    val homeSliderItems: LiveData<MutableSet<HomeSlider>> = _homeSliderItems

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
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Home RecyclerView
        noNetworkContainer = view.findViewById(R.id.noNetworkContainer)
        rcHome = view.findViewById(R.id.rcHome)
        homeScrollView = view.findViewById(R.id.homeScrollView)
        rcHome.layoutManager = LinearLayoutManager(context)
        rcHome.adapter = HomeAdapter(requireContext(),requireActivity(),mutableListOf())

        activityActionListener?.loadCart()
        activityActionListener?.showOrHideCart(true)

        // Observe changes to the homeItems
        homeItems.observe(viewLifecycleOwner, Observer { homeList ->
            Log.e("Test","HomeList Changed - $homeList")
            rcHome.adapter = HomeAdapter(requireContext(),requireActivity(),homeList.toList())
        })

        // Observe changes to the productList
//        productItems.observe(viewLifecycleOwner, Observer { productList ->
//            Log.e("Test","ProductList Changed - $productList")
//            rcHome.adapter = HomeAdapter(requireContext(), productList)
//        })

//        // Observe changes to the homeSliderList
//        homeSliderItems.observe(viewLifecycleOwner, Observer { homeSliderList ->
//            Log.e("Test","HomeSliderList Changed - $homeSliderList")
//            //rcHome.adapter = HomeAdapter(requireContext(), productList)
//        })

//        btnTop.setOnClickListener {
//            homeScrollView.scrollTo(0,0)
//        }

        networkAvailabilityCheck()

    }
    override fun onDestroy() {
        super.onDestroy()
        productListener.remove()
        homeSliderListener.remove()
    }

    fun networkAvailabilityCheck() {
        if (isNetworkAvailable(requireContext())) {
            //btnTop.visibility = View.VISIBLE
            homeScrollView.visibility = View.VISIBLE
            noNetworkContainer.visibility = View.GONE
        } else {
            //btnTop.visibility = View.GONE
            homeScrollView.visibility = View.GONE
            noNetworkContainer.visibility = View.VISIBLE
        }
    }

    fun listenToProducts(onProductsChanged: (List<Product>) -> Unit): ListenerRegistration {
        val db = FirebaseFirestore.getInstance()
        return db.collection("product")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    // Handle the error
                    println("Listen failed: $e")
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val productList = mutableListOf<Product>()
                    for (document in snapshots.documents) {
                        val product = document.toObject(Product::class.java)
                        product?.let { productList.add(it) }
                    }
                    onProductsChanged(productList)
                } else {
                    println("No products found")
                }
            }
    }

    fun listenToHomeSliders(onHomeSlidersChanged: (List<HomeSlider>) -> Unit): ListenerRegistration {
        val db = FirebaseFirestore.getInstance()
        return db.collection("homeSlider")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    // Handle the error
                    println("Listen failed: $e")
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val sliderList = mutableListOf<HomeSlider>()
                    for (document in snapshots.documents) {
                        val slider = document.toObject(HomeSlider::class.java)
                        slider?.let { sliderList.add(it) }
                    }
                    onHomeSlidersChanged(sliderList)
                } else {
                    println("No products found")
                }
            }
    }

    private fun addHomeItems(item: Pair<HomeSlider?, Product?>) {
        _homeItems.value?.apply {
            add(item)
            _homeItems.value = this
        }
    }
//    private fun addProductItem(item: Product) {
//        _productItems.value?.apply {
//            add(item)
//            _productItems.value = this
//        }
//    }
//    private fun addSliderItem(item: HomeSlider) {
//        _homeSliderItems.value?.apply {
//            add(item)
//            _homeSliderItems.value = this
//        }
//    }
}