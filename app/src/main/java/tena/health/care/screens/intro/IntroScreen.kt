package tena.health.care.screens.intro

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import tena.health.care.R
import tena.health.care.adapter.IntroSliderAdapter
import tena.health.care.interfaces.ActivityActionListener
import tena.health.care.screens.signIn.SignInScreen
import tena.health.care.screens.signUp.SignUpScreen
import tena.health.care.utils.COMPLETE_INTRO
import tena.health.care.utils.loadScreen
import tena.health.care.utils.prefs

class IntroScreen : Fragment() {

    private lateinit var viewPager: ViewPager
    private lateinit var sliderAdapter: IntroSliderAdapter
    private lateinit var dotsLayout: LinearLayout
    private lateinit var dots: Array<ImageView>
    private lateinit var nextHolder: RelativeLayout
    private lateinit var nextLayout: LinearLayout
    private lateinit var nextLayoutWithText: LinearLayout
    private val layouts = intArrayOf(
        R.layout.fragment_slide1,
        R.layout.fragment_slide2,
        R.layout.fragment_slide3
    )

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_intro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.viewPager)
        dotsLayout = view.findViewById(R.id.dotsLayout)
        nextHolder = view.findViewById(R.id.nextHolder)
        nextLayout = view.findViewById(R.id.layoutNext)
        nextLayoutWithText = view.findViewById(R.id.layoutNextWithText)

        activityActionListener?.showOrHideCart(false)

        nextLayoutWithText.visibility = View.GONE
        nextLayout.visibility = View.VISIBLE


        nextLayoutWithText.setOnClickListener {
            prefs.put(COMPLETE_INTRO,"completed")
            loadScreen(requireActivity(),SignInScreen())
        }

        sliderAdapter = IntroSliderAdapter(requireContext(), layouts)
        viewPager.adapter = sliderAdapter

        nextHolder.setOnClickListener {
            val currentItem = viewPager.currentItem
            if(currentItem == 1) {
                nextLayoutWithText.visibility = View.VISIBLE
                nextLayout.visibility = View.GONE
            } else {
                nextLayoutWithText.visibility = View.GONE
                nextLayout.visibility = View.VISIBLE
            }
            if (currentItem < layouts.size - 1) {
                viewPager.currentItem = currentItem + 1
                Log.e("Test","currentItem + 1 - ${currentItem + 1}")
            } else {
                viewPager.currentItem = 0
                Log.e("Test","currentItem = 0 - ${currentItem}")
            }
        }

        addDots(0) // Add initial dots
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener)
    }

    private fun addDots(currentPage: Int) {
        dots = Array(layouts.size) { ImageView(requireContext()) }
        dotsLayout.removeAllViews()

        for (i in dots.indices) {
            dots[i] = ImageView(requireContext())
            dots[i].setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.non_active_dot))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(10, 0, 10, 0)
            dotsLayout.addView(dots[i], params)
        }

        if (dots.isNotEmpty()) {
            dots[currentPage].setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.active_dot))
            animateDot(dots[currentPage])
        }
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

    private val viewPagerPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            addDots(position)
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        override fun onPageScrollStateChanged(state: Int) {}
    }
}
