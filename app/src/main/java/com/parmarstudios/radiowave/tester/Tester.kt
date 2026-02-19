package com.parmarstudios.radiowave.tester

fun main() {
    updateDnsList { result ->
        println("Servers:")
        result.forEach { println(it) }
    }
    // Wait for the thread to finish (not ideal for production, but works for this example)
    Thread.sleep(2000)
}

fun updateDnsList(onResult: (Array<String>) -> Unit) {
    Thread {
        val listResult = mutableListOf<String>()
        try {
            val list = java.net.InetAddress.getAllByName("all.api.radio-browser.info")
            for (item in list) {
                listResult.add(item.canonicalHostName)
            }
        } catch (e: java.net.UnknownHostException) {
            e.printStackTrace()
        }
        onResult(listResult.toTypedArray())
    }.start()
}