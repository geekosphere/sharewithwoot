package org.geekosphere.sharewithwoot;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class ShareActivity extends Activity {

	private static final String TAG = "ShareActivity";

	private final static String HAL_URL = "http://hal.apoc.cc/dispatch";
	private final static String HAL_USERNAME = "sharewithwoot";
	private final static String HAL_PASSWORD = "ROFGNIKOOLERAUOYSDIORDEHTTON";
	private final static String HAL_CHANNEL = "#woot";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		final String url;
		final String title;
		if (savedInstanceState == null && intent != null) {
			if (intent.getAction().equals(Intent.ACTION_SEND)) {
				url = intent.getStringExtra(Intent.EXTRA_TEXT);
				// might be empty
				title = intent.getStringExtra(Intent.EXTRA_SUBJECT);
			} else {
				return;
			}
		} else {
			return;
		}

		setContentView(R.layout.share);
		final Button button = (Button) findViewById(R.id.buttonShare);
		final EditText customText = (EditText) findViewById(R.id.editTitle);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.v(TAG, "share button clicked!");
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						shareWithWoot(url, title, customText.getText()
								.toString());
					}
				});
				thread.start();
				finish();
			}
		});
	}

	private void shareWithWoot(String url, String title, String customText) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String name = preferences.getString("KEY_NAME", "changeme");

		if (title != null && !title.equals(""))
			customText += String.format(" (%s)", title);

		final String message;
		if (dispatchToHal(announceCommand(HAL_CHANNEL, name, url, customText))) {
			message = "Successfully shared with " + HAL_CHANNEL + "!";
		} else {
			message = "An error occured, try again later!";
		}
		final Context context = this;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();
			}
		});
	}

	private String announceCommand(String channel, String name, String url,
			String customText) {
		return String.format("say %s \002%s just shared:\017 %s (%s)", channel,
				name, url, customText);
	}

	private boolean dispatchToHal(String command) {
		HttpClient httpclient = new DefaultHttpClient();

		// Prepare a request object
		HttpPost httppost = new HttpPost(HAL_URL);

		List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters.add(new BasicNameValuePair("username", HAL_USERNAME));
		postParameters.add(new BasicNameValuePair("password", HAL_PASSWORD));
		postParameters.add(new BasicNameValuePair("command", command));

		try {
			httppost.setEntity(new UrlEncodedFormEntity(postParameters));
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG,
					"dispatchToHal: UnsupportedEncodingException: "
							+ e.getMessage());
			return false;
		}

		// Execute the request
		HttpResponse response;
		try {
			response = httpclient.execute(httppost);
			Log.v(TAG, "dispatchToHal: Response Status: "
					+ response.getStatusLine().toString());
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				System.out.println(entity.getContent().toString());
			}
		} catch (Exception e) {
			Log.e(TAG, "dispatchToHal: Exception: " + e.getMessage());
			return false;
		}

		return true;
	}

}
