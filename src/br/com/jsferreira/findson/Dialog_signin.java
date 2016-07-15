package br.com.jsferreira.findson;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Dialog_signin extends Activity implements OnClickListener {

	  protected static final int DIALOG_1 = 0; // Dialog 1 ID
	  
	  // Dialog 1 default values (static variables to maintain the values on screen orientation change)
	  protected static String cdField2DefaultValue;
	   
	  // Interface elements
	  protected EditText cdField2DefValEdit;
	  protected Button showCDBtn;
	   
	  @Override
	  public void onCreate(Bundle savedInstanceState)
	  {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_dialog_signin);
	 
	    // Retrieve interface elements
	    cdField2DefValEdit = (EditText)findViewById(R.id.cdField2Edit);
	    showCDBtn = (Button)findViewById(R.id.cdOKBtn);
	 
	    // Add listeners
	    //showCDBtn.setOnClickListener(this);
	  }
	 
	  public void onClick(View v)
	  {
	    if (v == showCDBtn)
	    {
	      showDialog1(cdField2DefValEdit.getText().toString());
	    }
	  }
	   
	  @Override
	  protected Dialog onCreateDialog(int id)
	  {
	    switch (id)
	    {
	      case DIALOG_1:
	      {
	        final Dialog dialog = new Dialog(this);
	   
	        // Setup the custom dialog interface elements
	        dialog.setContentView(R.layout.activity_dialog_signin);
	        dialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
	   
	        Button cdOKBtn = (Button)dialog.findViewById(R.id.cdOKBtn);
	        Button cdCancelBtn = (Button)dialog.findViewById(R.id.cdCancelBtn);
	   
	        cdOKBtn.setOnClickListener(new OnClickListener()
	        {
	          // Called when the OK button is pressed
	          @Override
	          public void onClick(View v)
	          {
	            EditText cdField2Edit = (EditText)dialog.findViewById(R.id.cdField2Edit);
	   
	            String field2Value = cdField2Edit.getText().toString();
	   
	            // Call a method to process the dialog result
	            Dialog_signin.this.processDialog1Result(field2Value);
	   
	            // If we call removeDialog, then onCreateDialog will be called every time
	            // the dialog needs to be displayed, otherwise if we call dismissDialog,
	            // only onPrepareDialog will be called after the first dialog creation
	            // (the first showDialog).
	            removeDialog(DIALOG_1);
	            //dismissDialog(DIALOG_1);
	          }
	        });
	   
	        cdCancelBtn.setOnClickListener(new OnClickListener()
	        {
	          // Called when the Cancel button is pressed
	          @Override
	          public void onClick(View v)
	          {
	            removeDialog(DIALOG_1);               
	            //dismissDialog(DIALOG_1);
	          }
	        });
	   
	        dialog.setOnCancelListener(new OnCancelListener()
	        {
	          // Called when the Back button is pressed
	          @Override
	          public void onCancel(DialogInterface dialog)
	          {
	            removeDialog(DIALOG_1);               
	            //dismissDialog(DIALOG_1);
	          }
	        });
	   
	        return dialog;
	      }
	      default:
	        return null;
	    }
	  }
	 
	  @Override
	  protected void onPrepareDialog(int id, Dialog dialog)
	  {
	    switch (id)
	    {
	      case DIALOG_1:
	      {
	        dialog.setTitle("Custom dialog 1");
	   
	        EditText cdField2Edit = (EditText)dialog.findViewById(R.id.cdField2Edit);
	   
	        // Set the default values for Field 1 and 2
	        cdField2Edit.setText(cdField2DefaultValue != null ? cdField2DefaultValue : "");
	      }
	      break;
	    }
	  }
	 
	  protected void showDialog1(String field2DefaultValue)
	  {
	    // Set the default value for Field 2 (used in onPrepareDialog)
	    cdField2DefaultValue = field2DefaultValue;
	     
	    showDialog(DIALOG_1);
	  }
	   
	  protected void processDialog1Result(String field2Value)
	  {
	    Toast.makeText(this, "CD1 - field2Value: " + field2Value,
	      Toast.LENGTH_LONG).show();
	  }

}
