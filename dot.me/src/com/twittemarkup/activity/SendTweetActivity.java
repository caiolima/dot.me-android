package com.twittemarkup.activity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.twittemarkup.app.R;
import com.twittemarkup.assynctask.TwitterImageDownloadTask;
import com.twittemarkup.message.action.ISendAction;
import com.twittemarkup.model.Account;
import com.twittemarkup.model.Draft;
import com.twittemarkup.model.bd.Facade;

public class SendTweetActivity extends Activity {

	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;
	protected static final int GET_IMAGE_FROM_GALLERY = 1;
	private Button bt_send;
	private EditText txt_text;
	private ImageView img_profile, img_to_send;
	private LinearLayout lt_container;
	private TextView lbl_profile;
	private TextView lbl_type_acc;
	private static Uri imageURI;
	private String preText;
	private String form;
	private Bundle sendParams;
	private ISendAction action;
	private String typeText;
	private boolean isToSaveDraft=true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tweet);

		Intent intent = getIntent();
		if (intent != null) {
			Bundle b = intent.getExtras();
			if (b != null) {
				preText = b.getString("pre_text");
				form = b.getString("action");
				sendParams = b.getBundle("send_params");
				typeText=b.getString("type_message");

				Class<?> cls;
				try {
					cls = Class.forName("com.twittemarkup.message.action."
							+ form);

					Constructor<?> constructor = cls.getConstructor();
					action = (ISendAction) constructor
							.newInstance();
					
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		bt_send = (Button) findViewById(R.id.tweet_bt_send);
		txt_text = (EditText) findViewById(R.id.tweet_txt_text);
		img_profile = (ImageView) findViewById(R.id.message_send_img_profile);
		lbl_profile = (TextView) findViewById(R.id.send_message_lbl_current_user);
		lbl_type_acc = (TextView) findViewById(R.id.send_message_lbl_type_account);

		img_to_send = (ImageView) findViewById(R.id.tweet_image_to_send);
		lt_container = (LinearLayout) findViewById(R.id.tweet_lt_image_tosend);

		insertInfos();

		bt_send.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				/*
				 * Intent intent=new
				 * Intent(SendTweetActivity.this,FriendListActivity.class);
				 * 
				 * startActivity(intent);
				 */

				new SendTweetTask(SendTweetActivity.this,action,sendParams).execute();

			}
		});

		if (preText != null) {
			txt_text.setText(preText);
		}
		
		Draft savedDraft=Facade.getInstance(this).getOneDraft(action.getDraftId());
		if(savedDraft!=null){
			txt_text.setText(savedDraft.getText());
		}

	}

	/*
	 * @Override protected void onActivityResult(int requestCode, int
	 * resultCode, Intent data) { if (requestCode ==
	 * CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) { if (resultCode == RESULT_OK) { try
	 * { imageURI = data.getData(); } catch (NullPointerException e) {
	 * 
	 * } } else if (resultCode == RESULT_CANCELED) { imageURI = null; } else {
	 * imageURI = null;
	 * 
	 * } }else if(requestCode==GET_IMAGE_FROM_GALLERY){ if (resultCode ==
	 * RESULT_OK) { try { imageURI = data.getData(); Bitmap
	 * image=BitmapFactory.decodeFile(ImageUtils.getPath(imageURI, this));
	 * img_to_send.setImageBitmap(image);
	 * 
	 * lt_container.setVisibility(View.VISIBLE);
	 * //img_to_send.setVisibility(View.VISIBLE); } catch (NullPointerException
	 * e) {
	 * 
	 * } } else if (resultCode == RESULT_CANCELED) { imageURI = null; } else {
	 * imageURI = null;
	 * 
	 * } } }
	 */

	private void insertInfos() {
		Account a = action.getAccount(this);
		
		lbl_profile.setText(a.getName());
		lbl_type_acc.setText(typeText);

		TwitterImageDownloadTask.executeDownload(this, img_profile,
				a.getProfileImage());
		
		
		
	}

	private class SendTweetTask extends AsyncTask<Void, Void, Void> {

		private ProgressDialog progressDialog;
		private Activity parent;
		private ISendAction action;
		private Bundle sendParams;
		
		
		public SendTweetTask(Activity parent,ISendAction action,Bundle sendParams){
			this.parent=parent;
			this.action=action;
			this.sendParams=sendParams;
		}
		
		
		@Override
		protected void onPreExecute() {

			progressDialog = new ProgressDialog(SendTweetActivity.this);

			progressDialog.setMessage(getString(R.string.sending_tweet));

			try {
				progressDialog.show();
			} catch (Exception e) {
				// TODO: handle exception
			}

		}

		@Override
		protected Void doInBackground(Void... params) {
			String status = txt_text.getText().toString();
			
			action.execute(parent, status, sendParams);
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.cancel();
			parent.finish();
			Toast.makeText(parent, action.getResultMessage(parent), Toast.LENGTH_SHORT).show();
			isToSaveDraft=!action.messageSent();
		}

	}

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	/*
	/** Create a file Uri for saving an image or video 
	private static Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video 
	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"MyCameraApp");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}
	*/

	@Override
	protected void onPause() {
		super.onPause();
		
		if(isToSaveDraft&&!txt_text.getText().toString().equals("")){
			Draft d=new Draft();
			d.setId(action.getDraftId());
			d.setText(txt_text.getText().toString());
			
			Facade.getInstance(this).insert(d);
			
		}else if(Facade.getInstance(this).existsDraft(action.getDraftId())){
			Facade.getInstance(this).deleteDraft(action.getDraftId());
		}
			
	}

	
}
