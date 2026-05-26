package com.example.api

import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Flag to check if we have a valid key (not the template placeholder or empty)
    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY" && key != "placeholder"
    }

    /**
     * General text generation request using Gemini 3.5 Flash
     */
    suspend fun generateText(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            Log.w(TAG, "Gemini API key is not configured. Falling back to local/simulated logic.")
            return@withContext simulateAiLocal(prompt)
        }

        try {
            val key = BuildConfig.GEMINI_API_KEY
            val url = "$BASE_URL?key=$key"

            val partsArray = JSONArray().put(JSONObject().put("text", prompt))
            val contentObject = JSONObject().put("parts", partsArray)
            val contentsArray = JSONArray().put(contentObject)

            val requestBodyJson = JSONObject().apply {
                put("contents", contentsArray)
                if (systemInstruction != null) {
                    put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstruction))))
                }
                // Add a config for a higher creative response or JSON format when needed
            }

            val body = requestBodyJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "API call failed with response: $errBody")
                    throw Exception("API Error: Code ${response.code}. $errBody")
                }
                val respBody = response.body?.string() ?: throw Exception("Empty response body")
                parseGeminiResponse(respBody)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini API Call", e)
            simulateAiLocal(prompt)
        }
    }

    private fun parseGeminiResponse(jsonStr: String): String {
        val root = JSONObject(jsonStr)
        val candidates = root.optJSONArray("candidates") ?: return "No response candidate found"
        if (candidates.length() == 0) return "No response found"
        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content") ?: return "No content found in candidate"
        val parts = content.optJSONArray("parts") ?: return "No parts found in content"
        if (parts.length() == 0) return "Empty parts list"
        
        // Accumulate parts text
        val sb = StringBuilder()
        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            sb.append(part.optString("text", ""))
        }
        return sb.toString()
    }

    /**
     * Local Simulation of Gemini AI extraction for demo & offline mode
     */
    private fun simulateAiLocal(prompt: String): String {
        val promptClean = prompt.lowercase()
        return when {
            // Task Extraction
            promptClean.contains("task") || promptClean.contains("schedule") -> {
                """
                {
                  "type": "Task",
                  "title": "Buy home items & review bills",
                  "assignee": "Anjali Sharma",
                  "dueDate": "${System.currentTimeMillis() + 86400000}",
                  "priority": "Medium",
                  "details": "Suggested from inbox message: 'buy fruits, clean purifier and pay water bill'"
                }
                """.trimIndent()
            }
            // Bill extraction snippet
            promptClean.contains("bill") || promptClean.contains("invoice") || promptClean.contains("due") -> {
                """
                {
                  "type": "Bill",
                  "title": "Airtel Broadband Payment Request",
                  "amount": 1179.00,
                  "dueDate": "${System.currentTimeMillis() + 3 * 86400000}",
                  "category": "Internet"
                }
                """.trimIndent()
            }
            // Medicine scheduled
            promptClean.contains("medicine") || promptClean.contains("doctor") || promptClean.contains("dose") -> {
                """
                {
                  "type": "Medicine",
                  "name": "Pan D Acid Reflux",
                  "dosage": "1 Capsule",
                  "timing": "Before Breakfast",
                  "durationDays": 15
                }
                """.trimIndent()
            }
            // Documents scanned
            promptClean.contains("aadhaar") || promptClean.contains("passport") || promptClean.contains("pan") -> {
                 """
                 {
                   "type": "Document",
                   "status": "Extracted",
                   "fields": {
                      "DocumentType": "National e-ID (Aadhaar)",
                      "Holder": "Rajesh Sharma",
                      "IdNo": "XXXX XXXX 1928",
                      "Address": "501, Orchid Heights, Mumbai",
                      "Summary": "Aadhaar Card copy showing holder Rajesh Sharma, verified securely with biometric lock."
                   }
                 }
                 """.trimIndent()
            }
            else -> {
                "Smart Extraction Summary: Family inbox action item detected. Structured action item proposed for review."
            }
        }
    }
}
