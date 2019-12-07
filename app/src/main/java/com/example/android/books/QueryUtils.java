package com.example.android.books;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

final class QueryUtils {

	private static final String LOG_TAG = QueryUtils.class.getSimpleName();

	private QueryUtils() {
	}

	static List<Kitaab> fetchBooks(String requestUrl) {
		URL url = createUrl(requestUrl);
	String jsonResponse = "";

		try {
			jsonResponse = makeHttpRequest(url);
		} catch (IOException e) {
			Log.e(LOG_TAG, "Problem making the HTTP request for the search criteria");
		}
		return QueryUtils.extractFeatures(jsonResponse);
	}

	private static URL createUrl(String stringUrl) {
		URL url = null;

		try {
			url = new URL(stringUrl);
		} catch (MalformedURLException e) {
			Log.e(LOG_TAG, "Problem building the url!");
		}
		return url;
	}

	private static List<Kitaab> extractFeatures(String kitaabJSON) {
		if (TextUtils.isEmpty(kitaabJSON)) {
			return null;
		}
		List<Kitaab> allBooks = new ArrayList<>();

		try {
			JSONObject rawJSONResponse = new JSONObject(kitaabJSON);

			JSONArray kitaabs = rawJSONResponse.getJSONArray("items");
			for (int i = 0; i < kitaabs.length(); i++) {
				JSONObject kitaab = kitaabs.getJSONObject(i);
				JSONObject volume = kitaab.getJSONObject("volumeInfo");
				String bookTitle = volume.getString("title");
				StringBuilder authors = new StringBuilder();
				if (volume.has("authors")) {
					JSONArray kitaabAuthorsJSON = volume.getJSONArray("authors");
					int numberOfAuthors = kitaabAuthorsJSON.length();
					int maxAuthors = 3;
					String cAuthors = "";
					String[] allAuthors =  null;

					int numberOfLetters = kitaabAuthorsJSON.get(0).toString().length();
					if (numberOfLetters > 40) {
						cAuthors = kitaabAuthorsJSON.toString().substring(2, numberOfLetters - 1);
						allAuthors = cAuthors.split("[;,]");
						for (int j = 0; j < allAuthors.length && j < maxAuthors; j++) {
							authors.append(allAuthors[j].trim()).append("\n");
						}

					} else {
						for (int j = 0; j < numberOfAuthors && j < maxAuthors; j++) {
							authors.append(kitaabAuthorsJSON.getString(j)).append("\n");
						}
					}
				}


				float kitaabRating = 0f;
				if (volume.has("averageRating")) {
					kitaabRating = (float) volume.getDouble("averageRating");
				}

				JSONObject saleInfo = kitaab.getJSONObject("saleInfo");
				String saleability = saleInfo.getString("saleability");
				boolean isSold = saleability.equals("FOR_SALE");
				float kitaabPrice = 0f;
				if (isSold) {
					JSONObject priceInfo = saleInfo.getJSONObject("retailPrice");
					kitaabPrice = (float) priceInfo.getDouble("amount");
				}

				allBooks.add(new Kitaab(bookTitle, authors.toString(), kitaabRating, kitaabPrice));
			}

		} catch (JSONException e) {
			Log.e(LOG_TAG, "Problem parsing the google books JSON results", e);
		}

		return allBooks;
	}

	private static String makeHttpRequest(URL url) throws IOException {
		String jsonResponse = "";

		if (url == null) {
			return jsonResponse;
		}

		HttpURLConnection urlConnection = null;

		InputStream inputStream = null;

		try {
			urlConnection = (HttpURLConnection) url.openConnection();

			urlConnection.setRequestMethod("GET");
			urlConnection.setReadTimeout(10000);
			urlConnection.setConnectTimeout(15000);
			urlConnection.connect();

			if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				inputStream = urlConnection.getInputStream();
				jsonResponse = readFromStream(inputStream);
			} else {
				Log.e(LOG_TAG, "Error while connecting. Error Code: " + urlConnection.getResponseCode());
			}
		} catch (IOException e) {
			e.getMessage();
			Log.e(LOG_TAG, "Problem encountered while retrieving book results");
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			if (inputStream != null) {
				inputStream.close();
			}
		}

		return jsonResponse;
	}

	private static String readFromStream(InputStream inputStream) throws IOException {
		StringBuilder output = new StringBuilder();
		if (inputStream != null) {
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));

			BufferedReader reader = new BufferedReader(inputStreamReader);

			String line = reader.readLine();

			while (line != null) {
				output.append(line);
				line = reader.readLine();
			}
		}
		return output.toString();
	}
}
