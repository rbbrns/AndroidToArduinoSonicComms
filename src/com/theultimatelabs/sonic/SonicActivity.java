package com.theultimatelabs.sonic;

import android.app.Activity;
import android.content.Context;
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

	final double IN_TONES[] = { 16977, 18000 };// { 16700, 18100,
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
				char l = s.subSequence(s.length() - 1, s.length()).charAt(0);
				byte[] msg = new byte[]{(byte)l};
				FMSimpleSend(msg);//(byte) l, (byte) l, (byte) l, (byte) l);
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
				byte[] msg = new byte[]{0x55}; 
				FMSimpleSend(msg);
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
		final int BIT_PERIOD = 10;
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
				int periodHigh = b==0 ? BIT_PERIOD*2 : (BIT_PERIOD / 2);
				int periodLow = bit ? BIT_PERIOD*3/2 :  (BIT_PERIOD / 2);
				
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

	void FMSend(byte id, byte cmd, byte d1, byte d2) {
		short buffer[] = new short[SAMPLE_RATE * 2];
		byte crc = (byte) ((id + cmd + d1 + d2) & 0xff);
		byte msg[] = { id, cmd, d1, d2, crc };
		final int START_FADE = 3;
		final int PAUSE = 3;
		final int STOP_FADE = 3;
		final int BIT_PERIOD = 10;
		// final int BYTE_SAMPLES = buffer.length / (msg.length);
		// final int BIT_SAMPLES = buffer.length / ((msg.length) * (8 +
		// START_BITS + STOP_BITS) + START_FADE + STOP_FADE);
		final float SAMPLES_MS = (SAMPLE_RATE / 1000);
		double angle = 0;
		int bufi = 0;
		int tone = 0;

		for (int s = 0; s < START_FADE * SAMPLES_MS * BIT_PERIOD; s++) {
			angle += 2 * Math.PI * (OUT_TONES[tone] / SAMPLE_RATE);
			buffer[bufi++] = (short) (Math.sin(angle) * (Short.MAX_VALUE * ((float) s / (START_FADE * SAMPLES_MS * BIT_PERIOD))));
		}

		for (int m = 0; m < msg.length; m++) {
			for (int h = 0; h < 2; h++) {
				byte hamming = hammingEncode[(msg[m] >> (h * 4)) & 0xf];
				if (tone != 0)
					log.e("ERROR tone should be 0");
				for (int b = 0; b < 8; b++) {
					tone ^= 1;
					boolean bit = ((hamming >> b) & 0x1) != 0;
					/*
					 * for (int f = 0; f < SAMPLES_MS * BIT_PERIOD; f++) { angle
					 * += 2 * Math.PI * (OUT_TONES[tone ^ 1] + (tone == 1 ? 1 :
					 * -1) * (OUT_TONES[1] - OUT_TONES[0]) * (f) / (SAMPLES_MS *
					 * BIT_PERIOD)) / SAMPLE_RATE; buffer[bufi++] = (short)
					 * (Math.sin(angle) * (Short.MAX_VALUE)); // buffer[bufi++]
					 * = (short) (Math.sin(angle) * // (Short.MAX_VALUE *
					 * ((float) f / // (SAMPLES_MS*BIT_PERIOD)))); }
					 */
					for (int i = 0; i < SAMPLES_MS * BIT_PERIOD * (bit ? 2 : 1); i++) {
						angle += 2 * Math.PI * OUT_TONES[tone] / SAMPLE_RATE;
						buffer[bufi++] = (short) (Math.sin(angle) * (Short.MAX_VALUE));
					}
				}
				for (int s = 0; s < SAMPLES_MS * PAUSE * BIT_PERIOD; s++) {
					angle += 2 * Math.PI * OUT_TONES[tone] / SAMPLE_RATE;
					buffer[bufi++] = (short) (Math.sin(angle) * (Short.MAX_VALUE));
				}
			}
		}

		if (tone != 0)
			log.e("ERROR tone should be 0");

		for (int s = 0; s < STOP_FADE * SAMPLES_MS * BIT_PERIOD; s++) {
			angle += 2 * Math.PI * OUT_TONES[tone] / SAMPLE_RATE;
			buffer[bufi++] = (short) (Math.sin(angle) * (Short.MAX_VALUE * (1.0 - (float) s / (STOP_FADE * SAMPLES_MS * BIT_PERIOD))));
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

	// ID,CMD,D1,D2,CRC
	public void FM2(byte id, byte cmd, byte d1, byte d2) {
		// setVolume(85);
		short buffer[] = new short[this.samples];
		byte crc = (byte) ((id + cmd + d1 + d2) & 0xff);
		byte msg[] = { id, cmd, d1, d2, crc };
		final int START_BITS = 0;
		final int STOP_BITS = 0;
		final int START_FADE = 1;
		final int STOP_FADE = 1;
		final int BYTE_SAMPLES = buffer.length / (msg.length);
		final int BIT_SAMPLES = buffer.length / ((msg.length) * (8 + START_BITS + STOP_BITS) + START_FADE + STOP_FADE);

		double angle = 0;
		int bufi = 0;

		// FADE IN
		for (int s = 0; s < START_FADE * BIT_SAMPLES; s++) {
			angle += 2 * Math.PI * IN_TONES[1] / this.sampleRate;

			buffer[bufi++] = (short) (Math.sin(angle) * (Short.MAX_VALUE * ((float) s / (BIT_SAMPLES))));
		}

		int tone = 1;
		for (int m = 0; m < msg.length; m++) {
			for (int b = -START_BITS; b < 8 + STOP_BITS; b++) {
				boolean bit = ((msg[m] >> b) & 0x1) != 0;
				if (b <= -1) {
					tone = 1;
				} else if (b >= 8) {
					tone = tone;
				} else if (tone == 0) {
					tone = bit ? 2 : 1;
				} else if (tone == 1) {
					tone = bit ? 2 : 0;
				} else if (tone == 2) {
					tone = bit ? 1 : 0;
				}
				for (int s = 0; s < BIT_SAMPLES; s++) {
					if (b >= 8)
						angle = angle; // 0;
					else
						angle += 2 * Math.PI * IN_TONES[tone] / this.sampleRate;

					buffer[bufi++] = (short) ((Math.sin(angle)) * Short.MAX_VALUE);
				}
			}
		}
		// FADE OUT
		for (int s = 0; s < BIT_SAMPLES * STOP_FADE; s++) {
			angle += 2 * Math.PI * IN_TONES[tone] / this.sampleRate;

			buffer[bufi++] = (short) (Math.sin(angle) * (Short.MAX_VALUE * (1.0 - (float) s / (BIT_SAMPLES))));
		}

		AudioTrack track = new AudioTrack(this.STREAM, this.sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer.length * 2,
				AudioTrack.MODE_STATIC);
		track.write(buffer, 0, buffer.length); // write to the audio buffer....
												// and start all over again!
		track.play();

		while (track.getPlaybackHeadPosition() < buffer.length) {
			sleep(1);
		}
		track.stop();
		// this.track.flush();
		track.release();

	}

	public void FM1(byte x) {
		setVolume(100);
		short buffer[] = new short[this.samples];

		double angle1 = 0;
		for (int k = 0; k < 3; k++) {
			int tone = 0;
			for (int j = -2; j < 8; j++) {
				boolean b = ((x >> j) & 0x1) != 0;
				if (j < 0) {
					tone = 0;
				} else if (tone == 0) {
					tone = b ? 3 : 1;
				} else if (tone == 1) {
					tone = b ? 3 : 2;
				} else if (tone == 2) {
					tone = b ? 3 : 1;
				} else if (tone == 3) {
					tone = b ? 2 : 1;
				}
				for (int i = 0; i < buffer.length / (3 * 10); i++) {
					buffer[(k * buffer.length / 3) + (j + 2) * buffer.length / (3 * 10) + i] = (short) ((Math.sin(angle1)) * Short.MAX_VALUE);
					angle1 += 2 * Math.PI * IN_TONES[tone] / this.sampleRate;
					// angle2 += 2 * Math.PI * DTMF_TONES[(tone + 2) % 3] /
					// this.sampleRate;
				}
			}
		}

		AudioTrack track = new AudioTrack(this.STREAM, this.sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer.length * 2,
				AudioTrack.MODE_STATIC);
		track.write(buffer, 0, buffer.length); // write to the audio buffer....
												// and start all over again!
		track.play();

		while (track.getPlaybackHeadPosition() < buffer.length) {
			sleep(1);
		}
		track.stop();
		track.release();

	}

	public void DTMF(byte x) {
		setVolume(100);

		short buffer[] = new short[this.samples];
		double angle1 = 0;
		double angle2 = 0;
		int tone = 1;
		for (int j = -1; j < 8; j++) {
			boolean b = ((x >> j) & 0x1) != 0;
			if (j == -1) {
				tone = 1;
			} else if (tone == 0) {
				tone = b ? 2 : 1;
			} else if (tone == 1) {
				tone = b ? 2 : 0;
			} else if (tone == 2) {
				tone = b ? 1 : 0;
			}
			for (int i = 0; i < buffer.length / 9; i++) {
				// buffer[(j+1)*buffer.length/9 + i] = (short)
				// ((Math.sin(2*Math.PI * i * DTMF_TONES[tone] /
				// this.sampleRate)/2 +
				// Math.sin(2*Math.PI * i * DTMF_TONES[(tone+2)%3] /
				// this.sampleRate)/2)* Short.MAX_VALUE);
				buffer[(j + 1) * buffer.length / 9 + i] = (short) ((Math.sin(angle1) / 2 + Math.sin(angle2) / 2) * Short.MAX_VALUE);
				angle1 += 2 * Math.PI * IN_TONES[tone] / this.sampleRate;
				angle2 += 2 * Math.PI * IN_TONES[(tone + 2) % 3] / this.sampleRate;
			}
		}

		AudioTrack track = new AudioTrack(this.STREAM, this.sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer.length * 2,
				AudioTrack.MODE_STATIC);
		track.write(buffer, 0, buffer.length); // write to the audio buffer....
												// and start all over again!
		track.play();

		while (track.getPlaybackHeadPosition() < buffer.length) {
			sleep(1);
		}
		track.stop();
		track.release();

	}

	private class AudioInTask extends AsyncTask<Void, Character, Void> {

		protected void onProgressUpdate(Character... progress) {

			textView.append(String.valueOf(progress[0]));
		}

		protected void onPostExecute(Void... arg0) {

			// showDialog("Downloaded " + result + " bytes");
		}

		byte bytes[] = new byte[5 * 2];
		byte biti = 0;
		int bytei = bytes.length - 1;
		long stamp = 0;

		final int IDLE = 0;
		final int START = 1;
		final int HIGH_PULSE = 2;
		final int LOW_PULSE = 3;
		final int STOP = 4;
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
		final int BIT_PERIOD = 10;
		final int THRESHOLD = 333;
		final float STAMP_MS = (float) WINDOW_SIZE / (SAMPLE_RATE / 1000);
		long lastStamp;

		public void processNewSample(int newSample, double mag) {

			// log.v("newSample %d %f", newSample, (stamp - lastStamp) *
			// STAMP_MS);

			if ((stamp - lastStamp) * STAMP_MS > (BIT_PERIOD * 5)) {
				state = IDLE;
				bytei = 0;
			}

			switch (state) {
			case IDLE:
				if (newSample == 0) {
					state = START;
					log.d("DIAL: %f", (stamp - lastStamp) * STAMP_MS);
					lastStamp = stamp;
				}
				break;
			case START:
				biti = 0;
				bytes[bytei] = 0;
				if (newSample == 1) {
					if ((stamp - lastStamp) * STAMP_MS > (BIT_PERIOD * 2)) {
						log.d("START: %f", (stamp - lastStamp) * STAMP_MS);
						state = HIGH_PULSE;
					}
					lastStamp = stamp;
				}
				break;
			case HIGH_PULSE:
			case LOW_PULSE:
				if ((state == HIGH_PULSE && newSample == 0) || (state == LOW_PULSE && newSample == 1)) {
					log.d("PULSE: %f", (stamp - lastStamp) * STAMP_MS);
					if ((stamp - lastStamp) * STAMP_MS > (BIT_PERIOD * 3) / 2) {
						bytes[bytei] |= 1 << biti;
					} else {
						bytes[bytei] &= ~(1 << biti);
					}
					state = state == HIGH_PULSE ? LOW_PULSE : HIGH_PULSE;
					if (++biti == 7) {
						state = newSample == 0 ? START : IDLE;

						if ((bytes[bytei] & 0xF) != hammingDecode[bytes[bytei]])
							log.w("Error corrected:");
						log.i("%d:%x->%x", bytei, bytes[bytei], hammingDecode[bytes[bytei]]);
						bytes[bytei] = hammingDecode[bytes[bytei]];
						if (bytei + 1 == bytes.length) {
							for (int i = 0; i < bytes.length / 2; i++) {
								log.i("msg[%d]=%x", i, bytes[i * 2] | (bytes[i * 2 + 1] << 4));
								if (i == 1)
									publishProgress(new Character((char) (bytes[i * 2] | (bytes[i * 2 + 1] << 4))));
							}
						}
						bytei = (bytei + 1) % bytes.length;
					}
					lastStamp = stamp;
				}
				break;
			}

		}

		public void processNewState(int newState) {

			if (newState == state)
				return;

			if (newState == -1) {
				/*
				 * if (bytei >= 2) { log.i("byte[0]=%x byte[1]=%x byte[2]=%x",
				 * bytes[0], bytes[1], bytes[2]); log.i("%x", (bytes[0] &
				 * bytes[1] | bytes[0] & bytes[2] | bytes[1] & bytes[2]) &
				 * 0xff); }
				 */
				// log.i("%x",bytes[0]);
				// log.v("biti=%d bytei=%d",biti,bytei);
				log.d("reset");
				biti = -1;

				// bytei = (bytei + 1) % bytes.length;

				bytei = 0;

			} else if (newState == 0) {
				// log.v("%d->%d=%d",state,newState,0);
				bytes[bytei] &= ~(1 << biti);
			} else if (newState == 2) {
				bytes[bytei] |= 1 << biti;
			} else if (newState == 1) {
				if (state == -1) {
					// log.w("invalid bit");
					biti = -1;
				} else if (state == 0) {
					bytes[bytei] &= ~(1 << biti);
				} else if (state == 2) {
					bytes[bytei] |= 1 << biti;
				}
			}

			if (++biti == 8) {
				log.d("byte[%d]=%x", bytei, bytes[bytei]);
				biti = 0;
				bytei = (bytei + 1) % bytes.length;
				if (bytei == 0) {
					int sum = 0;
					for (int i = 0; i < bytes.length - 1; i++) {
						sum += bytes[i];
					}
					byte calcsum = (byte) (sum & 0xff);
					byte rcvdsum = bytes[bytes.length - 1];
					if (calcsum == rcvdsum) {
						log.i("pass");
						publishProgress(new Character((char) bytes[1]));
					} else {
						log.i("fail expecting %x got %x", calcsum, rcvdsum);
					}
					for (int i = 0; i < bytes.length; i++) {
						bytes[i] = 0;
					}

				}
			}
			state = newState;
		}

		int goertzelEmbedded(final int j, final short[] buffer, double[] mag, double[] Q1, double[] Q2, final double[] COEFF) {

			int maxi = 0;
			for (int i = 0; i < IN_TONES.length; i++) {
				Q1[i] = Q2[i] = 0;
				// avg[i] = 1;
				for (int k = 0; k < WINDOW_SIZE; k++) {
					// avg[i] += buffer[j];
					// log.v("%d", buffer[j * WINDOW_SIZE + k]);
					int sample = (buffer[j * WINDOW_SIZE + k] - Short.MIN_VALUE) >> 8;

					double Q0 = -1 * (Q1[i] + ((int) Q1[i] >> 2)) - Q2[i] + sample;
					Q2[i] = Q1[i];
					Q1[i] = Q0;
				}
				mag[i] = Math.sqrt(Q1[i] * Q1[i] + Q2[i] * Q2[i] - COEFF[i] * Q1[i] * Q2[i]);
				if (mag[i] > mag[maxi])
					maxi = i;
			}
			return maxi;
		}

		int maxSample = 0;
		int minSample = 0;

		int goertzel(final int j, final short[] buffer, double[] mag, double[] Q1, double[] Q2, final double[] COEFF) {

			int maxi = 0;
			for (int i = 0; i < IN_TONES.length; i++) {
				Q1[i] = Q2[i] = 0;
				// avg[i] = 1;
				for (int k = 0; k < WINDOW_SIZE; k++) {
					// avg[i] += buffer[j];
					// log.v("%d", buffer[j * WINDOW_SIZE + k]);
					int sample = buffer[j * WINDOW_SIZE + k];
					if (sample > maxSample)
						maxSample = sample;
					if (sample < minSample)
						minSample = sample;
					log.v("%d %d", minSample, maxSample);
					double Q0 = COEFF[i] * Q1[i] - Q2[i] + buffer[j * WINDOW_SIZE + k];
					Q2[i] = Q1[i];
					Q1[i] = Q0;
				}
				mag[i] = Math.sqrt(Q1[i] * Q1[i] + Q2[i] * Q2[i] - COEFF[i] * Q1[i] * Q2[i]);
				if (mag[i] > mag[maxi])
					maxi = i;
			}
			return maxi;
		}
		
		double goertzelSimple(final short[] buffer, final int bufferOffset, final double COEFF) {
			
			double Q1 = 0, Q2=0;
			for (int s=0; s<WINDOW_SIZE; s++) {
				double Q0 = COEFF * Q1 - Q2 + buffer[bufferOffset+s];
				Q2 = Q1;
				Q1 = Q0;
			}
			return Math.sqrt(Q1 * Q1 + Q2 * Q2 - COEFF * Q1 * Q2);
			
		}
		
		void FMSimpleRecv(byte smpl) {
			  byte newBit;
			  unsigned long stamp = millis();
			  unsigned int diff = stamp - lastStamp;

			  if(state != IDLE && diff > BIT_PERIOD*4) {
			    state = IDLE;
			    msgs[bank].len = bytei;
			    msgs[bank].ready = true;
			    Serial.print("Block ready:");
			    Serial.print(bank);
			    Serial.print(" ");
			    Serial.println(bytei);

			    bank = (bank + 1)%NUM_MSGS;
			    msgs[bank].ready = false;
			    bytei = 0;
			  }  

			  if(lastSmpl!=smpl){
			    lastSmpl = smpl;
			    //unsigned long stamp = millis();
			    //Serial.println(diff);
			    switch(state) {
			    case IDLE:
			      if(smpl == 1) {
			        lastStamp = stamp;
			        state = START;
			      }
			      break;
			    case START:
			      block = 0;
			      biti = 0;
			      state = DONE;
			      if(diff > BIT_PERIOD_3_2) {
			        //Serial.println(diff);
			        state = LOW_PULSE;
			      }
			      //else {
			      //  Serial.println("START pulse too short");
			      //}
			      break;
			    case LOW_PULSE:
			      //newBit = 0;
			      if(diff > BIT_PERIOD_3_2) {
			        //newBit = 1;
			        block |= 1<<biti;
			      }
			      lastStamp = stamp;
			      state = HIGH_PULSE;

			      if(++biti==8) {
			        state = START;
			        Serial.println(block,HEX);
			        msgs[bank].bytes[bytei++] = block;
			        //if(bytei == bytej) Serial.println("TOO SLOW");
			      }
			      break;
			    case HIGH_PULSE:
			      state = LOW_PULSE;
			      if(diff > BIT_PERIOD_3_2) {
			        Serial.println("HIGH_PULSE too long");
			        state = DONE;
			      }
			      break;
			    }
			  }
			}  

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

			double Q1[] = new double[IN_TONES.length];
			double Q2[] = new double[IN_TONES.length];
			double mag[] = new double[IN_TONES.length];

			int zcount = 0;

			final int NEW_SAMPLE_MATCH = 2;
			int lastNewSample = -1;
			int newSampleMatchCount = 0;

			while (true) {

				int read = 0;
				while (read < N) {
					read += recorder.read(buffer, read, N - read);
				}

				for (int j = 0; j < read / WINDOW_SIZE; j++) {
					stamp++;
					//int maxi = goertzelEmbedded(j, buffer, mag, Q1, Q2, COEFF);
					double mag = goertzelSimple(buffer, j*WINDOW_SIZE,  COEFF[0]);
					/*
					 * for (int i = 0; i < IN_TONES.length; i++) { Q1[i] = Q2[i]
					 * = 0; // avg[i] = 1; for (int k = 0; k < WINDOW_SIZE; k++)
					 * { // avg[i] += buffer[j]; double Q0 = COEFF[i] * Q1[i] -
					 * Q2[i] + buffer[j * WINDOW_SIZE + k]; Q2[i] = Q1[i]; Q1[i]
					 * = Q0; } mag[i] = Math.sqrt(Q1[i] * Q1[i] + Q2[i] * Q2[i]
					 * - COEFF[i] * Q1[i] * Q2[i]); if (mag[i] > mag[maxi]) maxi
					 * = i; }
					 */

					// log.v("max:%d=%f",maxi,mag[maxi]);
					int newSample = -1;
					if (mag[maxi] > THRESHOLD) {
						log.v("max:%d=%f", maxi, mag[maxi]);
						// log.d("mag[0]=%f mag[1]=%f mag[2]=%f",mag[0],mag[1],mag[2]);
						newSample = maxi;
						processNewSample(maxi, mag[maxi]);

					}

					if (newSample == lastNewSample) {
						if (++newSampleMatchCount == NEW_SAMPLE_MATCH) {
							// processNewSample(newSample);
						}
					} else {
						newSampleMatchCount = 0;
					}
					lastNewSample = newSample;
					/*
					 * else if (++zcount == NEW_STATE_SAMPLES*3) {
					 * log.d("reset"); for (int i = 0; i < NEW_STATE_SAMPLES;
					 * i++) { newStateSamples[i] = 1; } processNewState(-1);
					 * continue;
					 * 
					 * }
					 */

				}
			}

			// recorder.stop();
			// recorder.release();
			// return null;
		}
	}
}