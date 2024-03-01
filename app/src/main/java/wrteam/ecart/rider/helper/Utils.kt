package wrteam.ecart.rider.helper

import android.annotation.SuppressLint
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.EditText
import wrteam.ecart.rider.R

object Utils {
    @SuppressLint("ClickableViewAccessibility")
    fun setHideShowPassword(edtPassword: EditText) {
        edtPassword.tag = Constant.SHOW
        edtPassword.setOnTouchListener(OnTouchListener { _, event ->
            val drawableRight = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= edtPassword.right - edtPassword.compoundDrawables[drawableRight].bounds.width()) {
                    if (edtPassword.tag == Constant.SHOW) {
                        edtPassword.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_password,
                            0,
                            R.drawable.ic_hide,
                            0
                        )
                        edtPassword.transformationMethod = null
                        edtPassword.tag = Constant.HIDE
                    } else {
                        edtPassword.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_password,
                            0,
                            R.drawable.ic_show,
                            0
                        )
                        edtPassword.transformationMethod = PasswordTransformationMethod()
                        edtPassword.tag = Constant.SHOW
                    }
                    return@OnTouchListener true
                }
            }
            false
        })
    }
}