package tena.health.care.screens.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.tasks.Tasks
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import tena.health.care.MainActivity
import tena.health.care.R
import tena.health.care.adapter.HomeAdapter
import tena.health.care.dialog.CustomDialog
import tena.health.care.interfaces.ActivityActionListener
import tena.health.care.models.CartItem
import tena.health.care.models.HomeSlider
import tena.health.care.models.Order
import tena.health.care.models.Product
import tena.health.care.models.User
import tena.health.care.screens.cart.CartManager
import tena.health.care.screens.cart.CartScreen
import tena.health.care.screens.home.HomeScreen
import tena.health.care.screens.orders.OrderListScreen
import tena.health.care.utils.USER_DETAILS
import tena.health.care.utils.isNetworkAvailable
import tena.health.care.utils.loadImageFromUrl
import tena.health.care.utils.loadScreen
import tena.health.care.utils.prefs

class EditProfileScreen : Fragment() {

    private lateinit var backBtnHolder: LinearLayout
    private lateinit var ivProfilePic: ImageView
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etMobileNo: EditText
    private lateinit var etBio: EditText
    private lateinit var btnSave: LinearLayout
    private lateinit var progressBar: LottieAnimationView
    private var imageUrl = ""

    private val PICK_IMAGE_REQUEST = 1

    lateinit var db:FirebaseFirestore
    private var activityActionListener: ActivityActionListener? = null
    lateinit var userDetails:User

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
        return inflater.inflate(R.layout.fragment_edit_profile_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        activityActionListener?.showOrHideCart(false)
        userDetails = Gson().fromJson(prefs.get(USER_DETAILS, ""), User::class.java)

        backBtnHolder = view.findViewById(R.id.backBtnHolder)
        backBtnHolder.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        progressBar = view.findViewById(R.id.progressBar)
        ivProfilePic = view.findViewById(R.id.ivProfilePic)
        etFullName = view.findViewById(R.id.etFullName)
        etEmail = view.findViewById(R.id.etEmail)
        etMobileNo = view.findViewById(R.id.etMobileNo)
        etBio = view.findViewById(R.id.etBio)
        btnSave = view.findViewById(R.id.btnSave)

        ivProfilePic.setOnClickListener {
            openFileChooser()
        }

        btnSave.setOnClickListener {
            val updatedDetails = hashMapOf(
                "name" to etFullName.text.toString(),
                "mobileNo" to etMobileNo.text.toString(),
                "profilePic" to imageUrl,
                "bio" to etBio.text.toString(),
            )
            updateProfileDetailsByEmail(userDetails.userId, updatedDetails)
        }

        val userRef = db.collection("users")
            .document(userDetails.userId)
        userRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)
                etFullName.setText(user?.name)
                etEmail.setText(user?.emailId)
                etEmail.isEnabled = false
                etMobileNo.setText(user?.mobileNo)
                etBio.setText(user?.bio)
                loadImageFromUrl(requireContext(),user?.profilePic?:"",ivProfilePic)
            } else {
                Log.e("Firestore", "Product data is null")
            }
        }

    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                uploadImageToFirebase(imageUri)
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        progressBar.visibility = View.VISIBLE
        val storageReference = FirebaseStorage.getInstance().reference
        val fileReference = storageReference.child("profilePics/" + System.currentTimeMillis() + ".jpg")

        val uploadTask = fileReference.putFile(imageUri)

        // Show progress or handle completion
        uploadTask.addOnSuccessListener {
            // Image uploaded successfully
            Snackbar.make(requireView(), "Upload successful", Snackbar.LENGTH_LONG).show()

            // Get the download URL
            fileReference.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                loadImageFromUrl(requireContext(), downloadUrl, ivProfilePic)
                imageUrl = downloadUrl
                Log.d("Firebase Storage", "Image URL: $downloadUrl")
                progressBar.visibility = View.GONE
            }
        }.addOnFailureListener {
            // Handle failed upload
            Snackbar.make(requireView(), "Upload failed, Please try again", Snackbar.LENGTH_LONG).show()
            progressBar.visibility = View.GONE
        }.addOnProgressListener { taskSnapshot ->
            // You can display the progress of the upload here
            val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
            Log.d("Firebase Storage", "Upload is $progress% done")
        }
    }

    fun updateProfileDetailsByEmail(userId: String, updatedDetails: Map<String, Any>) {
        val db = FirebaseFirestore.getInstance()
        progressBar.visibility = View.VISIBLE
        db.collection("users")
            .whereEqualTo("userId", userId) // Query where name matches
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    for (document in documents) {
                        // Update the document found with new details
                        db.collection("users")
                            .document(userId)
                            .update(updatedDetails)
                            .addOnSuccessListener {
                                Snackbar.make(requireView(), "Profile Edited Successfully", Snackbar.LENGTH_LONG).show()
                                progressBar.visibility = View.GONE
                                parentFragmentManager.popBackStack()
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error updating document", e)
                                Snackbar.make(requireView(), "Something went wrong please try again", Snackbar.LENGTH_LONG).show()
                                progressBar.visibility = View.GONE
                                parentFragmentManager.popBackStack()
                            }
                    }
                } else {
                    Log.e("Firestore", "No such document found!")
                    Snackbar.make(requireView(), "Something went wrong please try again", Snackbar.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                    parentFragmentManager.popBackStack()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents: ", exception)
                Snackbar.make(requireView(), "Something went wrong please try again", Snackbar.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
                parentFragmentManager.popBackStack()
            }
    }

}