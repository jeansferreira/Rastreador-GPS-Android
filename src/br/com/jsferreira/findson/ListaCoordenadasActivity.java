package br.com.jsferreira.findson;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

public class ListaCoordenadasActivity extends Activity implements Runnable {

	private SQLiteDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lista_coordenadas);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lista_coordenadas, menu);
		return true;
	}
	
	protected void onResume() {
		super.onRestart();
		
		//db = openOrCreateDatabase("Local.db",Context.MODE_PRIVATE, null);
		Intent it = getIntent();
		String celular = it.getStringExtra("fone");
		
		Toast.makeText(getBaseContext(), "Celular:"+celular, Toast.LENGTH_SHORT).show();
		
		Toast.makeText(getBaseContext(), "H", Toast.LENGTH_SHORT).show();
		db = openOrCreateDatabase("Coordenadas.db",Context.MODE_PRIVATE, null);
        Cursor curso = db.rawQuery("Select * from Coordenadas where celular = ?",new String[] { String.valueOf(celular) });
        Toast.makeText(getBaseContext(), "I", Toast.LENGTH_SHORT).show();

        //String[] from = {"celular"};
        //int[] to = {R.id.txtvCelular, R.id.txtCelular};
        //Toast.makeText(getBaseContext(), "j", Toast.LENGTH_SHORT).show();
        
        //if (curso.moveToFirst()){
        	//Toast.makeText(getBaseContext(), ">>:"+curso.getInt(0), Toast.LENGTH_SHORT).show();
			//Toast.makeText(getBaseContext(), "k", Toast.LENGTH_SHORT).show();
			//android.widget.SimpleCursorAdapter ad = new android.widget.SimpleCursorAdapter(getBaseContext(), R.layout.listar_model, curso, from, to);
			//Toast.makeText(getBaseContext(), "l", Toast.LENGTH_SHORT).show();
			//ListView ltwDados = (ListView)findViewById(R.id.listLocalizacao);
			//Toast.makeText(getBaseContext(), "m", Toast.LENGTH_SHORT).show();
			//ltwDados.setAdapter(ad);
			//Toast.makeText(getBaseContext(), "n", Toast.LENGTH_SHORT).show();				
       // }
        
		//Toast.makeText(getBaseContext(), "o", Toast.LENGTH_SHORT).show();
        /*
		EditText txtLog = (EditText) findViewById(R.id.editText1);
		while (curso.moveToNext()) {
	        s = "Dados da Localização: Celular[" + curso.getString(1);
	        //    	"] \nLongitude[" + curso.getString(2) + 
	        //    	"] \nLatitude[" + curso.getString(3) + 
	        //   	"] \nData/Hora[" + curso.getString(4) + "]";

			s = s+"\n-----------------------";
			txtLog.setText(s);
		}
       //db.close();
       */
		finish();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}	

}
