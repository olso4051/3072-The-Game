package com.olyware.game3072.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.olyware.game3072.R;

public class PostHighScore extends AsyncTask<String, Integer, Integer> {
	private final String BASE_URL = "http://deeldat.com/score";
	private String globalHighScore, secondPlaceScore, name, secondPlaceName, place, success, error;
	private String passwordKey, password;

	public PostHighScore(Context ctx) {
		passwordKey = ctx.getString(R.string.password_key);
		password = ctx.getString(R.string.password);
	}

	public String getSuccess() {
		if (success != null)
			return success;
		else
			return "";
	}

	public String getGlobalHighScore() {
		if (globalHighScore != null)
			return globalHighScore;
		else
			return "";
	}

	public String getSecondPlaceScore() {
		if (secondPlaceScore != null)
			return secondPlaceScore;
		else
			return "";
	}

	public String getName() {
		if (name != null)
			return name;
		else
			return "";
	}

	public String getSecondPlaceName() {
		if (secondPlaceName != null)
			return secondPlaceName;
		else
			return "";
	}

	public String getPlace() {
		if (place != null)
			if (place.equals("1"))
				return "1st";
			else if (place.equals("2"))
				return "2nd";
			else if (place.equals("3"))
				return "3rd";
			else
				return place;
		else
			return "";
	}

	public String getError() {
		if (error != null)
			return error;
		else
			return "";
	}

	@Override
	protected Integer doInBackground(String... s) {
		// POST to API with old and new registration, also referral's registration
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);

		HttpPut httpput = new HttpPut(BASE_URL);
		HttpEntity entity;
		String fullResult;
		JSONObject jsonResponse;
		try {
			JSONObject data = new JSONObject();
			if (s.length > 0) {
				if (s[0].length() > 0) {
					data.put("high_score", s[0]);
				}
			}
			if (s.length > 1) {
				if (s[1].length() > 0) {
					data.put("type", s[1]);
				}
			}
			if (s.length > 2) {
				if (s[2].length() > 0) {
					data.put("name", s[2]);
				}
			}
			data.put(passwordKey, password);
			Log.d("test", "JSON to score: " + data.toString());
			httpput.setEntity(new StringEntity(data.toString()));
			httpput.setHeader("Content-Type", "application/json");
			HttpResponse response = httpclient.execute(httpput);
			entity = response.getEntity();
			fullResult = EntityUtils.toString(entity);
			Log.d("test", fullResult);
			jsonResponse = new JSONObject(fullResult);
		} catch (JSONException j) {
			j.printStackTrace();
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		if (entity != null && fullResult != null && jsonResponse != null) {
			success = getStringFromJSON(jsonResponse, "success");
			error = getStringFromJSON(jsonResponse, "error");
			globalHighScore = getStringFromJSON(jsonResponse, "global_high_score");
			secondPlaceScore = getStringFromJSON(jsonResponse, "second_place_score");
			name = getStringFromJSON(jsonResponse, "name");
			secondPlaceName = getStringFromJSON(jsonResponse, "second_place_name");
			place = getStringFromJSON(jsonResponse, "place");
			if (name.equals(""))
				return 1;
			else
				return 0;
		} else {
			return 1;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		// override in calling class
		// result == 0 success
	}

	private String getStringFromJSON(JSONObject json, String key) {
		try {
			return json.getString(key);
		} catch (JSONException e) {
			return "";
		}
	}

	private boolean getBooleanFromJSON(JSONObject json, String key) {
		try {
			return json.getBoolean(key);
		} catch (JSONException e) {
			return false;
		}
	}
}
