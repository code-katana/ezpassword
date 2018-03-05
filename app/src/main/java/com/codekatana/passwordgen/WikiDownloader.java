package com.codekatana.passwordgen;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * The background task that will call the Wikipedia API to get a random article and get random text
 * Created by arumugam on 3/3/18.
 */

public class WikiDownloader extends AsyncTask<Integer, Integer, List<String>> {

    private static final String WIKI_URL = "https://en.wikipedia.org/w/api.php";
    private static final String APPLICATION_JSON = "application/json";
    private static final String EZ_PASS = "EzPass";

    @Override
    protected List<String> doInBackground(Integer... integers) {
        OkHttpClient client = new OkHttpClient();
        int count = integers[0];
        String article = "";

        Random rand = new Random();
        Request request = new Request.Builder()
                .url(WIKI_URL + "?action=query&format=json&list=random&rnnamespace=0&" +
                        "rnfilterredir=nonredirects&rnlimit=3")
                .build();
        // Get a handler that can be used to post to the main thread
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String responseString = response.body().string();
            // Get the JSON articles from the response

            JSONObject respJson = new JSONObject(responseString);
            JSONObject query = respJson.getJSONObject("query");
            if (query != null) {
                JSONArray randoms = query.getJSONArray("random");
                JSONObject randomObj = randoms.getJSONObject(rand.nextInt(randoms.length()));
                long articleId = randomObj.getLong("id");
                Request articleRequest = new Request.Builder()
                        .url(WIKI_URL + "?action=query&format=json&prop=extracts&explaintext=1&pageids=" + articleId)
                        .build();
                Response articleResponse = client.newCall(articleRequest).execute();
                JSONObject articleObject = new JSONObject((articleResponse.body().string()));

                article = articleObject.getJSONObject("query")
                        .getJSONObject("pages")
                        .getJSONObject(Long.toString(articleId))
                        .getString("extract");

            }
            String cleanText = article.replaceAll("[^a-zA-Z\\s]", "");
            String[] words = cleanText.split("\\s+");
            int numWords = words.length;
            List<String> result = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                String n = words[rand.nextInt(numWords)];
                if (n.length() > 1) {
                    result.add(n.toLowerCase());
                } else {
                    i--;
                }
            }
            return result;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
