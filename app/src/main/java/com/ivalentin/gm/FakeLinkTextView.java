//TODO: Delete this file

package com.ivalentin.gm;

import android.content.Context;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Extension of TextView to make some text look like links.
 * 
 * Although the click function s not implemented here, the text is given
 * the appearance of a link: blue and underlined.
 * 
 * @author Iñigo Valentin
 *
 * @see TextView
 */
public class FakeLinkTextView extends TextView{

	//Indicates if the text is being modified
	private boolean modifyingText = false;

	/**
     * Constructor.
     * 
     * @param context The context of the app or Activity.
     */
	public FakeLinkTextView(Context context){
		super(context);
		init();
	}

	/**
     * Constructor.
     * 
     * @param context The context of the app or Activity.
     * @param attrs Attributes.
     */
	public FakeLinkTextView(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}

	/**
     * Constructor.
     * 
     * @param context The context of the app or Activity.
     * @param attrs Attributes.
     * @param defStyle Default style.
     */
	public FakeLinkTextView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * Sets a TextWatcher for the FakeLinkTextView that sets it's color and calls 
	 * a function to underline it. 
	 */
	private void init(){
		addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after){ }

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count){ }

			@Override
			public void afterTextChanged(Editable s){
				if (modifyingText == false){
					underlineText();
					setTextColor(getResources().getColor(R.color.link));
				}
			}
		});
		underlineText();
	}

	/**
	 * Underlines the text.
	 */
	private void underlineText(){
		if (modifyingText == false){
			modifyingText = true;
			SpannableString content = new SpannableString(getText());
			content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
			setText(content);
			modifyingText = false;
		}
	}
	
}
