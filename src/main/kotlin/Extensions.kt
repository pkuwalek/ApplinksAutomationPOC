package org.example

fun String.removeUrlPrefix(): String {
    val regex = Regex("https:\\/\\/[^\\/]+")
    return this.replace(regex, "")
}

fun String.removeUrlPrefixAndAddTrailing(): String {
    val regex = Regex("https:\\/\\/[^\\/]+")
    return this.replace(regex, "") + "/"
}
