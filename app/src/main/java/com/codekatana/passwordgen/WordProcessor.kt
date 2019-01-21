package com.codekatana.passwordgen

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.codekatana.passwordgen.brains.WordBank
import java.time.Duration
import java.time.Instant
import java.util.*

class WordProcessor(private val context: Context) {
    var listener: DownloadListener? = null
    var requestedWords: Int? = null

    fun getWords() {
        val wordBank = WordBank(context)

        val pref = context.getSharedPreferences(context.getString(R.string.usageFile), Context.MODE_PRIVATE)
        val lastRun = pref.getLong("LastRun", Date().time)
        val runSinceLastPull = pref.getInt("runSinceLastPull", 0)
        val firstRun = wordBank.wordEntries.size == 0
        if (firstRun) {
            pullNewData(true)
        } else if (runSinceLastPull % 7 == 0
                || Duration.between(Date(lastRun).toInstant(), Instant.now()).toMinutes() > 60) {
            pullNewData(false)
            // If it has data
            listener?.updateUI(wordBank.getWords(requestedWords ?: 3))
        } else {
            listener?.updateUI(wordBank.getWords(requestedWords ?: 3))
        }

        with(pref.edit()) {
            putLong("LastRun", Date().time)
            putInt("runSinceLastPull", runSinceLastPull + 1)
            apply()
        }
    }

    private fun pullNewData(shouldUpdateUi: Boolean) {
        Log.d(javaClass.simpleName, "Pulling new data from Wikipedia. First run: $shouldUpdateUi")
        val rand = Random()
        val randomJsonRequest = JsonObjectRequest(Request.Method.GET, randomQuery, null,
                Response.Listener { response ->
                    val randResponse = response.optJSONObject("query")
                            ?.optJSONArray("random")
                    val articleId = randResponse?.optJSONObject(rand.nextInt(randResponse?.length()))
                            ?.optLong("id")

                    if (articleId != null) {
                        processArticle(articleId, shouldUpdateUi)
                    }
                },
                Response.ErrorListener { error ->
                    Log.e(javaClass.simpleName, error.toString())
                }
        )

        MySingleton.getInstance(context).addToRequestQueue(randomJsonRequest)
    }

    private fun processArticle(articleId: Long?, shouldUpdateUi: Boolean) {
        val articleRequest = JsonObjectRequest(Request.Method.GET, specificQuery.format(articleId),
                null,
                Response.Listener { response ->
                    val article = response.optJSONObject("query")
                            ?.optJSONObject("pages")
                            ?.optJSONObject("$articleId")
                            ?.optString("extract") ?: ""
                    val cleanText = article.replace("[^a-zA-Z\\s]".toRegex(), "")
                    val words = cleanText.split("\\s+".toRegex()).filterNot { it.isEmpty() }

                    val result = mutableSetOf<String>()
                    words.forEach {
                        if (it.length > 1) result.add(it.toLowerCase())
                    }
                    val wordBank = WordBank(context)
                    wordBank.saveNewWords(result)
                    if (shouldUpdateUi) {
                        listener?.updateUI(wordBank.getWords(requestedWords ?: 3))
                    }
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
        private val specificQuery = "$WIKI_URL?action=query&format=json&prop=extracts&explaintext=1&pageids=%s"
    }
}
