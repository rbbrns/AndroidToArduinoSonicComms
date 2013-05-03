/*******************************************************************************
 * Copyright (c) 2012 rob@theultimatelabs.com.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     rob@theultimatelabs.com - initial API and implementation
 ******************************************************************************/
package com.theultimatelabs.sonic;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.HashMap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder.AudioSource;
import android.media.RingtoneManager;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

//import com.theultimatelabs.intercom.TimeoutRunnable.ResponseDelayRunnable;

public class SonicService extends Service {
	
	private AudioRecord record;
	private AudioTrack track;
	//private final static int BUF_SIZE = 4096;
	//private Handler mTimeoutHandler;
	//private Handler mResponseDelayHandler;
	//private TimeoutRunnable mTimeoutRunnable;
	//private ResponseDelayRunnable mResponseDelayRunnable;
	//private PowerManager mPowerManager;
	
	//private final int STREAM_TYPE = AudioManager.STREAM_MUSIC;
	//private AudioManager audioManager;
	//private MulticastLock mMulticastLock;
	//private MediaPlayer mStartTalkBeep;
	//private MediaPlayer mStartListenBeep;
	//private boolean mStartTalkBeepSent = true;
	//private boolean mStopTalkBeepSent = true;
	//private static final double THRESHOLD = 0.75;
	//public final static String TAG = "IntercomService";
	//private static final long TIMEOUT = 20000; // 10 seconds
	//private static final long RESPONSE_DELAY = 100;

	@Override
	public void onCreate() {
		MyLog.v("onCreate");

		this.audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		this.audioOut = initializeAudioOut();
		this.audioIn = initializeAudioIn();
		
		/*mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP, "Intercom WakeLock");
		mWakeLock.setReferenceCounted(false);*/

		new AudioIn().start();
		new AudioOut().start();

		/*mTimeoutHandler = new Handler();
		mResponseDelayHandler = new Handler();
		mTimeoutRunnable = new TimeoutRunnable();
		mResponseDelayRunnable = new ResponseDelayRunnable();

		mStartTalkBeep = MediaPlayer.create(getApplicationContext(),
				R.raw.beep3);
		mStartListenBeep = MediaPlayer.create(getApplicationContext(),
				R.raw.opbeep);

		onStatusAll();*/

		/*
		 * Log.v(TAG, "Setup RTP"); AudioManager audioManager = (AudioManager)
		 * getApplicationContext() .getSystemService(Context.AUDIO_SERVICE);
		 * audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		 * 
		 * 
		 * mAudioStream = new AudioStream(mLocalAddress);
		 * Log.v(TAG,mAudioStream.toString()); //AudioCodec codec =
		 * AudioCodec.getCodec(100, "AMR/8000", "mode-set=1");
		 * //mAudioStream.setCodec(codec);
		 * mAudioStream.setCodec(AudioCodec.PCMU);
		 * mAudioStream.setMode(AudioStream.MODE_NORMAL);
		 * 
		 * mAudioGroup = new AudioGroup(); Log.v(TAG,mAudioGroup.toString());
		 * mAudioGroup.setMode(AudioGroup.MODE_NORMAL);
		 * Log.v(TAG,mAudioStream.toString());
		 * Log.v(TAG,mAudioGroup.toString());
		 * 
		 * mAudioStream.join(mAudioGroup); Log.v(TAG,"RTP setup");
		 * 
		 * 
		 * // AudioManager AudioManager audioManager = (AudioManager)
		 * getSystemService(AUDIO_SERVICE);
		 * audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		 * 
		 * 
		 * try {
		 * Log.v(TAG,"Creating audio stream on "+mLocalAddress.toString());
		 * mAudioStream = new AudioStream(mLocalAddress); } catch
		 * (SocketException e) { Log.d("Quit", "Socket Error"); System.exit(1);
		 * } mAudioStream.setMode(RtpStream.MODE_NORMAL);
		 * mAudioStream.setCodec(AudioCodec.PCMU);
		 * mAudioStream.associate(mBroadcastAddress
		 * ,mAudioStream.getLocalPort());//8585);//this.createInet(192,168,0,7),
		 * 8585);
		 * 
		 * // Initialize an AudioGroup and attach an AudioStream AudioGroup
		 * main_grp = new AudioGroup();
		 * main_grp.setMode(AudioGroup.MODE_ECHO_SUPPRESSION);
		 * mAudioStream.join(main_grp); Log.d("Log","PORT: "+
		 * mAudioStream.getLocalPort());
		 */

	}
	
