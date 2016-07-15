package br.com.jsferreira.findson;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class CadastroActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cadastro);
		// TODO: Faz a carga dos dados na tela.
		onLoad();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.cadastro, menu);
		return true;
	}

	public void onLoad() {

		SQLiteDatabase db = openOrCreateDatabase("Configuracao.db", Context.MODE_PRIVATE, null);
		Cursor curso = db.rawQuery("Select celular,email,senha,sms,mail,timeRequet,timeSend,celularDestino from Configuracao", null);

		EditText txtfoneOrigem;
		EditText txtfoneDestino;
		EditText txtEmail;
		EditText txtSenha;
		EditText txtIntervalo;
		EditText txtNotificacao;
		RadioButton rb1;
		RadioButton rb2;
		Button btInserir;

		// Toast.makeText(CadastroActivity.this, "Antes do Cursor", Toast.LENGTH_SHORT).show();
		if (curso.moveToFirst()) {
			txtfoneOrigem = (EditText) findViewById(R.id.edtFoneOrigem);
			txtfoneDestino = (EditText) findViewById(R.id.edtFoneDestino);
			txtEmail = (EditText) findViewById(R.id.edtEmail);
			txtSenha = (EditText) findViewById(R.id.edtSenha);
			txtIntervalo = (EditText) findViewById(R.id.edtIntervalo);
			txtNotificacao = (EditText) findViewById(R.id.edtTempo);

			btInserir = (Button) findViewById(R.id.bttIniciarRastreamento);
			btInserir.setEnabled(false);

			rb1 = (RadioButton) findViewById(R.id.rdSms);
			rb2 = (RadioButton) findViewById(R.id.rdEmail);

			// Toast.makeText(getBaseContext(), "CELULAR:" + curso.getString(0), Toast.LENGTH_LONG).show();
			// Toast.makeText(getBaseContext(), "EMAIL:" + curso.getString(1), Toast.LENGTH_LONG).show();
			// Toast.makeText(getBaseContext(), "SENHA:" + curso.getString(2), Toast.LENGTH_LONG).show();
			// Toast.makeText(getBaseContext(), "SMS:" + curso.getString(3), Toast.LENGTH_LONG).show();
			// Toast.makeText(getBaseContext(), "MAIL:" + curso.getString(4), Toast.LENGTH_LONG).show();

			txtfoneOrigem.setText(curso.getString(0));
			txtEmail.setText(curso.getString(1));
			txtSenha.setText(curso.getString(2));
			txtIntervalo.setText(curso.getString(5));
			txtNotificacao.setText(curso.getString(6));
			txtfoneDestino.setText(curso.getString(7));

			int sms = curso.getInt(3);
			int mail = curso.getInt(4);

			if (sms == 0) {
				rb1.setChecked(true);
				rb2.setChecked(false);
			}
			if (mail == 0) {
				rb1.setChecked(false);
				rb2.setChecked(true);
			}

		} else {
			btInserir = (Button) findViewById(R.id.bttIniciarRastreamento);
			btInserir.setEnabled(true);
		}
	}

	public void onAlter(View v) {

		try {
			SQLiteDatabase db = openOrCreateDatabase("Configuracao.db", Context.MODE_PRIVATE, null);

			EditText txtFoneOrigem = (EditText) findViewById(R.id.edtFoneOrigem);
			EditText txtFoneDestino = (EditText) findViewById(R.id.edtFoneDestino);
			EditText txtEmail = (EditText) findViewById(R.id.edtEmail);
			EditText txtSenha = (EditText) findViewById(R.id.edtSenha);
			EditText txtIntervalo = (EditText) findViewById(R.id.edtIntervalo);
			EditText txtNotificacao = (EditText) findViewById(R.id.edtTempo);
			RadioButton rb1 = (RadioButton) findViewById(R.id.rdSms);
			RadioButton rb2 = (RadioButton) findViewById(R.id.rdEmail);

			String opcao1 = "1";
			String opcao2 = "1";

			if (rb2.isChecked()) {
				opcao2 = "0";
			}
			if (rb1.isChecked()) {
				opcao1 = "0";
			}
			// Toast.makeText(getBaseContext(), "SMS:" + opcao1, Toast.LENGTH_SHORT).show();
			// Toast.makeText(getBaseContext(), "MAIL:" + opcao2, Toast.LENGTH_SHORT).show();

			// Toast.makeText(getBaseContext(), "CELULAR:" + txtFone.getText().toString(), Toast.LENGTH_SHORT).show();
			// Toast.makeText(getBaseContext(), "EMAIL:" + txtEmail.getText().toString(), Toast.LENGTH_SHORT).show();
			// Toast.makeText(getBaseContext(), "SENHA:" + txtSenha.getText().toString(), Toast.LENGTH_SHORT).show();
			// Toast.makeText(getBaseContext(), "INTERVALO:" + txtIntervalo.getText(), Toast.LENGTH_SHORT).show();

			if (txtFoneOrigem.getText().toString().length() < 10) {
				Toast.makeText(CadastroActivity.this, "Número Celular menor que 10 digitos!", Toast.LENGTH_LONG).show();
			} else if (txtFoneOrigem.getText().toString().length() > 10) {
				Toast.makeText(CadastroActivity.this, "Número celular maior que 10 digitos!", Toast.LENGTH_LONG).show();
			} else if (txtEmail.getText().toString().length() < 0) {
				Toast.makeText(CadastroActivity.this, "E-Email não foi preenchido!", Toast.LENGTH_LONG).show();
			} else if ((txtSenha.getText().toString().length() < 0)) {
				Toast.makeText(CadastroActivity.this, "Digite sua senha!", Toast.LENGTH_LONG).show();
			} else if ((txtIntervalo.getText().toString().length() < 0)) {
				if ((Integer.valueOf(txtIntervalo.getText().toString()) < 0)) {
					Toast.makeText(CadastroActivity.this, "Intervalo válido tem de ser maior que 0(zero)!", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(CadastroActivity.this, "Necessário informar o intervalo para rastreamento!", Toast.LENGTH_LONG).show();
				}
			} else if (txtFoneDestino.getText().toString().length() < 10) {
				Toast.makeText(CadastroActivity.this, "Número Celular menor que 10 digitos!", Toast.LENGTH_LONG).show();
			} else if (txtFoneDestino.getText().toString().length() > 10) {
				Toast.makeText(CadastroActivity.this, "Número celular maior que 10 digitos!", Toast.LENGTH_LONG).show();
			} else if ((txtNotificacao.getText().toString().length() < 0)) {
				if ((Integer.valueOf(txtNotificacao.getText().toString()) < 0)) {
					Toast.makeText(CadastroActivity.this, "Intervalo válido tem de ser maior que 0(zero)!", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(CadastroActivity.this, "Necessário informar o intervalo para notificação!", Toast.LENGTH_LONG).show();
				}
			} else {

				String sql = "UPDATE Configuracao SET email='" + txtEmail.getText().toString() + "'," + "celular='"
						+ txtFoneOrigem.getText().toString() + "'," + "senha='" + txtSenha.getText().toString() + "'," + "sms='" + opcao1 + "',"
						+ "mail='" + opcao2 + "'," + "timeRequet='" + txtIntervalo.getText().toString() + "'," + "timeSend='"
						+ txtNotificacao.getText().toString() + "'," + "celularDestino='" + txtFoneDestino.getText().toString() + "';";
				// Toast.makeText(getBaseContext(), sql, Toast.LENGTH_LONG).show();
				db.execSQL(sql);
				db.close();

				Builder msg = new Builder(CadastroActivity.this);
				msg.setMessage("Registro alterados com sucesso!\nDeseja retorno ao MENU?");
				msg.setNegativeButton("Não", null);
				msg.setPositiveButton("Sim", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent it = new Intent(getBaseContext(), MainActivity.class);
						startActivity(it);
					}
				});
				msg.show();

			}
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), "Erro em atualizar:" + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	public void onInsert(View v) {
		SQLiteDatabase db = openOrCreateDatabase("Configuracao.db", Context.MODE_PRIVATE, null);

		EditText txtfoneOrigem = (EditText) findViewById(R.id.edtFoneOrigem);
		EditText txtfoneDestino = (EditText) findViewById(R.id.edtFoneDestino);
		EditText txtEmail = (EditText) findViewById(R.id.edtEmail);
		EditText txtSenha = (EditText) findViewById(R.id.edtSenha);
		EditText txtIntervalo = (EditText) findViewById(R.id.edtIntervalo);
		EditText txtNotificacao = (EditText) findViewById(R.id.edtTempo);
		RadioButton rb1 = (RadioButton) findViewById(R.id.rdSms);
		RadioButton rb2 = (RadioButton) findViewById(R.id.rdEmail);

		// Toast.makeText(getBaseContext(), "CELULAR:" + txtfone.getText().toString(), Toast.LENGTH_SHORT).show();
		// Toast.makeText(getBaseContext(), "EMAIL:" + txtEmail.getText().toString(), Toast.LENGTH_SHORT).show();
		// Toast.makeText(getBaseContext(), "SENHA:" + txtSenha.getText().toString(), Toast.LENGTH_SHORT).show();
		// Toast.makeText(getBaseContext(), "INTERVALO:" + txtIntervalo.getText(), Toast.LENGTH_SHORT).show();

		String opcao1 = "1";
		String opcao2 = "1";
		// Toast.makeText(getBaseContext(), "SMS:" + opcao1, Toast.LENGTH_SHORT).show();
		// Toast.makeText(getBaseContext(), "MAIL:" + opcao2, Toast.LENGTH_SHORT).show();

		// Toast.makeText(CadastroActivity.this, "Radio:"+rb1.isChecked(), Toast.LENGTH_SHORT).show();
		// Toast.makeText(CadastroActivity.this, "Radio:"+rb2.isChecked(), Toast.LENGTH_SHORT).show();
		if (rb2.isChecked()) {
			opcao2 = "0";
		}
		if (rb1.isChecked()) {
			opcao1 = "0";
		}

		if (txtfoneOrigem.getText().toString().length() < 8) {
			Toast.makeText(CadastroActivity.this, "Número Celular menor que 10 digitos!", Toast.LENGTH_LONG).show();
		} else if (txtfoneOrigem.getText().toString().length() > 10) {
			Toast.makeText(CadastroActivity.this, "Número celular maior que 10 digitos!", Toast.LENGTH_LONG).show();
		} else if (txtEmail.getText().toString().length() < 0) {
			Toast.makeText(CadastroActivity.this, "E-Email não foi preenchido!", Toast.LENGTH_LONG).show();
		} else if ((txtSenha.getText().toString().length() < 0)) {
			Toast.makeText(CadastroActivity.this, "Digite sua senha!", Toast.LENGTH_LONG).show();
		} else if ((txtIntervalo.getText().toString().length() < 0)) {
			if ((Integer.valueOf(txtIntervalo.getText().toString()) < 0)) {
				Toast.makeText(CadastroActivity.this, "Intervalo válido tem de ser maior que 0(zero)!", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(CadastroActivity.this, "Necessário informar o intervalo para rastreamento!", Toast.LENGTH_LONG).show();
			}

		} else if (txtfoneDestino.getText().toString().length() < 10) {
			Toast.makeText(CadastroActivity.this, "Número Celular menor que 10 digitos!", Toast.LENGTH_LONG).show();
		} else if (txtfoneDestino.getText().toString().length() > 10) {
			Toast.makeText(CadastroActivity.this, "Número celular maior que 10 digitos!", Toast.LENGTH_LONG).show();
		} else if ((txtNotificacao.getText().toString().length() < 0)) {
			if ((Integer.valueOf(txtNotificacao.getText().toString()) < 0)) {
				Toast.makeText(CadastroActivity.this, "Intervalo válido tem de ser maior que 0(zero)!", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(CadastroActivity.this, "Necessário informar o intervalo para notificação!", Toast.LENGTH_LONG).show();
			}
		} else {
			ContentValues ctv = new ContentValues();
			if (txtfoneOrigem.getText().toString().length() > 0) {
				ctv.put("celular", txtfoneOrigem.getText().toString());
			}
			if (txtfoneDestino.getText().toString().length() > 0) {
				ctv.put("celularDestino", txtfoneDestino.getText().toString());
			}			
			if (txtEmail.getText().toString().length() > 0) {
				ctv.put("email", txtEmail.getText().toString());
			}
			if (txtSenha.getText().toString().length() > 0) {
				ctv.put("senha", txtSenha.getText().toString());
			}
			if (txtIntervalo.getText().toString().length() > 0) {
				ctv.put("timeRequet", txtIntervalo.getText().toString());
			}
			if (txtIntervalo.getText().toString().length() > 0) {
				ctv.put("timeSend", txtNotificacao.getText().toString());
			}
			ctv.put("sms", opcao1);
			ctv.put("mail", opcao2);
			ctv.put("isRastreando", "1");

			try {
				if (db.insert("Configuracao", "_id", ctv) > 0) {
					Toast.makeText(getBaseContext(), "Sucesso em inserir Configuração!", Toast.LENGTH_LONG).show();
					Intent it = new Intent(getBaseContext(), MainActivity.class);
					startActivity(it);
				} else {
					Toast.makeText(getBaseContext(), "Erro em inserir o Configuração!", Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				Toast.makeText(getBaseContext(), "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
			}
			db.close();
		}

	}

}
