package tena.health.care.screens.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import tena.health.care.MainActivity
import tena.health.care.R
import tena.health.care.interfaces.ActivityActionListener
import tena.health.care.models.User
import tena.health.care.screens.cart.CartScreen
import tena.health.care.screens.notifications.NotificationListScreen
import tena.health.care.screens.orders.OrderListScreen
import tena.health.care.utils.USER_DETAILS
import tena.health.care.utils.loadImageFromUrl
import tena.health.care.utils.loadScreen
import tena.health.care.utils.prefs

class AboutUsScreen : Fragment() {

    private lateinit var backBtnHolder: LinearLayout
    private var activityActionListener: ActivityActionListener? = null

    private lateinit var webView: WebView

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
        return inflater.inflate(R.layout.fragment_about_us_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityActionListener?.showOrHideCart(false)

        backBtnHolder = view.findViewById(R.id.backBtnHolder)
        backBtnHolder.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Initialize WebView
        webView = view.findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("https://tenahealthcare.com/about/")
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true


    }

}