package com.androidmontreal.weddingvideoguestbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.androidmontreal.weddingvideoguestbook.db.DBItem;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GalleryImageAdapter extends BaseAdapter {
	public String TAG = PrivateConstants.TAG;
	private Context context;
	ArrayList<DBItem> galleryItems = new ArrayList<DBItem>();

	private DatumsDbAdapter mDbHelper;
	private Long rowId;

	public GalleryImageAdapter(Context context, ArrayList<DBItem> galleryItems) {
		this.context = context;
		this.galleryItems = galleryItems;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View gridView;

		if (convertView == null) {

			gridView = new View(context);
			gridView = inflater.inflate(R.layout.gallery_view_image, null);

			rowId = galleryItems.get(position).getId();
			mDbHelper = new DatumsDbAdapter(context);
			mDbHelper.open();
			Cursor note = mDbHelper.fetchNote(rowId);
			mDbHelper.close();
			if (note == null) {
				return null;
			}

			// Get goal for image label
			String tempGoal;
			String goal;
			String tempDate;
			String imageLabelText;

			try {
				tempGoal = note.getString(note
						.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD1));
				tempDate = note.getString(note
						.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD5));

				if (tempGoal.length() > 16) {
					goal = tempGoal.substring(0, 15).concat("...");
				} else {
					goal = tempGoal;
				}

				// TODO Format date
				imageLabelText = goal.concat("\n").concat(tempDate);

			} catch (Exception e) {
				return null;
			}

			String thumbnailPath = galleryItems.get(position)
					.getThumbnailImagePath().replace(".3gp", ".png");
			Bitmap roundedThumbnail = getRoundedCornerBitmap(thumbnailPath, 30);

			// Set value of textview
			TextView textView = (TextView) gridView
					.findViewById(R.id.grid_item_label);
			// Variables set based on device size
			boolean tabletSize = context.getResources().getBoolean(
					R.bool.isTablet);
			if (tabletSize) {
				textView.setTextSize(35);
			} else {
				textView.setTextSize(20);
			}
			textView.setShadowLayer(10f, 0, 0, Color.parseColor("#000000"));
			textView.setText(imageLabelText);

			// Set image
			ImageView imageView = (ImageView) gridView
					.findViewById(R.id.grid_item_image);

			if (roundedThumbnail != null) {
				imageView.setImageBitmap(roundedThumbnail);
			}
			/* No such method on android 4.0 */
			// imageView.setBackground(d);
			imageView.setTag(R.id.VIDEO_FILENAME_TAG_KEY,
					galleryItems.get(position).getFilename());
			imageView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent accessSession = new Intent(v.getContext(),
							SessionAccess.class);
					accessSession.putExtra("videoFilename",
							v.getTag(R.id.VIDEO_FILENAME_TAG_KEY).toString());
					accessSession.putExtra(DatumsDbAdapter.KEY_ROWID, rowId);
					context.startActivity(accessSession);
				}
			});

		} else {
			gridView = (View) convertView;
		}

		return gridView;
	}

	@Override
	public int getCount() {
		return galleryItems.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public static Bitmap getRoundedCornerBitmap(String thumbnailPath, int pixels) {
		if(thumbnailPath == null){
			Log.e(PrivateConstants.TAG, "thumbnailPath is null");
			return null;
		}
		if (new File(thumbnailPath).exists()) {
			return BitmapFactory.decodeFile(thumbnailPath);
		} else {
			// If it doesnt exist, create a rounded thumbnail for this video
			// and save it
			String videoPath = thumbnailPath.replace(".png", ".3gp");

			Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoPath,
					MediaStore.Images.Thumbnails.MINI_KIND);
			
			if(thumbnail == null){
				Log.e(PrivateConstants.TAG, "thumbnail is null");
				return null;
			}
			
			Bitmap roundedThumbnail = Bitmap.createBitmap(thumbnail.getWidth(),
					thumbnail.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(roundedThumbnail);

			final int color = 0xff424242;
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, thumbnail.getWidth(),
					thumbnail.getHeight());
			final RectF rectF = new RectF(rect);
			final float roundPx = pixels;

			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(color);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
			canvas.drawBitmap(thumbnail, rect, rect, paint);

			File file = new File(thumbnailPath);
			try {
				FileOutputStream fOut = new FileOutputStream(file);
				roundedThumbnail.compress(Bitmap.CompressFormat.PNG, 85, fOut);
				fOut.flush();
				fOut.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// return the rounded thumbnail regardless of whether we could save
			// it.
			return roundedThumbnail;
		}
	}
}
