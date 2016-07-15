package br.com.jsferreira.findson;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

@SuppressLint("NewApi")
public class SendSMS extends Service implements Runnable {

	static final String	TAG			= "SMS_SERVIÇO_SEND";

	// The vital part, it allows access to location and GPS status services
	private String		phoneNumberOrig;
	private String		phoneNumberDest;
	private String		isMail		= "1";
	private String		emailAdd;
	private int			time;
	private boolean		condicao	= true;

	public void onCreate() {
		new Thread(SendSMS.this).start();
	}

	@Override
	public void run() {
		try {
			//Log.d(TAG, "Metodo::run|Executando:" + condicao);
			for (;;) {
				getConfigDataBase();
				//Log.d(TAG, "Log: " + "Envio!!!!!!>>" + time);
				Thread.sleep(time);
			}
		} catch (InterruptedException e) {
			Thread.interrupted();
		}

	}

	private void getConfigDataBase() {
		try {
			SQLiteDatabase dbConfiguracao = openOrCreateDatabase("Configuracao.db", Context.MODE_PRIVATE, null);
			Cursor curso = dbConfiguracao
					.rawQuery("Select celular,sms,mail,timeRequet,email,timeSend,isRastreando,celularDestino from Configuracao", null);
			if (curso.moveToNext()) {

				
				phoneNumberOrig = curso.getString(0);
				phoneNumberDest = curso.getString(7);
				isMail = curso.getString(2).toString();
				time = 1000 * 60 * Integer.valueOf(curso.getString(5)).intValue();

				// Log.d(TAG, "QUAL OPÇÃO DE ENVIO::" + isMail.toString());
				// Log.d(TAG, "QUAL OPÇÃO DE ENVIO::" + curso.getString(1).toString());
				// Log.d(TAG, "QUAL OPÇÃO DE ENVIO::" + isMail.getClass());
				// Log.d(TAG, "QUAL OPÇÃO DE ENVIO::" + isMail.equalsIgnoreCase("1"));

				String rast = curso.getString(6).toString();
				//Log.d(TAG, "3-Metodo::run|Executando:" + rast);
				if (rast.equalsIgnoreCase("0")) {
					//Log.d(TAG, "3-Metodo::run|Executando:IF");
					condicao = true;
					if (isMail.equalsIgnoreCase("0")) {
						Log.d(TAG, "getConfigDataBase::EMAIL");
						emailAdd = curso.getString(4);
						String message = getCoordenadas();
						EMAIL(phoneNumberOrig, emailAdd, message);
					} else {
						Log.d(TAG, "getConfigDataBase::SMS");
						String message = getCoordenadas();
						if (message.trim().length() > 0) {
							Log.d(TAG, "getConfigDataBase::SMS>>" + message.trim().length());
							SMS(phoneNumberDest, message.substring(0, message.toString().length()));
						}
					}
				} else {
					//Log.d(TAG, "3-Metodo::run|Executando:ELSE");
					condicao = false;
					Thread.interrupted();
				}
			} else {
				Log.d(TAG, "INFO - getConfigDataBase: Nenhum registro ainda obtido!");
			}
			dbConfiguracao.close();
		} catch (NumberFormatException e) {
			Log.d(TAG, "getConfigDataBase-Erro:" + e.getMessage());
		}
	}

