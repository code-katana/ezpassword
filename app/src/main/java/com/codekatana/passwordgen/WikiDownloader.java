package com.codekatana.passwordgen;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sixface on 3/3/18.
 */

public class WikiDownloader extends AsyncTask<Integer, Integer, String> {

    private static final String WIKI_URL = "https://en.wikipedia.org/w/api.php";
    private static final String APPLICATION_JSON = "application/json";
    private static final String EZ_PASS = "EzPass";

    @Override
    protected String doInBackground(Integer... integers) {
        OkHttpClient client = new OkHttpClient();
        int count = integers[0];
        String article = "", out = "";

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
            StringBuilder sb = new StringBuilder();
//            Log.d(EZ_PASS, Arrays.asList(words).toString());
            int numWords = words.length;
            for (int i = 0; i < count; i++) {
                String n = words[rand.nextInt(numWords)];
                String str = processWord(n);
                if (str.length() > 1) {
                    sb.append(str);
                } else {
                    i--;
                }
            }
            out = sb.toString();
            out = out.replaceFirst("a", "@");
            out = out.replaceFirst("o", "0");
            out = out.replaceFirst("s", "$");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return out;
    }

    private String processWord(String input) {
        input = input.trim();
        String firstLetter = input.substring(0, 1);
        if (input.length() > 1) {
            return firstLetter.toUpperCase() + input.substring(1);
        } else
            return firstLetter.toUpperCase();
    }
}
