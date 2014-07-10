package org.geekosphere.sharewithwoot;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class SharePreferences extends PreferenceActivity implements OnPreferenceClickListener {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference shareFromClipboard = findPreference("KEY_PASTE");
        shareFromClipboard.setOnPreferenceClickListener(this);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onPreferenceClick(Preference p) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);

            String url = item.getText().toString();
            if (url.startsWith("http")) {
                shareIntent(url);
            }
        }
        else {
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

            String url = clipboard.getText().toString();
            if (url.startsWith("http")) {
                shareIntent(url);
            }
        }

        return false;
    }

    private void shareIntent(String url) {
        Intent sendIntent = new Intent(this, ShareActivity.class);
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }
}
