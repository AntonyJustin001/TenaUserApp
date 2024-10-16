package tena.health.care.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import tena.health.care.R
import tena.health.care.models.HomeSlider
import tena.health.care.models.Product
import tena.health.care.models.User
import tena.health.care.screens.profile.ProfileScreen
import tena.health.care.utils.GridSpacingItemDecoration
import tena.health.care.utils.USER_DETAILS
import tena.health.care.utils.loadScreen
import tena.health.care.utils.prefs

class HomeAdapter(context: Context, activity:FragmentActivity, homeItemsList: List<Pair<HomeSlider?, Product?>>) :
    RecyclerView.Adapter<HomeAdapter.ItemViewHolder>() {

    var context: Context
    var activity: FragmentActivity
    private lateinit var sliderAdapter: HomeSliderAdapter
    private lateinit var viewPager: ViewPager2
    private val sliderHandler = Handler(Looper.getMainLooper())
    private var currentPage = 0
    private lateinit var dotsLayout: LinearLayout
    private lateinit var dots: Array<ImageView>

    var homeItemsList : List<Pair<HomeSlider?, Product?>> = emptyList()
    var productsList = mutableListOf<Product>()
    var sliderList = mutableListOf<HomeSlider>()

    var userDetails:User = User()

    init {
        this.context = context
        this.activity = activity
        this.homeItemsList = homeItemsList

        // Get UserDetails From Local
        userDetails = fetchUserDetails()

        // Parse SliderList and ProductList from HomeItemsList
        for (pair in homeItemsList) {
            pair.first?.let { sliderList.add(it) }
            pair.second?.let { productsList.add(it) }
        }
    }

    private val slideRunnable = object : Runnable {
        override fun run() {
            if (sliderAdapter.itemCount > 0) {
                currentPage = (currentPage + 1) % sliderAdapter.itemCount
                viewPager.setCurrentItem(currentPage, true)
                addDots(currentPage)
                sliderHandler.postDelayed(this, 3000)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rc_item_home, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = 1

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rcPopularProducts: RecyclerView = itemView.findViewById(R.id.rcPopularProducts)
        val rcSearchedProducts: RecyclerView = itemView.findViewById(R.id.rcSearchedProduct)
        val layoutHomeContent: LinearLayout = itemView.findViewById(R.id.layoutHomeContent)

        val scale = context.resources.displayMetrics.density
        val spacingPx = (context.resources.getDimension(com.intuit.sdp.R.dimen._6sdp) * scale).toInt()
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val etSearch: TextView = itemView.findViewById(R.id.etSearch)
        val ivClear: ImageView = itemView.findViewById(R.id.ivClear)
        val ivProfile: ImageView = itemView.findViewById(R.id.ivProfile)

        fun bind() {
            dotsLayout = itemView.findViewById(R.id.dotsLayout)
            sliderAdapter = HomeSliderAdapter(context,sliderList.toList())
            viewPager = itemView.findViewById(R.id.viewPager)
            viewPager.adapter = sliderAdapter
            tvUserName.text = "Hi, ${userDetails.name}"

            ivClear.setOnClickListener {
                etSearch.text = ""
                ivClear.visibility = View.GONE
                rcSearchedProducts.visibility = View.GONE
                layoutHomeContent.visibility = View.VISIBLE
            }

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
                        if(lastWord!="") {
                            ivClear.visibility = View.VISIBLE
                            searchProducts(lastWord) { searchedProducts ->
                                if(searchedProducts.size>0) {
                                    Log.e("Test","searchedProducts size - ${searchedProducts.size}")
                                    rcSearchedProducts.visibility = View.VISIBLE
                                    layoutHomeContent.visibility = View.GONE
                                    rcSearchedProducts.layoutManager = LinearLayoutManager(context)
                                    rcSearchedProducts.adapter = SearchProductAdapter(context, activity, searchedProducts)
                                } else {
                                    rcSearchedProducts.visibility = View.GONE
                                    layoutHomeContent.visibility = View.VISIBLE
                                }
                            }
                        } else {
                            rcSearchedProducts.visibility = View.GONE
                            layoutHomeContent.visibility = View.VISIBLE
                        }
                    }
                }
            })

            rcPopularProducts.layoutManager = GridLayoutManager(context, 2)
            rcPopularProducts.adapter = PopularProductAdapter(context, activity,productsList.toList())
            rcPopularProducts.addItemDecoration(GridSpacingItemDecoration(2,spacingPx,true))

            addDots(0) // Add initial dots
            // Auto-slide setup
            startAutoSlide()

            ivProfile.setOnClickListener {
                //loadScreen(activity, OrderListScreen())
                loadScreen(activity, ProfileScreen())
            }

        }

        private fun startAutoSlide() {
            sliderHandler.postDelayed(slideRunnable, 3000) // Slide every 3 seconds
        }

    }

    fun fetchUserDetails():User {
        val userDetailsRaw = prefs.get(USER_DETAILS, "")
        return Gson().fromJson(userDetailsRaw, User::class.java)
    }

    private fun animateDot(view: View) {
        val scaleAnimation = ScaleAnimation(
            0.8f, 1.6f, // Start and end values for the X axis scaling
            0.8f, 1.2f, // Start and end values for the Y axis scaling
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scaleAnimation.duration = 300
        scaleAnimation.fillAfter = true
        view.startAnimation(scaleAnimation)
    }

    private fun addDots(currentPage: Int) {
        dots = Array(sliderList.size) { ImageView(context) }
        dotsLayout.removeAllViews()

        for (i in dots.indices) {
            dots[i] = ImageView(context)
            dots[i].setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.non_active_dot
                )
            )
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(10, 0, 10, 0)
            dotsLayout.addView(dots[i], params)
        }

        if (dots.isNotEmpty()) {
            dots[currentPage].setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.active_dot
                )
            )
            animateDot(dots[currentPage])
        }
    }

    override fun onViewDetachedFromWindow(holder: ItemViewHolder) {
        super.onViewDetachedFromWindow(holder)
        sliderHandler.removeCallbacks(slideRunnable)
    }

    fun searchProducts(searchWord: String, onResult: (List<Product>) -> Unit) {
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
                onResult(emptyList())
            }
    }
}