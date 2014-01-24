package com.theultimatelabs.sonic;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera.Size;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SonicActivity extends Activity {

	// private AudioTrack track;

	private int sampleRate;

	private int minBufSize;

	private double period;

	private int samples;

	final int SAMPLE_RATE = 44100;

	private TextView textView;

	private EditText editText;

	final double IN_TONES[] = { 20231, 18000 };// { 16700, 18100,
												// 17400 };
	final double OUT_TONES[] = { 19231, 18000 };// { 16700, 18100,
												// 17400 };
	final double OUT_DIFF = (OUT_TONES[1] - OUT_TONES[0]);

	final double SAMPLES_MS = (SAMPLE_RATE / 1000);

	public final int STREAM = AudioManager.STREAM_ALARM;

	// private AudioTrack track;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.period = 1.0;
		this.sampleRate = 44100; // AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
		log.v("SampleRate: %d", this.sampleRate);

		this.setVolumeControlStream(this.STREAM);

		this.minBufSize = AudioTrack.getMinBufferSize(this.sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		log.v("minBufsize=%d minPeriod=%f", this.minBufSize, ((float) this.minBufSize) / this.sampleRate);

		this.samples = (int) Math.max(this.period * this.sampleRate, this.minBufSize);

		/*
		 * this.track = new AudioTrack(this.STREAM, this.sampleRate,
		 * AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
		 * this.samples* 2, AudioTrack.MODE_STATIC);
		 */

		new AudioInTask().execute();

		textView = (TextView) findViewById(R.id.textView1);
		textView.setText("");
		editText = (EditText) findViewById(R.id.editText1);
		editText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if(s.length() == 0) return;
				char l = s.subSequence(s.length() - 1, s.length()).charAt(0);
				byte[] msg = new byte[] { (byte) l };
				FMSimpleSend(msg);// (byte) l, (byte) l, (byte) l, (byte) l);
				log.i("send done");
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});

		((Button) findViewById(R.id.button1)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// sineWave(100,size);
				// sineWave(200,size);
				// DTMF((byte)0xA4);
				// DTMFTest(50);
				byte[] msg = new byte[] { 0x55 };
				FMSimpleSend(msg);
				textView.setText("");
				editText.setText("");
				// 0x78);
				// FMTest();
				// sleep(3);
				// FMSend((byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0);
				// sleep(3);
				// FMSend((byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78);
				// sleep(3);
				// sineWaveTest2();
				// sineWave(400,size);
				// sineWave(500,size);
				// sineWave(600,size);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void setVolume(int volume) {
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int maxVolume = am.getStreamMaxVolume(this.STREAM);
		am.setStreamVolume(this.STREAM, (int) (maxVolume * ((float) volume / 100)), 0);
	}

	private void sleep(int period) {
		try {
			Thread.sleep(period);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	Runnable audioOutRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub

		}

	};

	/* table of Hamming codes hammingCodes[x] is the x encoded */
	final byte hammingEncode[] = { 0x00, /* 0 */
	0x71, /* 1 */
	0x62, /* 2 */
	0x13, /* 3 */
	0x54, /* 4 */
	0x25, /* 5 */
	0x36, /* 6 */
	0x47, /* 7 */
	0x38, /* 8 */
	0x49, /* 9 */
	0x5A, /* A */
	0x2B, /* B */
	0x6C, /* C */
	0x1D, /* D */
	0x0E, /* E */
	0x7F /* F */
	};

	static final int crc_table[] = { 0, 94, 188, 226, 97, 63, 221, 131, 194, 156, 126, 32, 163, 253, 31, 65, 157, 195, 33, 127, 252, 162, 64, 30, 95, 1, 227,
			189, 62, 96, 130, 220, 35, 125, 159, 193, 66, 28, 254, 160, 225, 191, 93, 3, 128, 222, 60, 98, 190, 224, 2, 92, 223, 129, 99, 61, 124, 34, 192,
			158, 29, 67, 161, 255, 70, 24, 250, 164, 39, 121, 155, 197, 132, 218, 56, 102, 229, 187, 89, 7, 219, 133, 103, 57, 186, 228, 6, 88, 25, 71, 165,
			251, 120, 38, 196, 154, 101, 59, 217, 135, 4, 90, 184, 230, 167, 249, 27, 69, 198, 152, 122, 36, 248, 166, 68, 26, 153, 199, 37, 123, 58, 100, 134,
			216, 91, 5, 231, 185, 140, 210, 48, 110, 237, 179, 81, 15, 78, 16, 242, 172, 47, 113, 147, 205, 17, 79, 173, 243, 112, 46, 204, 146, 211, 141, 111,
			49, 178, 236, 14, 80, 175, 241, 19, 77, 206, 144, 114, 44, 109, 51, 209, 143, 12, 82, 176, 238, 50, 108, 142, 208, 83, 13, 239, 177, 240, 174, 76,
			18, 145, 207, 45, 115, 202, 148, 118, 40, 171, 245, 23, 73, 8, 86, 180, 234, 105, 55, 213, 139, 87, 9, 235, 181, 54, 104, 138, 212, 149, 203, 41,
			119, 244, 170, 72, 22, 233, 183, 85, 11, 136, 214, 52, 106, 43, 117, 151, 201, 74, 20, 246, 168, 116, 42, 200, 150, 21, 75, 169, 247, 182, 232, 10,
			84, 215, 137, 107, 53 };

	byte crc8(byte msg[]) {
		byte crc = 0;
		for (byte b : msg) {
			crc = (byte) crc_table[(crc ^ b)];
		}
		return crc;
	}

	void FMTest() {
		short buffer[] = new short[SAMPLE_RATE * 4];
		final float SAMPLES_MS = (SAMPLE_RATE / 1000);
		int bufi = 0;
		double angle = 0;

		for (int s = 0; s < 1000 * SAMPLES_MS; s++) {
			angle += 2 * Math.PI * (OUT_TONES[0] / (double) SAMPLE_RATE);
			buffer[bufi++] = (short) (Math.sin(angle) * (Short.MAX_VALUE));
		}

		for (int f = 0; f < 100; f++) {
			for (int s = 0; s < 10 * SAMPLES_MS; s++) {
				if ((f % 2) == 0) {
					angle += 2 * Math.PI * (OUT_TONES[0] + (OUT_DIFF * s / (10 * SAMPLES_MS))) / SAMPLE_RATE;
				} else {
					angle += 2 * Math.PI * (OUT_TONES[1] - (OUT_DIFF * s / (10 * SAMPLES_MS))) / SAMPLE_RATE;
				}

				buffer[bufi++] = (short) (Math.sin(angle) * (Short.MAX_VALUE));
			}
		}

		for (int s = 0; s < 1000 * SAMPLES_MS; s++) {
			angle += 2 * Math.PI * (OUT_TONES[1] / (double) SAMPLE_RATE);
			buffer[bufi++] = (short) (Math.sin(angle) * (Short.MAX_VALUE));
		}

		AudioTrack track = new AudioTrack(this.STREAM, this.sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufi * 2,
				AudioTrack.MODE_STATIC);
		track.write(buffer, 0, bufi); // write to the audio buffer....

		// and start all over again!
		track.play();

		while (track.getPlaybackHeadPosition() < bufi) {
			sleep(1);
		}
		track.stop();
		// this.track.flush();
		track.release();
	}

	void FMSimpleSend(byte msg[]) {
		short buffer[] = new short[SAMPLE_RATE * 6];

		// byte crc = (byte) ((id + cmd + d1 + d2) & 0xff);
		// byte msg[] = { id, cmd, d1, d2, crc };
		final int BIT_PERIOD = 8;
		final int START_FADE = 1;
		final int STOP_FADE = 1;
		double angle = 0;
		int bufi = 0;

		for (int s = 0; s < START_FADE * SAMPLES_MS * BIT_PERIOD; s++) {
			angle += 2 * Math.PI * (OUT_TONES[1] / SAMPLE_RATE);
			buffer[bufi++] = (short) (Math.sin(angle) * (Short.MAX_VALUE * ((double) s / (START_FADE * SAMPLES_MS * BIT_PERIOD))));
		}

		for (byte m : msg) {
			for (int b = 0; b < 8; b++) {
				boolean bit = ((m >> b) & 0x1) != 0;
				int periodHigh = (BIT_PERIOD / 2);
				int periodLow = bit ? BIT_PERIOD * 2 : (BIT_PERIOD / 2);

				for (int s = 0; s < SAMPLES_MS * periodHigh; s++) {
					angle += 2 * Math.PI * (OUT_TONES[0] / SAMPLE_RATE);
					buffer[bufi++] = (short) (Math.sin(angle) * (Short.MAX_VALUE));
				}
				for (int s = 0; s < SAMPLES_MS * periodLow; s++) {
					angle += 2 * Math.PI * (OUT_TONES[1] / SAMPLE_RATE);
					buffer[bufi++] = (short) (Math.sin(angle) * (Short.MAX_VALUE));
				}
			}
		}

		for (int s = 0; s < STOP_FADE * SAMPLES_MS * BIT_PERIOD; s++) {
			angle += 2 * Math.PI * (OUT_TONES[0] / SAMPLE_RATE);
			buffer[bufi++] = (short) (Math.sin(angle) * (Short.MAX_VALUE * (1.0 - (double) s / (STOP_FADE * SAMPLES_MS * BIT_PERIOD))));
		}

		AudioTrack track = new AudioTrack(this.STREAM, this.sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufi * 2,
				AudioTrack.MODE_STATIC);
		track.write(buffer, 0, bufi); // write to the audio buffer....

		// and start all over again!
		track.play();

		while (track.getPlaybackHeadPosition() < bufi) {
			sleep(1);
		}
		track.stop();
		// this.track.flush();
		track.release();

	}

	private class AudioInTask extends AsyncTask<Void, Character, Void> {

		protected void onProgressUpdate(Character... progress) {
			log.i("Progress: %c",progress[0]);
			textView.append(String.valueOf(progress[0]));
		}

		protected void onPostExecute(Void... arg0) {

			// showDialog("Downloaded " + result + " bytes");
		}

		byte biti = 0;
		long stamp = 0;

		final int IDLE = 0;
		final int START = 1;
		final int HIGH_PULSE = 2;
		final int LOW_PULSE = 3;
		final int DONE = 4;
		int state = IDLE;

		/* table of Hamming codes hammingCodes[x] is the x encoded */
		final byte hammingEncode[] = { 0x00, /* 0 */
		0x71, /* 1 */
		0x62, /* 2 */
		0x13, /* 3 */
		0x54, /* 4 */
		0x25, /* 5 */
		0x36, /* 6 */
		0x47, /* 7 */
		0x38, /* 8 */
		0x49, /* 9 */
		0x5A, /* A */
		0x2B, /* B */
		0x6C, /* C */
		0x1D, /* D */
		0x0E, /* E */
		0x7F /* F */
		};

		/* table convering encoded value (with error) to original data */
		/* hammingDecodeValues[code] = original data */
		final byte hammingDecode[] = { 0x00, 0x00, 0x00, 0x03, 0x00, 0x05, 0x0E, 0x07, /*
																						 * 0x00
																						 * to
																						 * 0x07
																						 */
		0x00, 0x09, 0x0E, 0x0B, 0x0E, 0x0D, 0x0E, 0x0E, /* 0x08 to 0x0F */
		0x00, 0x03, 0x03, 0x03, 0x04, 0x0D, 0x06, 0x03, /* 0x10 to 0x17 */
		0x08, 0x0D, 0x0A, 0x03, 0x0D, 0x0D, 0x0E, 0x0D, /* 0x18 to 0x1F */
		0x00, 0x05, 0x02, 0x0B, 0x05, 0x05, 0x06, 0x05, /* 0x20 to 0x27 */
		0x08, 0x0B, 0x0B, 0x0B, 0x0C, 0x05, 0x0E, 0x0B, /* 0x28 to 0x2F */
		0x08, 0x01, 0x06, 0x03, 0x06, 0x05, 0x06, 0x06, /* 0x30 to 0x37 */
		0x08, 0x08, 0x08, 0x0B, 0x08, 0x0D, 0x06, 0x0F, /* 0x38 to 0x3F */
		0x00, 0x09, 0x02, 0x07, 0x04, 0x07, 0x07, 0x07, /* 0x40 to 0x47 */
		0x09, 0x09, 0x0A, 0x09, 0x0C, 0x09, 0x0E, 0x07, /* 0x48 to 0x4F */
		0x04, 0x01, 0x0A, 0x03, 0x04, 0x04, 0x04, 0x07, /* 0x50 to 0x57 */
		0x0A, 0x09, 0x0A, 0x0A, 0x04, 0x0D, 0x0A, 0x0F, /* 0x58 to 0x5F */
		0x02, 0x01, 0x02, 0x02, 0x0C, 0x05, 0x02, 0x07, /* 0x60 to 0x67 */
		0x0C, 0x09, 0x02, 0x0B, 0x0C, 0x0C, 0x0C, 0x0F, /* 0x68 to 0x6F */
		0x01, 0x01, 0x02, 0x01, 0x04, 0x01, 0x06, 0x0F, /* 0x70 to 0x77 */
		0x08, 0x01, 0x0A, 0x0F, 0x0C, 0x0F, 0x0F, 0x0F /* 0x78 to 0x7F */
		};

		final int WINDOW_SIZE = 128;
		final int BIT_PERIOD = 8;
		final double STAMP_MS = (float) WINDOW_SIZE / (SAMPLE_RATE / 1000);
		long lastStamp = 0;
		int bank = 0;
		int bytei = 0;

		class Msg {
			boolean ready = false;
			byte[] bytes = new byte[10];
			long stamp = 0;
			int len = 0;
		}

		Msg[] msgs = new Msg[10];

		double goertzelSimple(final short[] buffer, final int bufferOffset, final double COEFF) {

			double Q1 = 0, Q2 = 0;
			for (int s = 0; s < WINDOW_SIZE; s++) {
				double Q0 = COEFF * Q1 - Q2 + buffer[bufferOffset + s];
				Q2 = Q1;
				Q1 = Q0;
			}
			return Math.sqrt(Q1 * Q1 + Q2 * Q2 - COEFF * Q1 * Q2);

		}

		boolean lastSmpl = false;
		byte block = 0;

		void FMSimpleRecv(boolean smpl) {

			double diff = (stamp - lastStamp) * STAMP_MS;

			if (state != IDLE && diff > BIT_PERIOD * 4) {
				state = IDLE;
				if(bytei>0) {
					msgs[bank].len = bytei;
					msgs[bank].ready = true;
					log.v("Block ready | len=%d bank=%d", bytei, bank);
					this.publishProgress(new Character((char)msgs[bank].bytes[0]));
					bank = (bank + 1) % msgs[bank].bytes.length;
					msgs[bank].ready = false;
				}
				log.w("reset");
				bytei = block = biti = 0;
			}

			if (lastSmpl != smpl) {
				//log.v("smpl=%d diff=%f biti=%d block=%x", smpl ? 1 : 0, diff, biti, block);
				lastSmpl = smpl;
				switch (state) {
				case IDLE:
					if (smpl == true) {
						lastStamp = stamp;
						state = HIGH_PULSE;
					}
					break;
				case LOW_PULSE:
					// newBit = 0;
					log.v("diff=%f biti=%d magRng=%f-%f", diff, biti,maxMag,minMag);
					maxMag = 0;
					minMag = 99999999999999.9;
					if (diff > BIT_PERIOD * 2.75) {
						// newBit = 1;
						block |= 1 << biti;
						//log.v("+1 %x", block);
					}
					lastStamp = stamp;
					state = HIGH_PULSE;
					
					if (++biti == 8) {
						state = HIGH_PULSE;
						log.i("Byte:%x", block);
						msgs[bank].bytes[bytei++] = block;
						block = biti = 0;
						// if(bytei == bytej) Serial.println("TOO SLOW");
					}
					break;
				case HIGH_PULSE:
					state = LOW_PULSE;
					break;
				}
			}
		}

		double maxMag=0;
		double minMag=9999999999999.9;
		@Override
		protected Void doInBackground(Void... arg0) {

			log.v("audioIn thread started");

			final int N = 4096;// AudioRecord.getMinBufferSize(SAMPLE_RATE,
								// AudioFormat.CHANNEL_IN_MONO,
								// AudioFormat.ENCODING_PCM_16BIT);
			short buffer[] = new short[N];

			log.v("N=%d", N);

			AudioRecord recorder = new AudioRecord(AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, N * 2);
			recorder.startRecording();

			final double COEFF[] = new double[IN_TONES.length];

			for (int i = 0; i < IN_TONES.length; i++) {
				COEFF[i] = 2.0 * Math.cos(2 * Math.PI * IN_TONES[i] / SAMPLE_RATE);
			}

			for (int i = 0; i < msgs.length; i++) {
				msgs[i] = new Msg();
			}

			final double ON_THRESHOLD = 8000;//16384;
			final double OFF_THRESHOLD = ON_THRESHOLD * 1 / 2;

			while (true) {

				int read = 0;
				while (read < N) {
					read += recorder.read(buffer, read, N - read);
				}

				for (int j = 0; j < read / WINDOW_SIZE; j++) {
					stamp++;
					double mag = goertzelSimple(buffer, j * WINDOW_SIZE, COEFF[0]);
					if (mag > maxMag) maxMag = mag;
					if(mag < minMag) minMag = mag;
					if (mag > ON_THRESHOLD) {
						// log.v("mag = %f",mag);
						FMSimpleRecv(true);
					} else if (mag < OFF_THRESHOLD) {
						FMSimpleRecv(false);
					}

				}
			}

			// recorder.stop();
			// recorder.release();
			// return null;
		}
	}
}