	@Override
	public void onStart(Intent intent, int startId) {

		log.i("onStart");
		if (intent == null || intent.getAction() == null)
			return;
		log.i(intent.getAction());
		
		if (intent.getAction().equals("msg")) {
			DTMF()

		}

	}


	/*private void onTalk(boolean talkRequest) {

		mServerTalk = talkRequest;

		if (mServerTalk) {
			Log.i(TAG, "startTalk");

			synchronized (mState) {

				mStartTalkBeepSent = false;

				if (mState == SLEEP) {

					mWifiLock.acquire();
				} else if (mState == CLIENT) {
					Log.w(TAG, "Trying to talk while client");
				}
				mPrivate = false;
				mState = SERVER;
				mAudioRecord.startRecording();
				mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
				// beep();

			}
		} else {
			Log.i(TAG, "stopTalk");

			synchronized (mState) {
				mStopTalkBeepSent = false;
				mAudioRecord.stop();
				mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
				mPrivate = getSharedPreferences(PREFS, 0).getBoolean("private",
						false);
				if (mServerListen == false) {
					mState = SLEEP;
					mWifiLock.release();
					new Dismiss().start();
				}
			}

		}

		Intent status = new Intent(IntercomWidget.ACTION_STATUS);
		status.putExtra("talk", mServerTalk);
		status.putExtra("private", mPrivate);
		Log.v(TAG, "SendBroadcast");
		sendBroadcast(status);

	}*/

	/*private void stopAudio() {
		// mBroadcastAudioTrack.flush();
		// mBroadcastAudioTrack.pause();
		for (AudioTrack track : mAudioTracks.values()) {
			track.flush();
			track.pause();
		}
	}

	private void closeAudio() {
		mBroadcastAudioTrack.pause();
		mBroadcastAudioTrack.flush();
		stopAudio();
		mAudioTracks = new HashMap<InetAddress, AudioTrack>();
	}*/

	/*private void onListen(boolean listenRequest) {
		mServerListen = listenRequest;

		if (mServerListen) {
			Log.i(TAG, "startListen");

			synchronized (mState) {

				if (mState == SLEEP) {
					mWifiLock.acquire();
					new Wakeup().start();
					// byte buf[] = new byte[4096];
					// try {
					// mServerSocket.send(new DatagramPacket(buf, buf.length,
					// mBroadcastAddress, DST_PORT));
					// } catch (IOException e) {
					// e.printStackTrace();
					// }
				}

				mSilent = false;

			}
		} else {
			Log.i(TAG, "stopListen");
			synchronized (mState) {
				stopAudio();
				mSilent = getSharedPreferences(PREFS, 0).getBoolean("silent",
						false);
				if (mServerTalk == false) {
					mState = SLEEP;
					mWifiLock.release();
					new Dismiss().start();
				}

			}

		}
		Intent status = new Intent(IntercomWidget.ACTION_STATUS);
		status.putExtra("listen", mServerListen);
		status.putExtra("silent", mSilent);
		Log.v(TAG, "SendBroadcast");
		sendBroadcast(status);

	}*/

	


	private class Wakeup extends Thread {
		public final static String TAG = "Wakeup";

