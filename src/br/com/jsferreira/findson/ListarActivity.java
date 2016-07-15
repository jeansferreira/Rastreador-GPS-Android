package br.com.jsferreira.findson;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class ListarActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listar);
	}

	public void onResume() {
		// TODO: Dá uma refresh na tela depois de atualizada.
		super.onResume();

		SQLiteDatabase db = openOrCreateDatabase("Coordenadas.db", Context.MODE_PRIVATE, null);

		Cursor curso = db.rawQuery("Select * from Coordenadas", null);
		if (curso.moveToFirst()) {
			String[] from = { "myDate", "la", "lo", "address" };
			int[] to = { R.id.txtvData1, R.id.txtvLa1, R.id.txtvLo1, R.id.txtvCelular1 };

			android.widget.SimpleCursorAdapter ad = new android.widget.SimpleCursorAdapter(getBaseContext(), R.layout.listar_model, curso,
					from, to);

			ListView ltwDados = (ListView) findViewById(R.id.ltwDados);
			ltwDados.setAdapter(ad);

			ltwDados.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				public void onItemClick(AdapterView adapter, View view, int position, long id) {

					SQLiteCursor c = (SQLiteCursor) adapter.getAdapter().getItem(position);

					// Intent it = new Intent(getBaseContext(),Editar.class);
					// it.putExtra("id", c.getInt(0) );
					// startActivity(it);
				};

			});
			db.close();
		} else {
			Toast.makeText(getBaseContext(), "Aguarde! O sistema ainda está em processamento a conexão GPS!", Toast.LENGTH_LONG).show();
		}
	}	
}