	@SuppressLint("NewApi")
	private String getCoordenadas() {
		String addressString = null;
		String DescribeText = "";
		try {
			// addressString = null;
			SQLiteDatabase dbCoordenadas = openOrCreateDatabase("Coordenadas.db", Context.MODE_PRIVATE, null);
			Cursor curso = dbCoordenadas.rawQuery("Select la,lo,myDate from Coordenadas ASC", null);
			//Cursor cursor = dbCoordenadas.query("Coordenadas", new String[]{"la", "lo", "myDate"},null, null, null, null, "myDate DESC");
			if (curso.moveToLast()) {
				String coordinates[] = { curso.getString(0).toString(), curso.getString(1).toString() };
				double lat = Double.parseDouble(coordinates[0]);
				double lng = Double.parseDouble(coordinates[1]);

				Log.d(TAG, "Latitude:" + lat);
				Log.d(TAG, "Longitude:" + lng);

				String dd = "("+phoneNumberOrig.substring(0, 2)+")";
				String fone = phoneNumberOrig.substring(2, phoneNumberOrig.toString().length());
				
				DescribeText = dd+fone+": https://maps.google.com/maps?q=" + lat + ","+ lng + "&ll=" + lat;
				
				// Geocoder gc = new Geocoder(null, Locale.getDefault());
				Geocoder gc = new Geocoder(this, Locale.getDefault());

				try {
					// Log.d(TAG, "1");
					List<Address> addresses = gc.getFromLocation(lat, lng, 1);
					// Log.d(TAG, "2");
					StringBuilder sb = new StringBuilder();
					// Log.d(TAG, "3");

					if (addresses.size() > 0) {
						// Log.d(TAG, "4");
						Address address = addresses.get(0);
						// Log.d(TAG, "5");

						for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
							// Log.d(TAG, "6");
							sb.append(address.getAddressLine(i));
							// Log.d(TAG, "7");

							sb.append(address.getCountryName());
							// Log.d(TAG, "8");

							addressString = address.getAddressLine(0);
							// Log.d(TAG, "9");
							addressString += "\nCidade:" + address.getLocality();
							// addressString += "\nPais:" + address.getCountryName();
							addressString += "\nCEP:" + address.getPostalCode();
						}
					}
					//Log.d(TAG, DescribeText);
				} catch (IOException e) {
					Log.d(TAG, "Erro:" + e.getMessage());
				}

				// dbCoordenadas.close();
			} else {
				// Toast.makeText(getBaseContext(), "Aguarde! O sistema ainda está em processamento a conexão GPS!",
				// Toast.LENGTH_LONG).show();
			}
		} catch (NumberFormatException e) {
			Log.d(TAG, "getCoordenadas - Erro: " + e.getMessage());
		}
		// return addressString;
		// return DescribeText;
		return DescribeText;
	}

	private void SMS(String phoneNumber, String message) {

		try {
			//Metodo que está funcionando corretamente não fazer nenhuma alteração.
			String sent = "android.telephony.SmsManager.STATUS_ON_ICC_SENT";
			SmsManager smsManager = SmsManager.getDefault();
			PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(sent), 0);
			smsManager.sendTextMessage(phoneNumber, null, message, pi, null);
		} catch (Exception e) {
			Log.d(TAG, "SMS - Erro:" + e.getMessage());
		}

		/*
		 * try { // Metodo que está enviando a mensagem normalmente. Só que não consegue enviar uma mensagem muito
		 * grande // apenas com no maximo 128 Bits. PendingIntent pi = PendingIntent.getActivity(this, 0, new
		 * Intent(this, SendSMS.class), 0); android.telephony.gsm.SmsManager sms =
		 * android.telephony.gsm.SmsManager.getDefault(); sms.sendTextMessage(phoneNumber, null, message, pi, null);
		 * Log.d(TAG, "SendSMS: SMS - Enviado com sucesso!"); } catch (Exception e) { Log.d(TAG, "SendSMS: SMS - Erro:"
		 * + e.getMessage()); }
		 */

	}

	private void EMAIL(String phoneNumber, String emailAdd, String mensagemEmail) {
		try {
			Log.d(TAG, "SendSMS: EMAIL - 1");
			//Intent gmail = new Intent(Intent.ACTION_VIEW);
			Intent gmail = new Intent(Intent.ACTION_SEND);
			Log.d(TAG, "SendSMS: EMAIL - 2");
            gmail.setClassName("com.google.android.gm","com.google.android.gm.ComposeActivityGmail");
            Log.d(TAG, "SendSMS: EMAIL - 3");
            gmail.putExtra(Intent.EXTRA_EMAIL, new String[] { "jeansferreira@gmail.com" });
            Log.d(TAG, "SendSMS: EMAIL - 4");
            gmail.setData(Uri.parse("jeansferreira@gmail.com"));
            Log.d(TAG, "SendSMS: EMAIL - 5");
            gmail.putExtra(Intent.EXTRA_SUBJECT, "Notificação CELULAR");
            Log.d(TAG, "SendSMS: EMAIL - 6");
            gmail.setType("plain/text");
            Log.d(TAG, "SendSMS: EMAIL - 7");
            gmail.putExtra(Intent.EXTRA_TEXT, "hi android jack!");
            Log.d(TAG, "SendSMS: EMAIL - 8");
            startService(gmail);
            Log.d(TAG, "SendSMS: EMAIL - 9");
            
            
			String emailaddress[] = { emailAdd };
			// Intent emailIntent = new Intent(android.content.Intent.ACTION_SENDTO);
			Intent emailIntent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("gm", "jeansferreira@gmail.com", null));
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, emailaddress.toString());
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "FindSon - Notificação do Rastreamento!");
			emailIntent.setType("plain/text");
			emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, mensagemEmail.toString());
			//startActivity(emailIntent);
			
			startService(emailIntent);
			// LocalBroadcastManager.getInstance(this).sendBroadcast(emailIntent);
			// startActivity(Intent.createChooser(emailIntent, "Send mail..."));
		} catch (Exception e) {
			Log.d(TAG, "SendSMS: EMAIL - Erro:" + e.getMessage());
		}

	}

	public void onStop() {
		try {
			condicao = false;
			Thread.interrupted();
			Log.d(TAG, "Thread - FINALIZADA");
		} catch (Exception e) {
			Log.d(TAG, "onStop - Erro:" + e.getMessage());
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
