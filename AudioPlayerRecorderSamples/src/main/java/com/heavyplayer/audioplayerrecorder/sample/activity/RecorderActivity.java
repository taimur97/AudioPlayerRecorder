package com.heavyplayer.audioplayerrecorder.sample.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.heavyplayer.audioplayerrecorder.fragment.AudioRecorderFragment;
import com.heavyplayer.audioplayerrecorder.sample.R;
import com.heavyplayer.audioplayerrecorder.sample.obj.Item;
import com.heavyplayer.audioplayerrecorder.utils.AudioUtils;

import java.io.File;

public class RecorderActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {
	private static Item[] sItems = {
			new Item(0),
			new Item(1),
			new Item(2),
			new Item(3),
			new Item(4),
			new Item(5),
			new Item(6),
			new Item(7),
			new Item(8),
			new Item(9),
			new Item(11),
			new Item(12),
			new Item(13),
			new Item(14),
	};

	protected TextView mTitleView;
	protected ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

	    mTitleView = (TextView)findViewById(android.R.id.title);

	    mListView = (ListView)findViewById(android.R.id.list);
	    mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	    mListView.setAdapter(onCreateAdapter(this, sItems));
	    mListView.setOnItemClickListener(this);
	    // Select first item.
	    mListView.performItemClick(mListView.getChildAt(0), 0, mListView.getItemIdAtPosition(0));
    }

	protected ListAdapter onCreateAdapter(Context context, Item[] objects) {
		return new ItemAdapter(context, objects);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final String fileName = ((Item)parent.getItemAtPosition(position)).getFileName();
		mTitleView.setText(fileName);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	public void onRecord(View v) {
		if(!AudioUtils.isMicrophoneAvailable(this)) {
			Toast.makeText(this, R.string.error_microphone_not_available, Toast.LENGTH_LONG).show();
			return;
		}

		final String fileName = getSelectedItem().getFileName();
		if(fileName == null) {
			Toast.makeText(this, R.string.error_filename_invalid, Toast.LENGTH_LONG).show();
			return;
		}

		AudioRecorderFragment.newInstance(generateExternalStorageFileUri(fileName))
				.show(getSupportFragmentManager(), AudioRecorderFragment.TAG);
	}

	protected Uri generateExternalStorageFileUri(String fileName) {
		final File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName);
		return Uri.fromFile(file);
	}

	protected Item getSelectedItem() {
		return (Item)mListView.getItemAtPosition(mListView.getCheckedItemPosition());
	}

	protected class ItemAdapter extends ArrayAdapter<Item> {
		public ItemAdapter(Context context, Item[] objects) {
			super(context, R.layout.audio_player_list_item, R.id.item_title, objects);
		}
	}
}
