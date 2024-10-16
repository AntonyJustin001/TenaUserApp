package tena.health.care.models

data class User(
    val userId: String = "",
    val name: String = "",
    val emailId: String = "",
    val mobileNo: String = "",
    val whatsapp: String = "",
    val DOB: String = "",
    val homeAddress: String = "",
    val workAddress: String = "",
    val instaId: String = "",
    val pointsEarned: String = "",
    val offersAvailable: String = "",
    val bio: String = "",
    val profilePic: String = "",
    val fcmToken:String = ""
)