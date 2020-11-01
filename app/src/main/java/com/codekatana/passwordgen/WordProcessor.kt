package com.codekatana.passwordgen

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.codekatana.passwordgen.EzPassApplication.Companion.context
import java.time.Duration
import java.time.Instant
import java.util.*

class WordProcessor {

    fun getWords() {
        val pref = context.getSharedPreferences(context.getString(R.string.usageFile), Context.MODE_PRIVATE)
        val lastRun = pref.getLong("LastRun", Date().time)
        val runSinceLastPull = pref.getInt("runSinceLastPull", 0)

        if (EzPassApplication.wordBank.wordEntries.size == 0 || runSinceLastPull % 7 == 0
                || Duration.between(Date(lastRun).toInstant(), Instant.now()).toMinutes() > 60) {
            pullNewData()
        }
        with(pref.edit()) {
            putLong("LastRun", Date().time)
            putInt("runSinceLastPull", runSinceLastPull + 1)
            apply()
        }
    }

    private fun pullNewData() {
        Log.d(javaClass.simpleName, "Pulling new data from Wikipedia.")
        val rand = Random()
        val randomJsonRequest = JsonObjectRequest(Request.Method.GET, randomQuery, null,
                { response ->
                    val randResponse = response.optJSONObject("query")
                            ?.optJSONArray("random")
                    val articleId = randResponse?.optJSONObject(rand.nextInt(randResponse.length()))
                            ?.optLong("id")

                    if (articleId != null) {
                        processArticle(articleId)
                    }
                },
                { error ->
                    Log.e(javaClass.simpleName, error.toString())
                }
        )

        MySingleton.getInstance(context).addToRequestQueue(randomJsonRequest)
    }

    private fun processArticle(articleId: Long) {
        val articleRequest = JsonObjectRequest(Request.Method.GET, specificQuery.format(articleId),
                null,
                { response ->
                    val result = mutableSetOf<String>()
                    val article = response.optJSONObject("query")
                            ?.optJSONObject("pages")
                            ?.optJSONObject("$articleId")
                            ?.optString("extract") ?: ""
                    val cleanText = article.replace("[^a-zA-Z\\s]".toRegex(), "")
                    cleanText.split("\\s+".toRegex()).filter { it.length > 1 }.toCollection(result)
                    EzPassApplication.wordBank.saveNewWords(result)
                },
                {
                    Log.e(javaClass.simpleName, it.toString())
                })
        MySingleton.getInstance(context.applicationContext).addToRequestQueue(articleRequest)
    }

    companion object {
        private const val WIKI_URL = "https://en.wikipedia.org/w/api.php"
        private const val randomQuery = "$WIKI_URL?action=query&format=json&list=random&rnnamespace=0&" +
                "rnfilterredir=nonredirects&rnlimit=3"
        private const val specificQuery = "$WIKI_URL?action=query&format=json&prop=extracts&explaintext=1&pageids=%s"

        @VisibleForTesting
        fun sprinkleNumbers(input: String): String {
            var gen = input
            when {
                gen.contains("o") -> gen = gen.replaceFirst("o", "0")
                gen.contains("e") -> gen = gen.replaceFirst("e", "3")
                gen.contains("l") -> gen = gen.replaceFirst("l", "1")
            }
            val numContent = gen.replace(Regex("[^0-9]"), "")
            Log.d("EzPass", "numContent=$numContent")
            if (numContent.isBlank()) {
                Log.i("EzPass", "No number to replace. Adding rand at end")
                gen += (Random().nextInt() % 10)
            }
            return gen
        }

    }
}
