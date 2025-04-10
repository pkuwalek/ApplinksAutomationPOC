package org.example

class LoggingHelper() {
    fun logApplinksArray(
        appFlavor: String,
        firstNum: Int,
        numberOfLinks: Int,
    ) {
        println("FOR applinks_array.xml")
        for (i in firstNum .. firstNum + numberOfLinks) {
            println("<item>@string/applink_${appFlavor}_$i</item>")
        }

        println("")
        println("FOR applinks_array.xml - RESOLUTION")
        for (i in firstNum .. firstNum + numberOfLinks) {
            println("<item>@string/applink_${appFlavor}_${i}_resolution</item>")
        }
    }

    fun logAndroidManifest(
        appFlavor: String,
        firstNum: Int,
        numberOfLinks: Int,
    ) {
        println("")
        println("FOR AndroidManifest.xml")
        for (i in firstNum .. firstNum + numberOfLinks) {
            println("<data android:path=\"@string/applink_${appFlavor}_${i}\" />")
        }

        println("")
        println("FOR AndroidManifest.xml - TRAILING")
        for (i in firstNum .. firstNum + numberOfLinks) {
            println("<data android:path=\"@string/applink_${appFlavor}_${i}_trailing\" />")
        }
    }
}
