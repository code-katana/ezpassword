package com.codekatana.passwordgen

import android.os.AsyncTask
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * The background task that will call the Wikipedia API to get a random article and get random text
 * Created by arumugam on 3/3/18.
 */

class WikiDownloader : AsyncTask<Int, Int, List<String>>() {

    override fun doInBackground(vararg integers: Int?): List<String> {
        val client = OkHttpClient()
        val count = integers[0] ?: 0
        var article = ""
        val result = ArrayList<String>(count)
        val rand = Random()
        val request = Request.Builder()
                .url(WIKI_URL + "?action=query&format=json&list=random&rnnamespace=0&" +
                        "rnfilterredir=nonredirects&rnlimit=3")
                .build()
        // Get a handler that can be used to post to the main thread
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            }

            val responseString = response.body()!!.string()
            // Get the JSON articles from the response

            val respJson = JSONObject(responseString)
            val query = respJson.getJSONObject("query")
            if (query != null) {
                val randoms = query.getJSONArray("random")
                val randomObj = randoms.getJSONObject(rand.nextInt(randoms.length()))
                val articleId = randomObj.getLong("id")
                val articleRequest = Request.Builder()
                        .url("$WIKI_URL?action=query&format=json&prop=extracts&explaintext=1&pageids=$articleId")
                        .build()
                val articleResponse = client.newCall(articleRequest).execute()
                val articleObject = JSONObject(articleResponse.body()!!.string())

                article = articleObject.getJSONObject("query")
                        .getJSONObject("pages")
                        .getJSONObject(java.lang.Long.toString(articleId))
                        .getString("extract")

            }
            val cleanText = article.replace("[^a-zA-Z\\s]".toRegex(), "")
            val words = cleanText.split("\\s+".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            val numWords = words.size

            var i = 0
            while (i < count) {
                val n = words[rand.nextInt(numWords)]
                if (n.length > 1) {
                    result.add(n.toLowerCase())
                } else {
                    i--
                }
                i++
            }
            return result

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return result
    }

    companion object {
        private val WIKI_URL = "https://en.wikipedia.org/w/api.php"
        private const val APPLICATION_JSON = "application/json"
        private const val EZ_PASS = "EzPass"
    }
}
