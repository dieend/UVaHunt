package com.dieend.uvahunt;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_about);
		TextView tv = (TextView)findViewById(R.id.version_text);
		String version_name = "1.0";
		try {
			version_name = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		tv.append(" " + version_name);
		
		// TODO change all text content to HTML format
		((TextView) findViewById(R.id.about_content2)).setMovementMethod(LinkMovementMethod.getInstance());
		((TextView) findViewById(R.id.about_content2)).setText(Html.fromHtml(getResources().getString(R.string.about_content2)));
		
		super.onCreate(savedInstanceState);
	}
	
}
