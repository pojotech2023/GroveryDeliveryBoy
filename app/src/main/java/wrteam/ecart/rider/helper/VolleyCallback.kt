package wrteam.ecart.rider.helper

interface VolleyCallback {
    fun onSuccess(
        result: Boolean,
        message: String?
    ) //void onSuccessWithMsg(boolean result, String message);
}