package se.allco.githubbrowser.common.utils

/**
 * [callStackDepth] defines a point of interest in the call stack. In most cases it should be `1`.
 * @returns string with contains "<method_name> at <filename>:<linenumber>" from where it was called.
 */
fun getCallPlace(callStackDepth: Int = 1): String {
    val stackTrace = Throwable().stackTrace
    check(stackTrace.size > callStackDepth) { "Synthetic stacktrace didn't have enough elements: are you using proguard?" }
    val e = stackTrace[callStackDepth]
    val className = e.className.substring(e.className.lastIndexOf('.') + 1)
    return "$className.${e.methodName}(..) at ${e.fileName}:${e.lineNumber}"
}
