@file:Suppress("unused", "UNUSED_PARAMETER")
package android.content
import android.content.res.Resources
import android.os.Parcelable
abstract class Context {
    open val applicationContext: Context get() = TODO()
    open val resources: Resources get() = TODO()
    open val packageName: String get() = TODO()
}
open class Intent() {
    constructor(context: Context, cls: Class<*>) : this()
    fun putExtra(name: String, value: Parcelable?): Intent = this
    fun putParcelableArrayListExtra(name: String, value: java.util.ArrayList<out Parcelable>?): Intent = this
}
