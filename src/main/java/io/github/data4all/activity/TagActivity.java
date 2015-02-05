package io.github.data4all.activity;

import io.github.data4all.R;
import io.github.data4all.logger.Log;
import io.github.data4all.model.data.ClassifiedTag;
import io.github.data4all.model.data.Tag;
import io.github.data4all.model.data.OsmElement;
import io.github.data4all.model.data.Tags;
import io.github.data4all.util.SpeechRecognition;
import io.github.data4all.util.Tagging;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 
 * @author Maurice Boyke
 *
 */
public class TagActivity extends BasicActivity implements OnClickListener {

	//OSMElement Key
	protected static final String OSM = "OSM_ELEMENT";
    private static final int REQUEST_CODE = 1234;
    final Context context = this;
    private String key;
    private Map<String, String> map;
    private List <EditText> edit;
    private Boolean first;
    private Dialog dialog1;
    private CharSequence [] array;
    private AlertDialog alert;
    private AlertDialog alert1;
    private Map<String, ClassifiedTag> tagMap;

    /**
     * Called when the activity is first created.
     * 
     * @param savedInstanceState
     *            If the activity is being re-initialized after previously being
     *            shut down then this Bundle contains the data it most recently
     *            supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it
     *            is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_tag);    
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        createAlertDialogKey();
        

        

    }
    
    private void createAlertDialogKey(){
    	
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(TagActivity.this,android.R.style.Theme_Holo_Dialog_MinWidth);
        LayoutInflater inflater = getLayoutInflater();
        View view=inflater.inflate(R.drawable.header_listview, null);
        ((TextView) view.findViewById(R.id.titleDialog)).setText("Select Tag");;        
        alertDialog.setCustomTitle(view);
        ImageButton speechStart = (ImageButton) view.findViewById(R.id.speech); 
        speechStart.setOnClickListener(this);
        if(getIntent().hasExtra("TYPE_DEF")){
        	array = Tagging.getArrayKeys( getIntent().getExtras().getInt("TYPE_DEF"));
        	tagMap = Tagging.getMapKeys( getIntent().getExtras().getInt("TYPE_DEF"));
        }
        
        alertDialog.setItems(array, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	key = (String) array [which];
            	createAlertDialogValue();
            }
            
        });
        alert = alertDialog.create();
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alert.setOnKeyListener(new OnKeyListener() {
			
    			@Override
    			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
    				if(keyCode == KeyEvent.KEYCODE_BACK){
    					    					
    				}
    				return true;
    			}
    		});
        alert.show();
        

        
    }
    
    
    private void createAlertDialogValue(){
    	array =  tagMap.get(key).getClassifiedValues().toArray(new String [tagMap.get(key).getClassifiedValues().size()]);
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TagActivity.this);
    	alertDialogBuilder.setTitle("Select Tag");
    	alertDialogBuilder.setItems(array, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String value = (String) array [which];
                map = new LinkedHashMap<String, String>();
                map.put(key, value);
                if (key.equals("building")
                        || key.equals("amenity")) {                                  
                    createDialog(Tags.getAllAddressTags(), "Add Address", key.equals("building"), true);
                }
                else{
                 finish();
                }
			}
		});
    	
         alert1 = alertDialogBuilder.create();
         alert1.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
         alert1.setOnKeyListener(new OnKeyListener() {
 			
     			@Override
     			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
     				if(keyCode == KeyEvent.KEYCODE_BACK){
     					 alert1.dismiss();
     					 createAlertDialogKey();
     				}
     				return true;
     			}
     		});
         alert1.show();

    }

    public void onClick(View v) {
    	switch (v.getId()){
  	case R.id.speech:
    		Intent intent = new Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            startActivityForResult(intent, REQUEST_CODE);
            alert.dismiss();
            break;
    	case R.id.buttonNext:
    		List<String> tags = new ArrayList<String>();
			
			for (int i = 0; i < edit.size(); i++) {
				tags.add(edit.get(i).getText().toString());
			}
			map = Tagging.addressToTag(tags, map);
			dialog1.dismiss();
			createDialog(Tags.getAllContactTags(), "Add Contacts", true, false);
			
			break;
    	case R.id.buttonFinish:	
    		List<String> tags1 = new ArrayList<String>();
			
			for (int i = 0; i < edit.size(); i++) {
				tags1.add(edit.get(i).getText().toString());
			}
			if(first){
				map = Tagging.addressToTag(tags1, map);
			}
			else{
				map = Tagging.contactToTag(tags1, map);
			}
			dialog1.dismiss();
			finish();
			break;
    	}
    }
    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
        	new Dialog(TagActivity.this);
            ListView textList = (ListView) findViewById(R.id.listView1);
            List<String> matchesText = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            SpeechRecognition.splitStrings(matchesText);
            Map<String, String> map = SpeechRecognition
                    .speechToTag(matchesText);
            matchesText.clear();
            for (Entry entry : map.entrySet()) {
    			final String key = (String) entry.getKey();
    			matchesText.add(key + "=" + map.get(key));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, matchesText);
            textList.setAdapter(adapter);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    
    /**
     * Creates the Dialog of the Address and Contact tags
     * 
     * @param arrayList List of Tags
     * @param title The title of the Dialog
     * @param but False to add the contacts 
     * @param first1 true if the Method is called the first time 
     */


	public void createDialog(ArrayList<Tag> arrayList, String title, final Boolean but, final Boolean first1){
    	dialog1 = new Dialog(this,android.R.style.Theme_Holo_Dialog_MinWidth);
		dialog1.setContentView(R.layout.dialog_dynamic);
		dialog1.setTitle(title);
		LinearLayout layout = (LinearLayout) dialog1.findViewById(R.id.dialogDynamic);
		final Button next = new Button(this);
		final Button finish = new Button(this);
		next.setText(R.string.next);
		finish.setText(R.string.finish);
		next.setId(R.id.buttonNext);
		finish.setId(R.id.buttonFinish);
		first = first1;
		edit = new ArrayList<EditText>();
		for (int i = 0; i < arrayList.size(); i++) {
		final EditText text = new EditText(this);
			text.setHint(arrayList.get(i).getHintRessource());
			text.setHintTextColor(Color.DKGRAY);
    		text.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    		edit.add(text);
    		layout.addView(text);
		}
		finish.setOnClickListener(this);
		next.setOnClickListener(this);
		if(!but){
		layout.addView(next);
		}
		layout.addView(finish);
        dialog1.setOnKeyListener(new OnKeyListener() {
			
   			@Override
   			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
   				if(keyCode == KeyEvent.KEYCODE_BACK){
   					if(!but || first){
   						dialog1.dismiss();
   						createAlertDialogValue();
   					}else{
   						dialog1.dismiss();
   						createDialog(Tags.getAllAddressTags(), "Add Address", key.equals("building"), true);
   					}
   					
   				}
   				return true;
   			}
   		});
		dialog1.show();
	}
	
	

	@Override
	public void finish() {
	  final OsmElement element = getIntent().getParcelableExtra(OSM);
	  element.addTags(map);
	  final Intent intent = new Intent(this, ResultViewActivity.class);
	  intent.putExtra(OSM, element);
	  intent.putExtra("TYPE_DEF", getIntent().getExtras().getInt("TYPE_DEF"));		
	  super.finish();
	  startActivity(intent);
	}
}