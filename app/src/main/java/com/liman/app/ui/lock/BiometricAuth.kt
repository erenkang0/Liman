package com.liman.app.ui.lock

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/** Compose [Context]'inden barındıran [FragmentActivity]'yi bulur. */
fun Context.findActivity(): FragmentActivity? {
    var ctx: Context = this
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/** Cihazda kullanılabilir, kayıtlı bir biyometri var mı? */
fun canUseBiometric(context: Context): Boolean {
    val manager = BiometricManager.from(context)
    return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
        BiometricManager.BIOMETRIC_SUCCESS
}

/** Biyometrik (parmak izi / yüz tanıma) doğrulama akışını başlatır. */
fun promptBiometric(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onFailed: () -> Unit = {},
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onFailed()
            }
        },
    )
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Liman kilidi")
        .setSubtitle("İç dünyana erişmek için kimliğini doğrula")
        .setNegativeButtonText("PIN kullan")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        .build()
    prompt.authenticate(info)
}
