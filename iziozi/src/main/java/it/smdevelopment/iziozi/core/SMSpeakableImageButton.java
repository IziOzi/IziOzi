package it.smdevelopment.iziozi.core;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import it.smdevelopment.iziozi.R;
@Root(name = "SMSpeakableImageButton")
public class SMSpeakableImageButton extends ImageButton {

    @Element(required = false)
	private String mSentence = "";

	private Context mContext;
	
	public SMSpeakableImageButton(Context ctx) {
		super(ctx);
		mContext = ctx;
	}

    public SMSpeakableImageButton(@Element(name = "mSentence") String sentence){
        super(SMIziOziApplication.CONTEXT);
        this.mSentence = sentence;
    }

    public SMSpeakableImageButton(){
        super(SMIziOziApplication.CONTEXT);
    }

    public String getmSentence() {
        return mSentence;
    }

    public void setmSentence(String mSentence) {
        this.mSentence = mSentence;
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
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
        txt.setText(this.mSentence);
		
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
