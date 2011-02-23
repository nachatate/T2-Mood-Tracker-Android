package com.t2.vas.activity;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.t2.vas.R;
import com.t2.vas.ScaleAdapter;
import com.t2.vas.db.tables.Group;
import com.t2.vas.db.tables.Result;
import com.t2.vas.db.tables.Scale;

public class RateActivity extends ABSNavigationActivity implements OnScrollListener {
	public static final String EXTRA_GROUP_ID = "group_id";
	
	private static final String TAG = RateActivity.class.getName();
	private long activeGroupId = 1;
	private ScaleAdapter scaleAdapter;
	private ListView listView;
	private boolean allItemsViewed = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.rate_activity);
        
        this.setRightButtonText(getString(R.string.save));
        
        Intent intent = this.getIntent();
        this.activeGroupId = intent.getLongExtra(EXTRA_GROUP_ID, -1);
        
        if(this.activeGroupId < 0) {
        	this.finish();
        }

        listView = (ListView)this.findViewById(R.id.list);

        Group group = new Group(dbAdapter);
        group._id = this.activeGroupId;
        if(!group.load()) {
        	this.finish();
        	return;
        }
        
        this.setTitle(group.title);

        scaleAdapter = new ScaleAdapter(this, R.layout.slider_overlay_widget, group.getScales());
        listView.setAdapter(scaleAdapter);
        listView.setOnScrollListener(this);

        // Restore some of the data
        if(savedInstanceState != null) {
        	// Restore the positions of the scales
	        int[] scaleValues = savedInstanceState.getIntArray("scaleValues");
	        if(scaleValues != null) {
	        	for(int i = 0; i < scaleValues.length; i++) {
	        		this.scaleAdapter.setProgressValueAt(i, scaleValues[i]);
	        	}
	        }

	        // Remember the scroll position
	        int listFirstVisible = savedInstanceState.getInt("listFirstVisible");
        	listView.setSelection(listFirstVisible);
        }
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Remember the values of the scales.
		int[] scaleValues = new int[this.scaleAdapter.getCount()];
		for(int i = 0; i < this.scaleAdapter.getCount(); i++) {
        	scaleValues[i] = this.scaleAdapter.getProgressValuesAt(i);
		}
		outState.putIntArray("scaleValues", scaleValues);

		// Remember the list's scroll position.
		outState.putInt("listFirstVisible", listView.getFirstVisiblePosition());

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRightButtonPresed() {
		Log.v(TAG, "LVP:"+listView.getLastVisiblePosition());
		if(!allItemsViewed || !isLastItemVisible()) {
			Toast.makeText(this, R.string.scroll_to_see_more_scales, Toast.LENGTH_LONG).show();
			return;
		}
		this.saveResults();
		this.setResult(Activity.RESULT_OK);
		this.finish();
	}
	
	private boolean isLastItemVisible() {
		return listView.getLastVisiblePosition() == listView.getCount()-1;
	}
	
	private void saveResults() {
		long currentTime = Calendar.getInstance().getTimeInMillis();
        for(int i = 0; i < this.scaleAdapter.getCount(); i++) {
        	Scale s = this.scaleAdapter.getItem(i);
        	int value = this.scaleAdapter.getProgressValuesAt(i);

        	Result r = new Result(this.dbAdapter);

			r.group_id = this.activeGroupId;
			r.scale_id = s._id;
			r.timestamp = currentTime;
			r.value = value;

			r.save();
        }
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if(!allItemsViewed) {
			allItemsViewed = isLastItemVisible();
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
	}
}