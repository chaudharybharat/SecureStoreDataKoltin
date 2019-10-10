package com.example.securestoredatakoltin

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import com.example.securestoredatakoltin.securealogritham.SecurePreferences
import com.example.securestoredatakoltin.securealogritham.SecureStorageException
import com.example.securestoredatakoltin.securealogritham.SecureStorageException.ExceptionType.CRYPTO_EXCEPTION
import com.example.securestoredatakoltin.securealogritham.SecureStorageException.ExceptionType.KEYSTORE_NOT_SUPPORTED_EXCEPTION
import com.example.securestoredatakoltin.securealogritham.SecureStorageException.ExceptionType.INTERNAL_LIBRARY_EXCEPTION
import com.example.securestoredatakoltin.securealogritham.SecureStorageException.ExceptionType.KEYSTORE_EXCEPTION
import kotlinx.android.synthetic.main.activity_main.*
class MainActivity : AppCompatActivity() {
    companion object {
        private const val KEY = "TEMPTAG"
        private const val TAG = "LOGTAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        generate_key_button.setOnClickListener { handleOnGenerateKeyButtonClick() }
        clear_field_button.setOnClickListener { handleOnClearFieldButtonClick() }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_clear_all -> {
                try {
                    SecurePreferences.clearAllValues(this@MainActivity)
                    Toast.makeText(this@MainActivity, "SecurePreferences cleared and KeyPair deleted", Toast.LENGTH_SHORT).show()
                    plain_message_edit_text.setText("")
                    key_info_text_view.text = ""
                    clear_field_button.isEnabled = false
                    shield_image.setImageResource(R.drawable.lock)
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                }
                return true
            }
            R.id.action_info -> {
                startActivity(
                    Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/chaudharybharat/SecureStoreDataKoltin.git"))
                )
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun handleOnGenerateKeyButtonClick() {
        if (!TextUtils.isEmpty(plain_message_edit_text.text)) {
            if (generate_key_button.text.toString() == getString(R.string.button_generate_encrypt)) {
                generate_key_button.setText(R.string.button_encrypt)
            }
            try {
                SecurePreferences.setValue(this@MainActivity, KEY, plain_message_edit_text.text.toString())
                val decryptedMessage = SecurePreferences.getStringValue(this@MainActivity, KEY, "")

                val fadeIn = AlphaAnimation(0f, 1f)
                fadeIn.duration = 500
                val fadeOut = AlphaAnimation(1f, 0f)
                fadeOut.duration = 500

                fadeOut.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationRepeat(animation: Animation) {}

                    override fun onAnimationEnd(animation: Animation) {
                        shield_image.setImageResource(R.drawable.lock)
                        shield_image.startAnimation(fadeIn)
                        clear_field_button.isEnabled = true

                        val finalMessage = String.format(getString(R.string.message_encrypted_decrypted,
                            plain_message_edit_text.text.toString(), decryptedMessage))
                        key_info_text_view.text = getSpannedText(finalMessage)
                    }
                })
                shield_image.startAnimation(fadeOut)
            } catch (e: SecureStorageException) {
                handleException(e)
            }
        } else {
            Toast.makeText(this@MainActivity, "Field cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleOnClearFieldButtonClick() {
        SecurePreferences.removeValue(this@MainActivity, KEY)
        plain_message_edit_text.setText("")
        key_info_text_view.text = ""
        clear_field_button.isEnabled = false
        shield_image.setImageResource(R.drawable.lock)
    }

    private fun getSpannedText(text: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(text)
        }
    }

    private fun handleException(e: SecureStorageException) {
        Log.e(TAG, e.message)
        when (e.type) {
            KEYSTORE_NOT_SUPPORTED_EXCEPTION -> Toast.makeText(this, R.string.error_not_supported, Toast.LENGTH_LONG).show()
            KEYSTORE_EXCEPTION -> Toast.makeText(this, R.string.error_fatal, Toast.LENGTH_LONG).show()
            CRYPTO_EXCEPTION -> Toast.makeText(this, R.string.error_encryption, Toast.LENGTH_LONG).show()
            INTERNAL_LIBRARY_EXCEPTION -> Toast.makeText(this, R.string.error_library, Toast.LENGTH_LONG).show()
            else -> return
        }
    }
}