		// Some devices, like the Nexus Galaxy, won't wakeup to broadcast
		// packets,
		// so we send a single empty packet to every client on the subnet.
		@Override
		public void run() {
			Log.i(TAG, "Running wifi wakeup!");
			byte buf[] = new byte[2];
			buf[0] = buf[1] = 1;
			byte[] subnet = mBroadcastAddress.getAddress();
			// Log.i(TAG,String.format("subnet[0] = %x subnet[4] = %x",
			// subnet[0], subnet[3]));
			for (int i = 1; i < 254; i++) {
				subnet[3] = (byte) i;// i;
				Log.v(TAG, String.format("Wakeup %d", i));
				try {
					mServerSocket.send(new DatagramPacket(buf, 2, InetAddress
							.getByAddress(subnet), DATA_DST_PORT));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Log.d(TAG, "Running wifi wakeup done");
		}
	}

	private class Dismiss extends Thread {
		public final static String TAG = "Dismiss";

		@Override
		public void run() {
			Log.i(TAG, "Running wifi dismiss");
			byte buf[] = new byte[2];
			buf[0] = buf[1] = 0;
			try {
				mServerSocket.send(new DatagramPacket(buf, 2,
						mBroadcastAddress, DATA_DST_PORT));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public class ControlIn extends Thread {

		public final static String TAG = "ControlIn";

		public void run() {

			byte[] buf = new byte[128];

			DatagramPacket pkt = new DatagramPacket(buf, 128);

			while (!this.isInterrupted()) {

				try {
					mCtrlReceiveSocket.receive(pkt);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e(TAG, "Error receiving packet");
					return;
				}
			}
		}
	}

	private void serverAudioIn(byte[] buf, int N, double rms) throws UnknownHostException, IOException {

		if (mStartTalkBeepSent == false) {
			try {
				InputStream beepStream = getResources().openRawResource(
						R.raw.beep3);
				int len = beepStream.available();
				byte[] beepBuf = new byte[len];

				beepStream.read(beepBuf);
				mServerSocket.send(new DatagramPacket(beepBuf, len, InetAddress
						.getByName("224.0.0.10"), DATA_DST_PORT));
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "Error sending start talk beep");
			}

			mStartTalkBeepSent = true;

		} else if (mStopTalkBeepSent == false) {
			try {
				InputStream beepStream = getResources().openRawResource(
						R.raw.beep2);
				int len = beepStream.available();
				byte[] beepBuf = new byte[len];

				beepStream.read(beepBuf);
				mServerSocket.send(new DatagramPacket(beepBuf, len, InetAddress
						.getByName("224.0.0.10"), DATA_DST_PORT));
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "Error sending start talk beep");
			}

			mStopTalkBeepSent = true;
		} 
		
		if (rms > THRESHOLD && mServerTalk) {
				mServerSocket.send(new DatagramPacket(buf,
						N, InetAddress
								.getByName("224.0.0.10"),
						DATA_DST_PORT));
		}	

	}
	
	private void clientAudioIn(byte[] buf, int N, double rms) throws IOException {
		if (rms > THRESHOLD && !mPrivate) {
			// Only send small packets
			for (int i = 0; i < N; i += 256) {
				int len = Math.min(N - i, 256);
				mClientSocket.send(new DatagramPacket(
						buf, i, len, mServerAddress,
						DATA_DST_PORT));
			}
		}
	}

	public class AudioIn extends Thread {

		public final static String TAG = "AudioIn";

		@Override
		public void run() {

			try {

				while (!this.isInterrupted()) {

					byte[] buf = new byte[BUF_SIZE];
					int N = mAudioRecord.read(buf, 0, buf.length);

					if (N > 0) {

						double rms = 0;
						for (int i = 0; i < N; i += 2) {
							/*
							 * ByteBuffer bb = ByteBuffer.allocate(2);
							 * bb.order(ByteOrder.LITTLE_ENDIAN);
							 * bb.put(buf[i]); bb.put(buf[i+1]); short pcm =
							 * bb.getShort(0); rms += Math.pow(pcm, 2);
							 */
							rms += (double) buf[i + 1] * buf[i + 1];
						}
						rms = Math.sqrt(rms / N);

						Log.v(TAG, String.format(
								"Got mic buffer of %d bytes with rms = %f", N,
								rms));

						if(mState==SERVER) {
							serverAudioIn(buf, N, rms);
						}
						else if (mState==CLIENT){
							clientAudioIn(buf, N, rms);
						}
						else {
							Log.e(TAG, "Recording while sleeping");
							gotoSleep();							
						}
					} else {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "Error recording audio");
			} finally {
				mAudioRecord.stop();
			}
		}
	}
	
	private void resetTimeout() {
		mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
		mTimeoutHandler.postDelayed(mTimeoutRunnable, TIMEOUT);
	}
	
	private void wakeupAudioOut(DatagramPacket pkt) {
		if (pkt.getPort() == SERVER_DATA_DST_PORT) {
			Log.i(TAG, "Now in CLIENT mode");
			
			mState = CLIENT;
			
			mWakeLock.acquire();
			mWifiLock.acquire();
			mMulticastLock.acquire();

			Intent status = new Intent(IntercomWidget.ACTION_STATUS);
			mServerAddress = pkt.getAddress();

			if (!mSilent) {
				mBroadcastAudioTrack.flush();
				status.putExtra("listen", true);
			}

			if (!mPrivate) {
				mAudioRecord.startRecording();
				status.putExtra("talk", true);
			}

			mAudioManager.setStreamVolume(STREAM_TYPE,
					mAudioManager.getStreamMaxVolume(STREAM_TYPE),
					0);

			mStartListenBeep.start();

			sendBroadcast(status);
			
			mBroadcastAudioTrack.play();
			
		}
		else if (pkt.getPort() == CLIENT_DATA_SRC_PORT) {
			Log.e(TAG, "Got packet from client while sleeping");
		}
	}
	
	private void serverAudioOut(DatagramPacket pkt) {
		if(pkt.getPort() == SERVER_DATA_DST_PORT) {
			Log.e(TAG,"Server got packet from server\n");
			//Ignore it
		}
		else if (pkt.getPort() == CLIENT_DATA_SRC_PORT) {
			
			if (!mAudioTracks.containsKey(pkt.getAddress())) {
				Log.i(TAG, "open new track for "
						+ pkt.getAddress().toString());
				AudioTrack track = initializeAudioOut();
				track.play();
				mAudioTracks.put(pkt.getAddress(), track);
			}
			if (mServerListen) {
				// Log.i(TAG,mAudioTracks.get(pkt.getAddress()).getS)
				AudioTrack track = mAudioTracks.get(pkt.getAddress());
				track.write(pkt.getData(), 0, pkt.getLength());
			}
		}
		
	}

	private void clientAudioOut(DatagramPacket pkt){
		
		if (pkt.getPort() == SERVER_DATA_DST_PORT) {
			
			if (mState == CLIENT) {
				Log.v(TAG, "In client mode, got packet from server");
				
				resetTimeout();

				// Delay response to cut down on traffic
				mAudioRecord.stop();
				mResponseDelayHandler
						.removeCallbacks(mResponseDelayRunnable);
				mResponseDelayHandler.postDelayed(
						mResponseDelayRunnable, RESPONSE_DELAY);

				if (!mSilent && pkt.getLength() > 2) {
					mBroadcastAudioTrack.write(pkt.getData(), 0,
							pkt.getLength());
													// the last buffer
				} else if (pkt.getLength() == 2) {
					if (pkt.getData()[0] == 0) {
						gotoSleep();
					} else if (pkt.getData()[0] == 1) {

					}
				}
			} 
			
		}
		else if (pkt.getPort() == CLIENT_DATA_SRC_PORT) {
			Log.w(TAG,
					"Received client packets while in client mode, ignoring");
		}
	}
	public class AudioOut extends Thread {

		public final static String TAG = "AudioOut";

		@Override
		public void run() {

			byte[] buf = new byte[BUF_SIZE];

			DatagramPacket pkt = new DatagramPacket(buf, BUF_SIZE);

			while (!this.isInterrupted()) {

				try {
					mDataReceiveSocket.receive(pkt);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e(TAG, "Error receiving packet");
					return;
				}

				if (pkt.getAddress().equals(mLocalAddress))
					continue;

				Log.v(TAG, String.format(
						"Received %d byte udp packet from %s local=%s port=%d",
						pkt.getLength(), pkt.getAddress().toString(),
						mLocalAddress.toString(), pkt.getPort()));

				if(mState==SERVER) {
					serverAudioOut(pkt);
				}
				else if(mState==CLIENT) {
					clientAudioOut(pkt);
				}
				else if (mState==SLEEP) {
					wakeupAudioOut(pkt);
				}
				// synchronized (mState) {
			}
		}
	}

	class TimeoutRunnable implements Runnable {

		public void run() {
			Log.i(TAG, "TimeOut");
			gotoSleep();
		}

	};

	void gotoSleep() {
		synchronized (mState) {
			// if (mState != SLEEP) {
			mState = SLEEP;
			closeAudio();
			mAudioRecord.stop();
			if (mWakeLock.isHeld()) {
				mWakeLock.release();
			}
			if (mWifiLock.isHeld()) {
				mWifiLock.release();
			}
			if (mMulticastLock.isHeld()) {
				mMulticastLock.release();
			}
			Intent status = new Intent(IntercomWidget.ACTION_STATUS);
			mServerTalk = mServerListen = false;
			status.putExtra("talk", mServerTalk);
			status.putExtra("listen", mServerListen);
			sendBroadcast(status);
		}
	}

	class ResponseDelayRunnable implements Runnable {

		public void run() {
			Log.i(TAG, "ResponseDelayRunnable");
			synchronized (mState) {
				mAudioRecord.startRecording();
			}
		}
	};

	public InetAddress getLocalAddress() throws IOException {
		WifiManager wifi = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		int local = wifi.getDhcpInfo().ipAddress;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
			quads[k] = (byte) ((local >> k * 8) & 0xFF);
		return InetAddress.getByAddress(quads);
	}

	public InetAddress getBroadcastAddress() throws IOException {
		WifiManager wifi = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();
		// handle null somehow

		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		return InetAddress.getByAddress(quads);
	}

	public void beep() {
		Uri soundUri = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		MediaPlayer mMediaPlayer = new MediaPlayer();
		try {
			mMediaPlayer.setDataSource(getApplicationContext(), soundUri);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final AudioManager audioManager = (AudioManager) getApplicationContext()
				.getSystemService(Context.AUDIO_SERVICE);
		if (audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) != 0) {
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
			mMediaPlayer.setLooping(false);
			try {
				mMediaPlayer.prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mMediaPlayer.start();
		}
	}

	private AudioTrack initializeAudioOut() {
		int minBufSize = AudioTrack.getMinBufferSize(8000,
				AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

		Log.w(TAG, String.format("AudioOut minBufSize %d", minBufSize));
		assert BUF_SIZE >= minBufSize;

		return new AudioTrack(STREAM_TYPE, 8000, AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT, minBufSize,
				AudioTrack.MODE_STREAM);

	}

	private boolean initializeAudioIn() {
		int minBufSize = AudioRecord.getMinBufferSize(8000,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

		Log.w(TAG, String.format("AudioIn minBufSize %d", minBufSize));
		assert BUF_SIZE >= minBufSize;

		mAudioRecord = new AudioRecord(AudioSource.VOICE_COMMUNICATION, 8000,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
				minBufSize);

		return true;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
