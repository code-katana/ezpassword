package com.codekatana.passwordgen

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import java.time.Duration
import java.time.Instant
import java.util.*

class WordProcessor(private val context: Context) {

    fun getWords() {
        val pref = context.getSharedPreferences(context.getString(R.string.usageFile), Context.MODE_PRIVATE)
        val lastRun = pref.getLong("LastRun", Date().time)
        val runSinceLastPull = pref.getInt("runSinceLastPull", 0)

        if (wordBank.wordEntries.size == 0 || runSinceLastPull % 7 == 0
                || Duration.between(Date(lastRun).toInstant(), Instant.now()).toMinutes() > 60) {
            pullNewData()
            with(pref.edit()) {
                putLong("LastRun", Date().time)
                putInt("runSinceLastPull", runSinceLastPull + 1)
                apply()
            }
        }
    }

    private fun pullNewData() {
        Log.d(javaClass.simpleName, "Pulling new data from Wikipedia.")
        val rand = Random()
        val randomJsonRequest = JsonObjectRequest(Request.Method.GET, randomQuery, null,
                Response.Listener { response ->
                    val randResponse = response.optJSONObject("query")
                            ?.optJSONArray("random")
                    val articleId = randResponse?.optJSONObject(rand.nextInt(randResponse.length()))
                            ?.optLong("id")

                    if (articleId != null) {
                        processArticle(articleId)
                    }
                },
                Response.ErrorListener { error ->
                    Log.e(javaClass.simpleName, error.toString())
                }
        )

        MySingleton.getInstance(context).addToRequestQueue(randomJsonRequest)
    }

    private fun processArticle(articleId: Long) {
        val articleRequest = JsonObjectRequest(Request.Method.GET, specificQuery.format(articleId),
                null,
                Response.Listener { response ->
                    val result = mutableSetOf<String>()
                    val article = response.optJSONObject("query")
                            ?.optJSONObject("pages")
                            ?.optJSONObject("$articleId")
                            ?.optString("extract") ?: ""
                    val cleanText = article.replace("[^a-zA-Z\\s]".toRegex(), "")
                    cleanText.split("\\s+".toRegex()).filter { it.length > 1 }.toCollection(result)
                    wordBank.saveNewWords(result)
                },
                Response.ErrorListener {
                    Log.e(javaClass.simpleName, it.toString())
                })
        MySingleton.getInstance(context.applicationContext).addToRequestQueue(articleRequest)
    }

    companion object {
        private const val WIKI_URL = "https://en.wikipedia.org/w/api.php"
        private const val randomQuery = "$WIKI_URL?action=query&format=json&list=random&rnnamespace=0&" +
                "rnfilterredir=nonredirects&rnlimit=3"
        private const val specificQuery = "$WIKI_URL?action=query&format=json&prop=extracts&explaintext=1&pageids=%s"
    }
}
