package tena.health.care.screens.splash

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import tena.health.care.R
import tena.health.care.data.preference.SharedPreferencesHelper
import tena.health.care.interfaces.ActivityActionListener
import tena.health.care.models.User
import tena.health.care.screens.home.HomeScreen
import tena.health.care.screens.intro.IntroScreen
import tena.health.care.screens.signIn.SignInScreen
import tena.health.care.screens.signUp.SignUpScreen
import tena.health.care.utils.COMPLETE_INTRO
import tena.health.care.utils.USER_DETAILS
import tena.health.care.utils.loadScreen
import tena.health.care.utils.prefs

class SplashScreen : Fragment() {

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
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.e("Test","User Details - ${prefs.get(USER_DETAILS, "")}")

        activityActionListener?.showOrHideCart(false)

        Handler(Looper.getMainLooper()).postDelayed({
            if(prefs.get(COMPLETE_INTRO,"") == "completed") {
                if(prefs.get(USER_DETAILS,"")!="") {
                    loadScreen(requireActivity(),HomeScreen())
                } else {
                    loadScreen(requireActivity(), SignInScreen())
                }
            } else {
                loadScreen(requireActivity(), IntroScreen())
            }
        }, 5000)
    }

    override fun onDetach() {
        super.onDetach()
        activityActionListener = null // Clean up the reference
    }

}