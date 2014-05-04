package com.dieend.uvahunt;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.dieend.uvahunt.tools.Utility;

public class StartActivity extends Activity{
	private EditText usernameField;
	private SharedPreferences preference;
	private final OnClickListener loginButtonClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String username = usernameField.getText().toString();
			preference.edit()
				.putString("username", username)
				.commit();
			login(username);
		}
	};
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preference = getSharedPreferences(UvaHuntActivity.PREFERENCES_FILE, MODE_PRIVATE);
        String username = preference.getString("uid", null);
        if (username == null) {
        	setContentView(R.layout.login_layout);
        	usernameField = (EditText) findViewById(R.id.username_field);
        	Button loginButton = (Button) findViewById(R.id.login_button);
        	loginButton.setOnClickListener(loginButtonClick);
        } else {
        	finishLogin();
        }
    }
	private void finishLogin() {
		String username = preference.getString("username", null);
		String id = preference.getString("uid",null);
		if (username == null) {
			throw new RuntimeException("somehow no username");
		}
		if (id == null || Integer.parseInt(id)==0) {
			login(username);
			return;
		}
		Intent intent = new Intent(StartActivity.this, UvaHuntActivity.class);
    	intent.putExtra("username", username);
    	intent.putExtra("uid", id);
    	startActivity(intent);
    	finish();
	}
	private void login(String username) {
		setContentView(R.layout.loading_circle);
		new LoginTask().execute("http://uhunt.felix-halim.net/api/uname2uid/" + username);
	}
	private class LoginTask extends AsyncTask<String, Integer, String> {
		String url;
		
		@Override
		protected String doInBackground(String... params) {
			HttpClient client = new DefaultHttpClient();
			url = params[0];
		    HttpGet request = new HttpGet(url);
		    HttpResponse response;
		    String result = null;
		    try {
		        response = client.execute(request);         
		        HttpEntity entity = response.getEntity();

		        if (entity != null) {
		            InputStream instream = entity.getContent();
		            result = Utility.convertStreamToString(instream);
		            instream.close();
		        }
		        return result.trim();
		    } catch (Exception e1) {
		    	e1.printStackTrace();
		    	return null;
		    }
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result == null) {
				new LoginTask().execute(url);
			} else {
				preference.edit()
					.putString("uid", result)
					.commit();
				finishLogin();
			}
		}
		
	}
}
