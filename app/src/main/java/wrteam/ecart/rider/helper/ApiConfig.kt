package wrteam.ecart.rider.helper

import android.app.Activity
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.security.Key
import java.util.*
import javax.crypto.spec.SecretKeySpec
import java.time.Instant

object ApiConfig {

    fun volleyErrorMessage(error: VolleyError?): String {
        var message = ""
        try {
            message = when (error) {
                is NetworkError -> {
                    "Cannot connect to Internet...Please check your connection!"
                }
                is ServerError -> {
                    "The server could not be found. Please try again after some time!!"
                }
                is AuthFailureError -> {
                    "Cannot connect to Internet...Please check your connection!"
                }
                is ParseError -> {
                    "Parsing error! Please try again after some time!!"
                }
                is TimeoutError -> {
                    "Connection TimeOut! Please check your internet connection."
                }
                else -> ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return message
    }

    fun setAppLocal(activity: Activity, languageCode: String) {
        val resources = activity.resources
        val dm = activity.resources.displayMetrics
        val configuration = resources.configuration
        configuration.setLocale(Locale(languageCode.lowercase(Locale.getDefault())))
        resources.updateConfiguration(configuration, dm)
    }

    fun disableSwipe(swipeRefreshLayout: SwipeRefreshLayout) {
        swipeRefreshLayout.isEnabled = false
        swipeRefreshLayout.postDelayed({ swipeRefreshLayout.isEnabled = true }, 3000)
    }

    fun createJWT(issuer: String?, subject: String?): String? {
        try {
            val signatureAlgorithm: SignatureAlgorithm = SignatureAlgorithm.HS256
            val apiKeySecretBytes = Constant.JWT_KEY.toByteArray()
            val signingKey: Key = SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.jcaName)
            val now: Instant = Instant.now()
            return Jwts.builder()
                .claim("cust", "22015911")
                .setSubject(subject)
                .setIssuer(issuer)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(Date.from(now))
                .signWith(signatureAlgorithm, signingKey)
                .compact()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun requestToVolley(
        callback: (Boolean, String?) -> Unit,
        activity: Activity,
        url: String,
        params: MutableMap<String, String?>
    ) {
        if (AppController.isConnected(activity)) {
            val stringRequest: StringRequest = object : StringRequest(
                Method.POST, url, Response.Listener { response: String? ->
                    callback(true, response.toString())
                },
                Response.ErrorListener { error: VolleyError ->
                    val message = volleyErrorMessage(error)
                    callback(false, message)
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                }) {
                override fun getHeaders(): MutableMap<String, String?> {
                    val params1: MutableMap<String, String?> = HashMap()
                    params1["Authorization"] =
                        "Bearer " + createJWT("eKart", "eKart Authentication")
                    return params1
                }

                override fun getParams(): MutableMap<String, String?> {
                    params[Constant.AccessKey] = Constant.AccessKeyVal
                    return params
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            AppController.instance?.getRequestQueue()?.cache?.clear()
            AppController.instance?.addToRequestQueue(stringRequest)
        }
    }
}
