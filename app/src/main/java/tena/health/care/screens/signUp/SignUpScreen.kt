package tena.health.care.screens.signUp

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.text.set
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import tena.health.care.R
import tena.health.care.dialog.CustomDialog
import tena.health.care.models.Product
import tena.health.care.models.User
import tena.health.care.screens.home.HomeScreen
import tena.health.care.screens.signIn.SignInScreen
import tena.health.care.utils.USER_DETAILS
import tena.health.care.utils.loadScreen
import tena.health.care.utils.prefs
import java.util.UUID
import kotlin.random.Random

class SignUpScreen : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var layoutSignUp: RelativeLayout
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etName: EditText
    private lateinit var tvSignIn: TextView
    private lateinit var progressBar: LottieAnimationView
    lateinit var customDialog: CustomDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etName = view.findViewById(R.id.etName)
        progressBar = view.findViewById(R.id.progressBar)
        tvSignIn = view.findViewById(R.id.tvSignIn)


        val fullText = resources.getText(R.string.already_have_ac_sign_in)
        val spannableString = SpannableString(fullText)

        // Set the first link ("Click here")
        val clickableSpan1 = object : ClickableSpan() {
            override fun onClick(widget: View) {
                loadScreen(requireActivity(), SignInScreen())
            }
        }
        val start1 = fullText.indexOf("SIGNIN")
        val end1 = start1 + "SIGNIN".length
        spannableString[start1, end1] = clickableSpan1
        spannableString[start1, end1] = ForegroundColorSpan(Color.BLUE)

        // Set the spannable string to the TextView
        tvSignIn.text = spannableString
        tvSignIn.movementMethod = android.text.method.LinkMovementMethod.getInstance()


        layoutSignUp = view.findViewById(R.id.layoutSignUp)
        layoutSignUp.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val name = etName.text.toString()
            progressBar.visibility = View.VISIBLE

            if (!email.equals("")) {
                if (!password.equals("")) {
                    if (!name.equals("")) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    // Store User Data in Firebase Database
                                    val user = auth.currentUser
                                    val profileUpdates = UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build()
                                    user?.updateProfile(profileUpdates)
//                                    val userId = user?.uid
//                                    val database = FirebaseDatabase.getInstance().getReference("Users")
//                                    val userMap = mapOf(
//                                        "displayName" to name,
//                                        "email" to email,
//                                    )
//                                    userId?.let { database.child(it).setValue(userMap) }

                                    //saveUser(User(userId = UUID.randomUUID().toString(), name = name, emailId = email))
                                    saveUser(User(userId = FirebaseAuth.getInstance().currentUser?.uid?:""
                                        , name = name, emailId = email))


                                    //Store UserData in Local
                                    val userData = User(userId = FirebaseAuth.getInstance().currentUser?.uid?:""
                                        ,name = name, emailId = email)
                                    prefs.put(USER_DETAILS, Gson().toJson(userData))

                                    //Show Dialog
                                    customDialog = CustomDialog(
                                        resources.getDrawable(R.drawable.setting_icon),
                                        resources.getString(R.string.congratulations),
                                        resources.getString(R.string.success_msg),
                                        true, 5000
                                    ) {
                                        //LoadHome Screen
                                        loadScreen(requireActivity(), HomeScreen())
                                        progressBar.visibility = View.GONE
                                        clearUI()
                                    }
                                    customDialog.show(childFragmentManager, "CustomDialog")

                                    Log.e("Test", "Sign-Up Successful")
                                } else {
                                    Log.e("Test", "Sign-Up Failed: ${task.exception?.message}")
                                    progressBar.visibility = View.GONE
                                    customDialog = CustomDialog(
                                        resources.getDrawable(R.drawable.ic_wrong),
                                        resources.getString(R.string.error),
                                        task.exception?.message.toString()
                                    )
                                    customDialog.show(childFragmentManager, "CustomDialog")
                                    clearUI()
                                }
                            }

                    } else {
                        customDialog = CustomDialog(
                            resources.getDrawable(R.drawable.ic_wrong),
                            resources.getString(R.string.error), "Please Enter Name"
                        )
                        customDialog.show(childFragmentManager, "CustomDialog")
                        progressBar.visibility = View.GONE
                        clearUI()
                    }
                } else {
                    customDialog = CustomDialog(
                        resources.getDrawable(R.drawable.ic_wrong),
                        resources.getString(R.string.error), "Please Enter Password"
                    )
                    customDialog.show(childFragmentManager, "CustomDialog")
                    progressBar.visibility = View.GONE
                    clearUI()
                }
            } else {
                customDialog = CustomDialog(
                    resources.getDrawable(R.drawable.ic_wrong),
                    resources.getString(R.string.error), "Please Enter Email"
                )
                customDialog.show(childFragmentManager, "CustomDialog")
                progressBar.visibility = View.GONE
                clearUI()
            }
        }
    }

    fun saveUser(user: User) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(user.userId)
            .set(user)
            .addOnSuccessListener {
                // Product successfully written!
                println("User added successfully")
            }
            .addOnFailureListener { e ->
                // Handle the error
                println("Error adding product: $e")
            }
    }

    fun clearUI() {
        //etEmail.setText("")
        etPassword.setText("")
        //etName.setText("")
    }

}