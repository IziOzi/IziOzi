package it.smdevelopment.iziozi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

public class SpeakableImageButton extends ImageButton {

	private String mSentence = null;
	private Context mContext;
	
	public SpeakableImageButton(Context ctx) {
		super(ctx);
		mContext = ctx;
	}
	
	public void setSentence(String sentence) {
		mSentence = sentence;
	}
	
	public String getSentence() {
		return mSentence;
	}
	
	public void showInsertDialog() {
		Log.d("home debug",	"options selected");
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View layoutView = inflater.inflate(R.layout.spkbtn_input_layout, null);
		
		final EditText txt = (EditText) layoutView.findViewById(R.id.textField);
		
		builder.setTitle("Text to speech")
		.setView(layoutView)
		.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d("dialog", "should dismiss and apply ");
				
				setSentence(txt.getText().toString());
			}
		})
		.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d("dialog", "should dismiss and discard");
			}
		});
		
		
		builder.create().show();
	}
	
}
