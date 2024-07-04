package com.mycompany.posmultimarca.POS.Newland.PIN;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.newland.os.NlBuild;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.widget.TextView;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.pin.*;
import com.newland.mtype.util.ISOUtils;

import com.mycompany.posmultimarca.R;



import java.util.concurrent.TimeUnit;

/**
 * Password Keyboard Activity
 */


public class KeyBoardNumberActivity extends Activity {
/*    private static final String TAG = "KeyBoardNumberActivity";
    private K21Pininput pinInput;
    private SDKDevice device;
    private TextView txtPassword;
    private StringBuffer buffer;
    private int inputLen = 0;
    private PinKeyBoard pkb;
    private SoundPoolImpl spi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.input_pin_fragment);
        pinInput = device.getInstance(this).getK21Pininput();
        spi = SoundPoolImpl.getInstance();
        spi.initLoad(this);
        init();
    }

    private void init() {
        txtPassword = (TextView) findViewById(R.id.txt_password);
        pkb = (PinKeyBoard) findViewById(R.id.n900pinkeyboard);
        final String accNo = getIntent().getStringExtra("accNo");
        pkb.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            private boolean first;//  To prevent it from entering the onPreDraw() all the time.

            @Override
            public boolean onPreDraw() {
                if (!first) {
                    first = true;
                    boolean bool = getRandomKeyBoardNumber();
                    if (!bool) {
                        finish();
                        return first;
                    }
                    WorkingKey wkPinIndex = null;
                    AccountInputType acctInputType = AccountInputType.USE_ACCOUNT;
                    int pwMaxLen = 12;
                    byte[] pwdLenRange = getPinLengthRange(0, 12);
                    byte[] pinPadding = new byte[]{'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F'};
                    int timeOut = 59;
                    KeyManageType keyManageType = null;
                    PinConfirmType pinConfirmType = PinConfirmType.ENABLE_ENTER;
                    if (AppConfig.MKSK_DES.equals(AppConfig.KEY_SYS_ALG)) {
                        wkPinIndex = new WorkingKey(AppConfig.Pin.MKSK_DES_INDEX_WK_PIN);
                        keyManageType = KeyManageType.MKSK;
                    } else if (AppConfig.MKSK_SM4.equals(AppConfig.KEY_SYS_ALG)) {
                        wkPinIndex = new WorkingKey(AppConfig.Pin.MKSK_SM4_INDEX_WK_PIN);
                        keyManageType = KeyManageType.SM4;
                    } else if (AppConfig.MKSK_AES.equals(AppConfig.KEY_SYS_ALG)) {
                        wkPinIndex = new WorkingKey(AppConfig.Pin.MKSK_AES_INDEX_WK_PIN);
                        keyManageType = KeyManageType.MKSK_AES;
                    } else if (AppConfig.DUKPT_DES.equals(AppConfig.KEY_SYS_ALG)) {
                        wkPinIndex = new WorkingKey(AppConfig.Pin.DUKPT_DES_INDEX);
                        keyManageType = KeyManageType.DUKPT;
                    }
                    pinInput.startStandardPinInput(null, wkPinIndex, keyManageType, acctInputType,
                            accNo, pwMaxLen, pwdLenRange, pinPadding, pinConfirmType,
                            timeOut, TimeUnit.SECONDS, null, null, pinInputListener);
                }
                return first;
            }
        });

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2: // inputting
                    int len = (Integer) msg.obj;
                    buffer = new StringBuffer();
                    for (int i = 0; i < len; i++) {
                        buffer.append(" * ");
                    }
                    txtPassword.setText(buffer.toString());
                    break;

                default:
                    break;
            }
        }
    };


    private boolean getRandomKeyBoardNumber() {
        try {
            byte[] initCoordinate = pkb.getCoordinate();
            Log.i(TAG, getString(R.string.keyboard_activity_log_init_coordinates) + ISOUtils.hexString(initCoordinate));
            // get key value of random keyboard
            byte[] keySeq = pkb.getPinKeySeq(PinKeyBoard.PinKeySeq.RANDOM_NUM);
            KeyboardRandom keyboardRandom = null;
            // If the number is random and the function key is fixed, do not pass the key value sequence.
            if (keySeq != null) {
                keyboardRandom = new KeyboardRandom(initCoordinate, keySeq);
            } else {
                keyboardRandom = new KeyboardRandom(initCoordinate);
            }

            byte[] randomCoordinate = pinInput.loadRandomKeyboard(keyboardRandom);
            pkb.loadRandomKeyboardfinished(randomCoordinate);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private DeviceEventListener<K21PininutEvent> pinInputListener = new DeviceEventListener<K21PininutEvent>() {
        @Override
        public Handler getUIHandler() {
            return null;
        }

        @Override
        public void onEvent(K21PininutEvent event, Handler h) {
            spi.play();
            if (event.isProcessing()) {// Inputting
                Log.i(TAG, "is Processing");
                PinInputEvent.NotifyStep notifyStep = event.getNotifyStep();
                if (notifyStep == PinInputEvent.NotifyStep.ENTER) {
                    inputLen = inputLen + 1;
                    Log.i(TAG, getString(R.string.keyboard_activity_log_press_key_code) + inputLen);
                } else if (notifyStep == PinInputEvent.NotifyStep.BACKSPACE) {
                    inputLen = (inputLen <= 0 ? 0 : inputLen - 1);
                    Log.i(TAG, getString(R.string.keyboard_activity_log_press_cancel_code) + inputLen);
                }
                Message msg = mHandler.obtainMessage(2);
                msg.obj = inputLen;
                msg.sendToTarget();

            } else if (event.isUserCanceled()) {// cancel
                Log.i(TAG, "Is UserCanceled");
                finish();
                Message pinFinishMsg = new Message();
                pinFinishMsg.what = AppConfig.EMV.PIN_FINISH;
                pinFinishMsg.obj = null;
                SimpleTransferListener.getPinEventHandler().sendMessage(pinFinishMsg);

                Message pinFinishMsg_finalSelt = new Message();
                pinFinishMsg_finalSelt.what = AppConfig.EMV.PIN_FINISH;
                pinFinishMsg_finalSelt.obj = null;
                SimpleTransferListener.getPinEventHandler().sendMessage(pinFinishMsg_finalSelt);

                Message pinFinishMsg_l3 = new Message();
                pinFinishMsg_l3.what = AppConfig.EMV.PIN_FINISH;
                pinFinishMsg_l3.obj = null;
                SimpleEmvL3Listener.getPinEventHandler().sendMessage(pinFinishMsg_l3);
            } else if (event.isSuccess()) {// confirm
                Log.i(TAG, "Is Success"+event.getInputLen());
                if (event.getInputLen() == 0) {
                    Log.i(TAG, getString(R.string.keyboard_activity_log_input_empty));
                    Intent i = new Intent();
                    i.putExtra("pin", new byte[]{});
                    setResult(RESULT_OK, i);
                    finish();
                    Message pinFinishMsg = new Message();
                    pinFinishMsg.what = AppConfig.EMV.PIN_FINISH;
                    pinFinishMsg.obj = new byte[]{};
                    SimpleTransferListener.getPinEventHandler().sendMessage(pinFinishMsg);

                    Message pinFinishMsg_finalSelt = new Message();
                    pinFinishMsg_finalSelt.what = AppConfig.EMV.PIN_FINISH;
                    pinFinishMsg_finalSelt.obj = new byte[]{};
                    SimpleTransferListener.getPinEventHandler().sendMessage(pinFinishMsg_finalSelt);

                    Message pinFinishMsg_l3 = new Message();
                    pinFinishMsg_l3.what = AppConfig.EMV.PIN_FINISH;
                    pinFinishMsg_l3.obj = new byte[]{};
                    SimpleEmvL3Listener.getPinEventHandler().sendMessage(pinFinishMsg_l3);
                } else {
                    byte[] pin = event.getEncrypPin();
                    AppConfig.EMV.pinBlock = pin;
                    Log.i(TAG, getString(R.string.keyboard_activity_log_input_success) + ISOUtils.hexString(pin));
                    Intent i = new Intent();
                    i.putExtra("pin", pin);
                    setResult(RESULT_OK, i);
                    finish();
                    Message pinFinishMsg = new Message();
                    pinFinishMsg.what = AppConfig.EMV.PIN_FINISH;
                    pinFinishMsg.obj = pin;
                    SimpleTransferListener.getPinEventHandler().sendMessage(pinFinishMsg);

                    Message pinFinishMsg_finalSelt = new Message();
                    pinFinishMsg_finalSelt.what = AppConfig.EMV.PIN_FINISH;
                    pinFinishMsg_finalSelt.obj = pin;
                    SimpleTransferListener.getPinEventHandler().sendMessage(pinFinishMsg_finalSelt);

                    Message pinFinishMsg_l3 = new Message();
                    pinFinishMsg_l3.what = AppConfig.EMV.PIN_FINISH;
                    pinFinishMsg_l3.obj = pin;
                    SimpleEmvL3Listener.getPinEventHandler().sendMessage(pinFinishMsg_l3);
                }
            } else {
                Log.i(TAG, getString(R.string.keyboard_activity_log_input_exception), event.getException());
                Intent i = new Intent();
                setResult(-2, i);
                finish();
                Message pinFinishMsg = new Message();
                pinFinishMsg.what = AppConfig.EMV.PIN_FINISH;
                pinFinishMsg.obj = null;
                SimpleTransferListener.getPinEventHandler().sendMessage(pinFinishMsg);

                Message pinFinishMsg_finalSelt = new Message();
                pinFinishMsg_finalSelt.what = AppConfig.EMV.PIN_FINISH;
                pinFinishMsg_finalSelt.obj = null;
                SimpleTransferListener.getPinEventHandler().sendMessage(pinFinishMsg_finalSelt);

                Message pinFinishMsg_l3 = new Message();
                pinFinishMsg_l3.what = AppConfig.EMV.PIN_FINISH;
                pinFinishMsg_l3.obj = null;
                SimpleEmvL3Listener.getPinEventHandler().sendMessage(pinFinishMsg_l3);
            }
        }
    };


    private byte[] getPinLengthRange(int pinMinLen, int pinMaxLen) {
        byte[] sumPinLen = new byte[]{0x00, 0x00, 0x00, 0x00, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C};
        byte[] pinLen = new byte[pinMaxLen - pinMinLen + 1];
        System.arraycopy(sumPinLen, pinMinLen, pinLen, 0, pinLen.length);
        return pinLen;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        spi.release();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Rect rect = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        // status bar height
        int statusBarHeight = rect.top;

        // title bar height + status bar height
        int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();

        // title bar height
        int contentViewHeight = contentViewTop - statusBarHeight;
        // Give it different values depending on the platform.
        Log.i(TAG, "contentViewHeight=" + contentViewHeight + ";contentViewTop" + contentViewTop + "statusBarHeight" + statusBarHeight);

        // screen
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Log.i(TAG, getString(R.string.keyboard_activity_log_screen_height) + dm.heightPixels); //952  实际的屏幕高度

        // application area
        Rect outRect1 = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect1);
        Log.i(TAG, getString(R.string.keyboard_activity_log_application_top) + outRect1.top);// the status bar height is 50dp,The navigation bar height is 96dp.
        Log.i(TAG, getString(R.string.keyboard_activity_log_application_height) + outRect1.height());

        //View draw area
        Rect outRect2 = new Rect();
        getWindow().findViewById(Window.ID_ANDROID_CONTENT).getDrawingRect(outRect2);
        Log.i(TAG, getString(R.string.keyboard_activity_log_view_draw_area_top_error_method) + outRect2.top);   // get outRect2.top don't like the above,it maybe get 0 with outRect2.top, maybe a bug.
        int viewTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();   // right method
        Log.i(TAG, getString(R.string.keyboard_activity_log_view_draw_area_top_right_method) + viewTop);  // status bar height + title bar height=146
        Log.i(TAG, getString(R.string.keyboard_activity_log_view_draw_area_top) + outRect2.height());


        String TOUCHSCREEN_RESOLUTION = NlBuild.VERSION.TOUCHSCREEN_RESOLUTION;
        int height = Integer.valueOf(TOUCHSCREEN_RESOLUTION.split("x")[0]); // Get Touch resolution of K21
        // Geometric scaling
        int width = Integer.valueOf(TOUCHSCREEN_RESOLUTION.split("x")[1]);
        Log.i(TAG, "TOUCHSCREEN_RESOLUTION：height" + height + "width：" + width);
    }
*/
}

