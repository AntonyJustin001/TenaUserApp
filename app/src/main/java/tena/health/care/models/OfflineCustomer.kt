package tena.health.care.models

data class OfflineCustomer(
    val userId: String = "",
    val name: String = "",
    val emailId: String = "",
    val mobileNo: String = "",
    val whatsapp: String = "",
    val dateOfBirth: String = "",
    val soldProduct:String = "",
    val homeAddress: String = "",
    val workAddress: String = "",
    val instaId: String = "",
    val pointsEarned: String = "",
    val offersAvailable: String = "",
    val bio: String = "",
    val profilePic: String = "",
    val fcmToken:String = "",
    val addedDate:String = ""
)