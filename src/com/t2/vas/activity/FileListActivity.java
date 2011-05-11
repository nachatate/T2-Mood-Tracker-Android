package com.t2.vas.activity;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.t2.vas.R;

public class FileListActivity extends ABSNavigationActivity implements OnItemClickListener, OnItemLongClickListener {
	public static final String EXTRA_BASE_DIR = "baseDir";
	public static final String EXTRA_SELECTED_FILE = "selectedFile";
	
	private static final int FILE_SELECTOR_ACTIVITY = 345;
	private File selectedFile;
	private SimpleAdapter fileListAdapter;
	private File srcDir;
	ArrayList<HashMap<String,Object>> fileListItems = new ArrayList<HashMap<String,Object>>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.list_layout);
		
		ListView listView = (ListView)this.findViewById(R.id.list);
		listView.setEmptyView(this.findViewById(R.id.emptyListTextView));
		
		((TextView)this.findViewById(R.id.emptyListTextView)).setText(R.string.no_csv_files_found);
		
		srcDir = (File)getIntent().getSerializableExtra(EXTRA_BASE_DIR);
		if(srcDir == null || !srcDir.exists()) {
			this.finish();
			return;
		}
		
		buildFileListItems();
		fileListAdapter = new SimpleAdapter(
				this, 
				fileListItems, 
				R.layout.list_item_1, 
				new String[] {
						"title",
				}, 
				new int[] {
						R.id.text1,
				}
		);
		
		listView.setAdapter(fileListAdapter);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
	}
	
	private void buildFileListItems() {
		fileListItems.clear();
		
		File[] files = srcDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().toLowerCase().endsWith(".csv");
			}
		});
		if(files == null || files.length == 0) {
			return;
		}
		
		HashMap<String,Object> item;
		File file;
		String fileName;
		for(int i = 0; i < files.length; ++i) {
			file = files[i];
			fileName = file.getName();
			if(file.isDirectory()) {
				fileName += "/";
			}
			
			item = new HashMap<String,Object>();
			item.put("title", fileName);
			item.put("file", file);
			
			fileListItems.add(item);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		@SuppressWarnings("unchecked")
		HashMap<String,Object> item = (HashMap<String,Object>)arg0.getItemAtPosition(arg2);
		File selectedFile = (File)item.get("file");
		
		if(selectedFile.isDirectory()) {
			Intent intent = new Intent(this, FileListActivity.class);
			intent.putExtra(EXTRA_BASE_DIR, selectedFile);
			intent.putExtra(FileListActivity.EXTRA_BACK_BUTTON_TEXT, getString(R.string.back_button));
			this.startActivityForResult(intent, FILE_SELECTOR_ACTIVITY);
			
		} else {
			Intent intent = new Intent();
			intent.putExtra(EXTRA_SELECTED_FILE, selectedFile);
			this.setResult(RESULT_OK, intent);
			this.finish();
		}
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		@SuppressWarnings("unchecked")
		HashMap<String,Object> item = (HashMap<String,Object>)arg0.getItemAtPosition(arg2);
		File selectedFile = (File)item.get("file");
		
		if(selectedFile.isFile()) {
			this.selectedFile = selectedFile;
			showFileOptionsDialog();
		}
		
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == RESULT_OK) {
			this.setResult(RESULT_OK, data);
			this.finish();
			return;
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void showFileOptionsDialog() {
		new AlertDialog.Builder(this)
			.setTitle(R.string.file_options_title)
			.setNegativeButton(R.string.close, null)
			.setItems(new String[]{
					getString(R.string.delete_selected_file)
				}, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch(which) {
							case 0:
								showFileDeleteDialog();
								break;
						}
						dialog.dismiss();
					}
				}
			)
			.create()
			.show();
	}
	
	private void showFileDeleteDialog() {
		new AlertDialog.Builder(this)
			.setTitle(R.string.delete_file_title)
			.setMessage(R.string.delete_file_desc)
			.setPositiveButton(R.string.delete_file_yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					selectedFile.delete();
					dialog.dismiss();
					
					buildFileListItems();
					fileListAdapter.notifyDataSetChanged();
				}
			})
			.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.create()
			.show();
	}
}
