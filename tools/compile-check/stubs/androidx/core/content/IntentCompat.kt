@file:Suppress("unused", "UNUSED_PARAMETER")
package androidx.core.content
import android.content.Intent
object IntentCompat {
    @JvmStatic fun <T> getParcelableExtra(intent: Intent, name: String?, clazz: Class<T>): T? = null
    @JvmStatic fun <T> getParcelableArrayListExtra(intent: Intent, name: String?, clazz: Class<out T>): java.util.ArrayList<T>? = null
}
