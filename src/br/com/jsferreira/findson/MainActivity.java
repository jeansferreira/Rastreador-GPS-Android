package br.com.jsferreira.findson;

import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	static final String					TAG			= "MAIL_ACTIVITY";
	protected static final int			DIALOG_1	= 0;					// Dialog 1 ID
	protected static final int			DIALOG_2	= 2;					// Dialog 2 ID

	// Dialog 1 default values (static variables to maintain the values on screen orientation change)
	protected static String				cdField2DefaultValue;

	// Interface elements
	protected EditText					cdField2DefValEdit;
	protected Button					showCDBtn;
	protected Dialog					dialog;
	// protected Dialog dialog2;
	protected String					senha;
	protected Intent					itService;
	protected Intent					itSendSMS;
	protected SendSMS					clSendSMS;
	protected LocalizarPosicaoService	clService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		clSendSMS = new SendSMS();
		clService = new LocalizarPosicaoService();
		setContentView(R.layout.main);
	}

	private void createConfigDataBase() {

		// SQLiteDatabase dbCoordenadas1 = openOrCreateDatabase("Coordenadas.db", Context.MODE_PRIVATE, null);
		// dbCoordenadas1.execSQL("delete from Coordenadas");
		// dbCoordenadas1.close();

		try {
			// Toast.makeText(getBaseContext(), "1", Toast.LENGTH_LONG).show();

			//SQLiteDatabase dbConfiguracao1 = openOrCreateDatabase("Configuracao.db", Context.MODE_PRIVATE, null);
			//dbConfiguracao1.execSQL("DROP TABLE IF EXISTS Configuracao");
			//dbConfiguracao1.close();

			//Toast.makeText(getBaseContext(), "2", Toast.LENGTH_LONG).show();
			//SQLiteDatabase dbConfiguracao2 = openOrCreateDatabase("Coordenadas.db", Context.MODE_PRIVATE, null);
			//dbConfiguracao2.execSQL("DROP TABLE IF EXISTS Coordenadas");

			SQLiteDatabase dbConfiguracao = openOrCreateDatabase("Configuracao.db", Context.MODE_PRIVATE, null);
			StringBuilder sqlConfiguracao = new StringBuilder();
			sqlConfiguracao.append("CREATE TABLE IF NOT EXISTS Configuracao (");
			sqlConfiguracao.append("_id INTEGER PRIMARY KEY, ");
			sqlConfiguracao.append("celular VARCHAR(10),");
			sqlConfiguracao.append("email VARCHAR(50),");
			sqlConfiguracao.append("senha VARCHAR(10),");
			sqlConfiguracao.append("sms VARCHAR(1),");
			sqlConfiguracao.append("mail VARCHAR(1),");
			sqlConfiguracao.append("isRastreando VARCHAR(1),");
			sqlConfiguracao.append("timeRequet VARCHAR(2),");
			sqlConfiguracao.append("timeSend VARCHAR(2),");
			sqlConfiguracao.append("celularDestino VARCHAR(10));");
			dbConfiguracao.execSQL(sqlConfiguracao.toString());
			dbConfiguracao.close();

			SQLiteDatabase dbCoordenadas = openOrCreateDatabase("Coordenadas.db", Context.MODE_PRIVATE, null);

			StringBuilder sqlCoordenadas = new StringBuilder();
			sqlCoordenadas.append("CREATE TABLE IF NOT EXISTS Coordenadas (");
			sqlCoordenadas.append("_id INTEGER PRIMARY KEY, ");
			sqlCoordenadas.append("la VARCHAR(50),");
			sqlCoordenadas.append("lo VARCHAR(50),");
			sqlCoordenadas.append("myDate VARCHAR(25),");
			sqlCoordenadas.append("address VARCHAR(200));");
			dbCoordenadas.execSQL(sqlCoordenadas.toString());
			dbCoordenadas.close();
		} catch (SQLException e) {
			Toast.makeText(getBaseContext(), "Problema na criação dos dados. verifique o celular. Erro:" + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	public void onResume() {

		super.onResume();
		try {
			// Toast.makeText(getBaseContext(), "onResume:Inicio", Toast.LENGTH_LONG);
			Button iniciar = (Button) findViewById(R.id.bttIniciarRastreamento);
			Button finalizar = (Button) findViewById(R.id.bttFinalizarRastreamento);

			// TODO: Cria as tabelas no Banco de Dados.
			createConfigDataBase();
			SQLiteDatabase dbConfig = openOrCreateDatabase("Configuracao.db", Context.MODE_PRIVATE, null);
			Cursor curso = dbConfig.rawQuery("Select * from Configuracao", null);

			if (curso.moveToNext()) {
				String startRastreamento = curso.getString(6).toString();
				senha = curso.getString(3).toString();
				Log.d("MainActivity", "RASTREAMENTO:" + startRastreamento);
				if (startRastreamento.equalsIgnoreCase("0")) {
					Log.d("MainActivity", "1-RASTREAMENTO:" + startRastreamento);
					iniciar.setEnabled(false);
					finalizar.setEnabled(true);
				} else {
					Log.d("MainActivity", "2-RASTREAMENTO:" + startRastreamento);
					iniciar.setEnabled(true);
					finalizar.setEnabled(false);
				}
				Log.d("MainActivity", "3-RASTREAMENTO:" + startRastreamento);
			} else {
				Builder msg = new Builder(MainActivity.this);
				msg.setMessage("É necessário configurar o perfil!\nDeseja continuar?");
				msg.setNegativeButton("Não", null);
				msg.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent it = new Intent(getBaseContext(), CadastroActivity.class);
						startActivity(it);
					}
				});
				msg.show();
			}
			iniciar.refreshDrawableState();
			finalizar.refreshDrawableState();

			dbConfig.close();
		} catch (Exception e) {
			Log.d("MainActivity", "onResume-Erro:" + e.getMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onClickConfiguracoes(View v) {
		if (senha == "" || senha == null){
			Intent it = new Intent(getApplicationContext(), CadastroActivity.class);
			startActivity(it);
		}else{
			dialog = onCreateDialog(0);
			dialog.show();
		}
	}

	public void onClicklogCoordenadas(View v) {
		try {
			SQLiteDatabase db = openOrCreateDatabase("Coordenadas.db", Context.MODE_PRIVATE, null);
			Cursor curso = db.rawQuery("Select * from Coordenadas", null);

			if (curso.moveToNext()) {
				Intent it = new Intent(getBaseContext(), ListarActivity.class);
				startActivity(it);
			} else {
				Toast.makeText(getBaseContext(), "Nenhum registro encontrado para visualizar!", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			Log.d("onClicklogCoordenadas-Erro:", e.getMessage());
		}
		Log.d("onClicklogCoordenadas:", String.valueOf(new Date().toString()));
	}

	public void onClickIniciarLocalizacao(View v) {

		SQLiteDatabase db = openOrCreateDatabase("Configuracao.db", Context.MODE_PRIVATE, null);
		// Toast.makeText(getBaseContext(), "onRedirect:2", Toast.LENGTH_SHORT).show();
		Cursor curso = db.rawQuery("Select * from Configuracao", null);

		final Button iniciar = (Button) findViewById(R.id.bttIniciarRastreamento);
		final Button finalizar = (Button) findViewById(R.id.bttFinalizarRastreamento);

		try {
			// Toast.makeText(getBaseContext(), "onRedirect:4", Toast.LENGTH_SHORT).show();
			if (curso.moveToNext()) {

				Builder msg = new Builder(MainActivity.this);
				msg.setMessage("Deseja iniciar o Rastreamento?");
				msg.setNegativeButton("Não", null);
				msg.setPositiveButton("Sim", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						setInRastreamento("0");
						LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
						// locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

						// getting GPS status
						boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
						if (!isGPSEnabled) {
							Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(intent);
						}

						// Log.d("1-onClickIniciarLocalizacao:", String.valueOf(new Date().toString()));

						if (itService == null) {
							itService = new Intent(getBaseContext(), clService.getClass());
							Log.d("onClickIniciarLocalizacao-startService", "Serviço incializado!");
							startService(itService);
						} else {
							Log.d("onClickIniciarLocalizacao", "Serviço já incializado!");

						}

						if (itSendSMS == null) {
							Log.d("onClickIniciarLocalizacao", "Serviço de envio incializado!");
							itSendSMS = new Intent(getBaseContext(), clSendSMS.getClass());
							startService(itSendSMS);
						} else {
							Log.d("onClickIniciarLocalizacao", "Serviço de envio já incializado!");

						}

						iniciar.setEnabled(false);
						finalizar.setEnabled(true);
						// Mudar cor texto
						// Color.GREEN
						// iniciar.setTextColor(Color.parseColor("GRAY")); // set button text colour to be blue
						// finalizar.setTextColor(Color.parseColor("BLACK")); // set button text colour to be blue
						// Mudar cor fundo
						// iniciar.setBackgroundColor(Color.parseColor("black"));
						// finalizar.setBackgroundColor(Color.parseColor("black"));

						iniciar.refreshDrawableState();
						finalizar.refreshDrawableState();
						Log.d("2-onClickIniciarLocalizacao:", String.valueOf(new Date().toString()));
					}
				});
				msg.show();
			} else {
				// Toast.makeText(getBaseContext(), "onRedirect:7", Toast.LENGTH_SHORT).show();
				Log.d("3-onClickIniciarLocalizacao:", String.valueOf(new Date().toString()));
				Intent it = new Intent(getBaseContext(), CadastroActivity.class);
				// Toast.makeText(getBaseContext(), "onRedirect:8", Toast.LENGTH_SHORT).show();
				startActivity(it);
			}
		} catch (Exception e) {
			Toast.makeText(getBaseContext(),
					"Problema em executar localização\n verifique se o seu celular é compativel.:" + e.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}
	}

	public void onClickFinalizarLocalizacao(View v) {

		try {
			Log.d("onClickFinalizarLocalizacao", "Finalizar");
			dialog = onCreateDialog(2);
			dialog.show();
		} catch (Exception e) {
			Toast.makeText(getBaseContext(),
					"Problema em finalizar localização\n verifique se o seu celular é compativel.:" + e.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}
	}

	public void onClickClear(View v) {
		try {
			Builder msg = new Builder(MainActivity.this);
			msg.setMessage("Deseja limpar os rastreamentos anteriores?");
			msg.setNegativeButton("Não", null);
			msg.setPositiveButton("Sim", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					SQLiteDatabase dbCoordenadas = openOrCreateDatabase("Coordenadas.db", Context.MODE_PRIVATE, null);

					StringBuilder sqlCoordenadas = new StringBuilder();
					sqlCoordenadas.append("DELETE FROM Coordenadas");
					dbCoordenadas.execSQL(sqlCoordenadas.toString());
					Toast.makeText(getBaseContext(), "Lista de RASTREAMENTOS excluida!", Toast.LENGTH_LONG).show();
				}
			});
			msg.show();
		} catch (Exception e) {
			Toast.makeText(getBaseContext(),
					"Problema em remover localizações\n verifique se o seu celular é compativel.:" + e.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void setInRastreamento(String status) {
		try {
			SQLiteDatabase dbConfiguracao = openOrCreateDatabase("Configuracao.db", Context.MODE_PRIVATE, null);
			Log.d("setInRastreamento", "Metodo - setInRastreamento");
			// dbConfiguracao = openOrCreateDatabase("Configuracao.db", Context.MODE_PRIVATE, null);

			// String sql = "UPDATE Configuracao SET isRastreando='"+status+"' WHERE _id = 1";
			// dbConfiguracao.execSQL(sql);
			// dbConfiguracao.compileStatement(sql);

			ContentValues ctv = new ContentValues();
			ctv.put("isRastreando", status);

			int id = 1;

			if (dbConfiguracao.update("Configuracao", ctv, "_id=?", new String[] { String.valueOf(id) }) > 0) {
				Log.d("setInRastreamento", "Sucesso em marcar rastreamento!");
			}

			dbConfiguracao.close();
			Button iniciar = (Button) findViewById(R.id.bttIniciarRastreamento);
			Button finalizar = (Button) findViewById(R.id.bttFinalizarRastreamento);

			iniciar.setEnabled(true);
			finalizar.setEnabled(false);

			iniciar.refreshDrawableState();
			finalizar.refreshDrawableState();

		} catch (Exception e) {
			Toast.makeText(getBaseContext(),
					"Problema em marcar rastreamento\n verifique se o seu celular é compativel.:" + e.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}
	}

	protected Dialog onCreateDialog(int id) {
		// Toast.makeText(getBaseContext(), "B", Toast.LENGTH_SHORT).show();
		switch (id) {
		case DIALOG_1: {
			// Toast.makeText(getBaseContext(), "C", Toast.LENGTH_SHORT).show();
			final Dialog dialog = new Dialog(this);

			// Setup the custom dialog interface elements
			dialog.setContentView(R.layout.activity_dialog_signin);
			dialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

			Button cdOKBtn = (Button) dialog.findViewById(R.id.cdOKBtn);
			Button cdCancelBtn = (Button) dialog.findViewById(R.id.cdCancelBtn);

			cdOKBtn.setOnClickListener(new OnClickListener() {
				// Called when the OK button is pressed
				@Override
				public void onClick(View v) {
					EditText cdField2Edit = (EditText) dialog.findViewById(R.id.cdField2Edit);

					String field2Value = cdField2Edit.getText().toString();

					// Call a method to process the dialog result
					MainActivity.this.processDialog1Result(field2Value);

					// If we call removeDialog, then onCreateDialog will be called every time
					// the dialog needs to be displayed, otherwise if we call dismissDialog,
					// only onPrepareDialog will be called after the first dialog creation
					// (the first showDialog).
					removeDialog(DIALOG_1);
					// dismissDialog(DIALOG_1);
				}

			});

			cdCancelBtn.setOnClickListener(new OnClickListener() {
				// Called when the Cancel button is pressed
				@Override
				public void onClick(View v) {
					Log.d("setOnClickListener", "onCancela");
					removeDialog(DIALOG_1);
					dialog.cancel();
					// dismissDialog(DIALOG_1);

				}

			});

			dialog.setOnCancelListener(new OnCancelListener() {
				// Called when the Back button is pressed
				@Override
				public void onCancel(DialogInterface dialog) {
					Log.d("setOnClickListener", "dialog");
					removeDialog(DIALOG_1);
					dialog.cancel();
				}
			});
			// Toast.makeText(getBaseContext(), "D", Toast.LENGTH_SHORT).show();
			return dialog;
		}
		case DIALOG_2: {
			// Toast.makeText(getBaseContext(), "C", Toast.LENGTH_SHORT).show();
			final Dialog dialog = new Dialog(this);

			// Setup the custom dialog interface elements
			dialog.setContentView(R.layout.activity_dialog_signin);
			dialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

			Button cdOKBtn = (Button) dialog.findViewById(R.id.cdOKBtn);
			Button cdCancelBtn = (Button) dialog.findViewById(R.id.cdCancelBtn);

			cdOKBtn.setOnClickListener(new OnClickListener() {
				// Called when the OK button is pressed
				@Override
				public void onClick(View v) {

					if (itService != null) {
						itService = new Intent(getBaseContext(), LocalizarPosicaoService.class);
						Log.d("onCreateDialog-stopService", "Serviço finalizado!");
						clSendSMS.onStop();
						clService.onFinalizar();
						stopService(itService);
						stopService(itSendSMS);

					} else {
						Log.d("onCreateDialog", "Serviço ainda não finalizado!");
					}
					EditText cdField2Edit = (EditText) dialog.findViewById(R.id.cdField2Edit);
					String field2Value = cdField2Edit.getText().toString();
					MainActivity.this.processDialogStop(field2Value);
					removeDialog(DIALOG_2);

				}

			});

			cdCancelBtn.setOnClickListener(new OnClickListener() {
				// Called when the Cancel button is pressed
				@Override
				public void onClick(View v) {
					removeDialog(DIALOG_2);
					dialog.cancel();
					// dismissDialog(DIALOG_2);
				}

			});

			dialog.setOnCancelListener(new OnCancelListener() {
				// Called when the Back button is pressed
				@Override
				public void onCancel(DialogInterface dialog) {
					removeDialog(DIALOG_2);
					dialog.cancel();
				}
			});
			// Toast.makeText(getBaseContext(), "D", Toast.LENGTH_SHORT).show();
			return dialog;
		}
		default:
			return null;
		}
	}

	protected void processDialogStop(String field2Value) {
		// Toast.makeText(this, "CD1 - field2Value: " + field2Value, Toast.LENGTH_LONG).show();
		try {
			// Toast.makeText(this, "1-Digitação: : " + field2Value.getClass(), Toast.LENGTH_LONG).show();
			// Toast.makeText(this, "2-Digitação: : " + senha.getClass(), Toast.LENGTH_LONG).show();

			String ret = field2Value;
			
			if (ret.equalsIgnoreCase(senha)) {
				final Button iniciar = (Button) findViewById(R.id.bttIniciarRastreamento);
				final Button finalizar = (Button) findViewById(R.id.bttFinalizarRastreamento);

				iniciar.setEnabled(true);
				finalizar.setEnabled(true);

				TextView error = (TextView) dialog.findViewById(R.id.cdTextErros);
				error.setText("");
				Toast.makeText(this, "Senha correta!", Toast.LENGTH_SHORT).show();
				setInRastreamento("1");
				dialog.cancel();
			} else {
				EditText eraser = (EditText) dialog.findViewById(R.id.cdField2Edit);
				TextView error = (TextView) dialog.findViewById(R.id.cdTextErros);

				error.setText("Senha incorreta!");
				eraser.setText("");
				Toast.makeText(this, "Senha incorreta!", Toast.LENGTH_SHORT).show();
				// Toast.makeText(this, "Senha incorreta, favor digite novamente!", Toast.LENGTH_LONG).show();
			}
		} catch (Throwable e) {
			Toast.makeText(this, "Problema no Login. Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	protected void processDialog1Result(String field2Value) {
		// Toast.makeText(this, "CD1 - field2Value: " + field2Value, Toast.LENGTH_LONG).show();
		try {
			// Toast.makeText(this, "1-Digitação: : " + field2Value.getClass(), Toast.LENGTH_LONG).show();
			// Toast.makeText(this, "2-Digitação: : " + senha.getClass(), Toast.LENGTH_LONG).show();

			String ret = field2Value;

			if (ret.equalsIgnoreCase(senha)) {
				TextView error = (TextView) dialog.findViewById(R.id.cdTextErros);
				error.setText("");
				Toast.makeText(this, "Senha correta!", Toast.LENGTH_SHORT).show();
				dialog.cancel();
				Intent it = new Intent(getBaseContext(), CadastroActivity.class);
				startActivity(it);
			} else {
				EditText eraser = (EditText) dialog.findViewById(R.id.cdField2Edit);
				TextView error = (TextView) dialog.findViewById(R.id.cdTextErros);

				error.setText("Senha incorreta!");
				eraser.setText("");
				Toast.makeText(this, "Senha incorreta!", Toast.LENGTH_SHORT).show();
				// Toast.makeText(this, "Senha incorreta, favor digite novamente!", Toast.LENGTH_LONG).show();
			}
		} catch (Throwable e) {
			Toast.makeText(this, "Problema no Login. Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	public void onTeste(View v) {
		
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		Toast.makeText(this, tm.getSimSerialNumber(), Toast.LENGTH_LONG).show();
		
		/*
		Intent intent = new Intent(getBaseContext(), SendSMS.class);
		startService(intent);
		/*
		 * Toast.makeText(this, "A", Toast.LENGTH_SHORT).show();
		 * 
		 * Intent shareIntent1 = new Intent(android.content.Intent.ACTION_SEND); Toast.makeText(this, "B",
		 * Toast.LENGTH_SHORT).show(); shareIntent1.setType("text/plain"); Toast.makeText(this, "C",
		 * Toast.LENGTH_SHORT).show(); shareIntent1.putExtra(android.content.Intent.EXTRA_SUBJECT,
		 * "FindSon - Notificação do Rastreamento!"); Toast.makeText(this, "D", Toast.LENGTH_SHORT).show();
		 * shareIntent1.putExtra(android.content.Intent.EXTRA_TEXT, "aaaaaaaaaaaaaaaaaaaaaaa");
		 * shareIntent1.putExtra(android.content.Intent.EXTRA_EMAIL, "jeansferreira@gmail.com"); Toast.makeText(this,
		 * "E", Toast.LENGTH_SHORT).show(); PackageManager pm1 = v.getContext().getPackageManager();
		 * Toast.makeText(this, "F", Toast.LENGTH_SHORT).show(); List<ResolveInfo> activityList1 =
		 * pm1.queryIntentActivities(shareIntent1, 0); Toast.makeText(this, "G", Toast.LENGTH_SHORT).show(); for (final
		 * ResolveInfo app : activityList1) { Toast.makeText(this, "H", Toast.LENGTH_SHORT).show(); Toast.makeText(this,
		 * ">>>" + (app.activityInfo.name), Toast.LENGTH_SHORT).show(); if ((app.activityInfo.name).contains("gm")) {
		 * Toast.makeText(this, "I", Toast.LENGTH_SHORT).show(); final ActivityInfo activity = app.activityInfo;
		 * Toast.makeText(this, "J", Toast.LENGTH_SHORT).show(); final ComponentName name = new
		 * ComponentName(activity.applicationInfo.packageName, activity.name); Toast.makeText(this, "K",
		 * Toast.LENGTH_SHORT).show(); shareIntent1.addCategory(Intent.CATEGORY_LAUNCHER); Toast.makeText(this, "L",
		 * Toast.LENGTH_SHORT).show(); shareIntent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
		 * Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED); Toast.makeText(this, "M", Toast.LENGTH_SHORT).show();
		 * shareIntent1.setComponent(name); Toast.makeText(this, "N", Toast.LENGTH_SHORT).show();
		 * v.getContext().startService(shareIntent1); break; } }
		 * 
		 * /* final CharSequence[] items = { "Facebook", "Twitter", "Gmail" };
		 * 
		 * AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this); builder.setTitle("Share Via:");
		 * builder.setItems(items, new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog,
		 * int item) {
		 * 
		 * if (items[item] == "Gmail") {
		 * 
		 * //Intent email = new Intent(Intent.ACTION_SEND); Log.d("GMAIL", "A");
		 * 
		 * Intent email = new Intent(Intent.ACTION_VIEW, Uri.fromParts("gm", "jeansferreira@gmail.com", null));
		 * Log.d("GMAIL", "A"); email.putExtra(android.content.Intent.EXTRA_EMAIL, "jean.ferreira@gmail.com.br");
		 * Log.d("GMAIL", "A"); email.putExtra(android.content.Intent.EXTRA_SUBJECT,
		 * "FindSon - Notificação do Rastreamento!"); Log.d("GMAIL", "A"); email.setType("message/rfc822");
		 * Log.d("GMAIL", "A"); email.putExtra(android.content.Intent.EXTRA_TEXT, "testesssssssssssssssss");
		 * Log.d("GMAIL", "A");
		 * 
		 * //email.putExtra(Intent.EXTRA_EMAIL, new String[] { "" }); //email.putExtra(Intent.EXTRA_SUBJECT, "dsdsd");
		 * //email.putExtra(Intent.EXTRA_TEXT, "www.dsdsd.com"); //startActivityForResult(Intent.createChooser(email,
		 * "Choose an Email client :"), item); startActivity(email);
		 * 
		 * Intent email1 = new Intent(Intent.ACTION_SEND); email1.setType("plain/text");
		 * email1.putExtra(Intent.EXTRA_EMAIL,new String[]{"jeansferreira@gmail.com"});
		 * email1.putExtra(Intent.EXTRA_SUBJECT, "TimeSheet Wartsila");
		 * email1.putExtra(android.content.Intent.EXTRA_TEXT, "incluir dados");
		 * //startActivity(Intent.createChooser(email,"Enviar e-mail..." ));
		 * 
		 * } }
		 * 
		 * }); AlertDialog alert = builder.create(); alert.show();
		 */

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
