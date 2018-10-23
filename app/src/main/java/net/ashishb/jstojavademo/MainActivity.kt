package net.ashishb.jstojavademo

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast

/**
 * Check weather READ_CONTACTS permissions is granted via `adbe app info net.ashishb.jstojavademo`
 * Grant the permission via "adbe permissions grant net.ashishb.jstojavademo contacts"
 * Revoke the permission via "adbe permissions grant net.ashishb.jstojavademo contacts"
 * The app will crash because any exception on JavaBridge thread is caught and is being rethrown on the
 * main thread. Java Bridge on its own will just swallow the Exception and the Javascript code which is supposed
 * to handle the return value won't execute either leaving app is an inconsistent state.
 */
class MainActivity : AppCompatActivity() {

    private val jsTag = "JsInterface"

    private val htmlPage = """
    <html>
        <head>
        </head>
        <Button onClick='%s.getContacts()'>Click me</Button>
    </html>
    """.format(jsTag)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val webView = findViewById<WebView>(R.id.webView)
        webView.loadDataWithBaseURL("about:blank", htmlPage, "text/html", null, null)
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(WebkitJsInterface(this), jsTag)
    }

    class WebkitJsInterface(context: Context) {
        private val mContext = context

        @JavascriptInterface
        fun getContacts() {
            try {
                showToast("Getting contacts...")
                showToast("Thread is " + Thread.currentThread().name)
                getContactsUnsafe()
                showToast("Finished")
            } catch (e: Exception) {
                if (isSevereException(e)) {
                    rethrowOnMainThread(e)
                } else {
                    logError(e)
                }
            }
        }

        private fun logError(e: Exception) {
            Log.e("WebkitJsInterface", "Error occurred", e)
        }

        private fun rethrowOnMainThread(e: Exception) {
            Handler(Looper.getMainLooper()).post { throw e }
        }

        private fun isSevereException(e: Exception): Boolean {
            // More complex logic goes here
            return true
        }

        private fun getContactsUnsafe() {
            mContext.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null
            )
        }

        private fun showToast(msg: String) {
            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show()

        }
    }
}