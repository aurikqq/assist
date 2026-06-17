package com.aurikqq.assist

import android.util.Log
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

object Root {
    fun execute(command: String): ArrayList<String?> {
        return executeList(arrayListOf(command))
    }

    fun executeList(commands: List<String>): ArrayList<String?> {
        val process = Runtime.getRuntime().exec("su") ?: return arrayListOf(null, null)
        val outputStream = DataOutputStream(process.outputStream)
        for (command in commands) {
            outputStream.writeBytes(command + "\n")
        }
        outputStream.writeBytes("exit\n")
        outputStream.flush()
        outputStream.close()
        process.waitFor()

        return extractOutput(process)
    }

    fun executeSingle(command: String) {
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
        process.waitFor()
    }

    private fun extractOutput(process: Process): ArrayList<String?> {
        val stdInput = BufferedReader(InputStreamReader(process.inputStream))
        val stdError = BufferedReader(InputStreamReader(process.errorStream))
        var s: String?
        var o: String? = ""
        var e: String? = ""
        while (stdInput.readLine().also { s = it } != null) { o += s }
        while (stdError.readLine().also { s = it } != null) { e += s }

        if (o.isNullOrBlank()) o = null
        if (e.isNullOrBlank()) e = null

        Log.d("su", "[success] $o")
        Log.d("su", "[error] $e")

        return arrayListOf(o, e)
    }
}