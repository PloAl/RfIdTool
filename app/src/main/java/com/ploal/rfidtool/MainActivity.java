package com.ploal.rfidtool;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends Activity {

    public static final String TAG = "MainActivity";
    private TextView tagId;
    private ImageView mImageView;
    private boolean NFCEnabled;
    private String WriteString;
    private String CurAction;
    private int WritePGNum;

    @Override
    protected void onNewIntent(Intent intent) {
        StringBuilder DataRfId = new StringBuilder();
        //String DataRfId = "";
        int pgcount;
        String Id = "";
        String[] TechList;
        NfcA nfca = null;
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            sendResult(Id, DataRfId.toString(), null);
            return;
        }
        Id = ByteArrayToHexString(tag.getId());
        TechList = tag.getTechList();
        try {

            nfca = NfcA.get(tag);
            if (nfca == null) {
                sendResult(Id, DataRfId.toString(), TechList);
                return;
            }
            nfca.connect();
            if (!nfca.isConnected()) {
                sendResult(Id, DataRfId.toString(), TechList);
                return;
            }
            if (CurAction.equals("write")) {
                int ind = 0;
                int WriteLenght = WriteString.length();
                String WriteStr;

                for (int pageNum = WritePGNum; pageNum <= nfca.getMaxTransceiveLength() - 2 && ind < WriteLenght; pageNum++) {
                    byte[] PG = new byte[]{(byte) 0x000, (byte) 0x000, (byte) 0x000, (byte) 0x000};
                    if (WriteLenght < ind + 4) {
                        WriteStr = WriteString.substring(ind, WriteLenght);
                        if (WriteStr.length() >= 1)
                            PG[0] = (byte) ((int) WriteStr.charAt(0) & 0x0ff);
                        if (WriteStr.length() >= 2)
                            PG[1] = (byte) ((int) WriteStr.charAt(1) & 0x0ff);
                        if (WriteStr.length() >= 3)
                            PG[2] = (byte) ((int) WriteStr.charAt(2) & 0x0ff);
                    } else {
                        WriteStr = WriteString.substring(ind, ind + 4);
                        PG[0] = (byte) ((int) WriteStr.charAt(0) & 0x0ff);
                        PG[1] = (byte) ((int) WriteStr.charAt(1) & 0x0ff);
                        PG[2] = (byte) ((int) WriteStr.charAt(2) & 0x0ff);
                        PG[3] = (byte) ((int) WriteStr.charAt(3) & 0x0ff);
                    }
                    byte[] result = nfca.transceive(new byte[]{
                            (byte) 0xA2,  // WRITE
                            (byte) (pageNum & 0x0ff),
                            PG[0],
                            PG[1],
                            PG[2],
                            PG[3]
                    });
                    if (result == null) {
                        DataRfId.append(",error");
                    } else if ((result.length == 1) && ((result[0] & 0x00A) != 0x00A)) {
                        DataRfId.append(ByteArrayToHexString(result));
                    } else {
                        DataRfId.append(",success");
                    }

                    ind = ind + 4;
                }
            } else {
                pgcount = nfca.getMaxTransceiveLength() / 4;
                for (int pageNum = 0; pageNum < pgcount; pageNum = pageNum + 4) {
                    byte[] result = nfca.transceive(new byte[]{
                            (byte) 0x30,  // READ
                            (byte) (pageNum & 0x0ff)
                    });

                    DataRfId.append(ByteArrayToHexString(result));
                }
            }

        } catch (IOException er) {

        } finally {

            if (nfca != null) {
                try {
                    nfca.close();
                } catch (IOException er) {
                }
            }

        }
        sendResult(Id, DataRfId.toString(), TechList);
    }

    public static String unHex(String arg) {
        StringBuilder str = new StringBuilder();
        //String str = "";
        for (int i = 0; i < arg.length(); i += 2) {
            String s = arg.substring(i, (i + 2));
            int decimal = Integer.parseInt(s, 16);
            str.append((char) decimal);
        }
        return str.toString();
    }

    public static String ByteArrayToHexString(byte[] bytes) {

        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);

    }

    public void sendResult(String Id, String DataRfId, String[] TechList) {
        Intent intent_ = new Intent();
        if (DataRfId.isEmpty())
            DataRfId = "empty";

        intent_.putExtra("tech", TechList);
        intent_.putExtra("result", DataRfId);
        if (CurAction.equals("read") && !DataRfId.equals("empty"))
            intent_.putExtra("text", unHex(DataRfId));
        else
            intent_.putExtra("text", DataRfId);

        intent_.putExtra("event", CurAction);
        intent_.putExtra("uid", Id);
        setResult(RESULT_OK, intent_);
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();

        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null && NFCEnabled) {
            nfc.disableForegroundDispatch(this);
            NFCEnabled = false;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null && NFCEnabled) {
            nfc.disableForegroundDispatch(this);
            NFCEnabled = false;
        }
        finish();

    }

    @Override
    public void onResume() {
        super.onResume();

        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null) {
            final Intent intent = new Intent(this.getApplicationContext(), this.getClass());
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            String[][] techList = new String[3][1];
            techList[0][0] = "android.nfc.tech.NdefFormatable";
            techList[1][0] = "android.nfc.tech.NfcA";
            techList[2][0] = "android.nfc.tech.Ndef";

            IntentFilter[] filters = new IntentFilter[2];
            filters[0] = new IntentFilter();
            filters[0].addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
            filters[0].addCategory(Intent.CATEGORY_DEFAULT);
            filters[1] = new IntentFilter();
            filters[1].addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
            filters[1].addCategory(Intent.CATEGORY_DEFAULT);
            nfc.enableForegroundDispatch(this, pendingIntent, filters, techList);
            NFCEnabled = true;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = this.getIntent();
        String Action = intent.getAction();
        if (Action != null && Action.equals("com.ploal.rfidtool.VERSION")) {
            Intent intent_ = new Intent();
            intent_.putExtra("version", "1.1");
            intent_.putExtra("capabilities", "read write");
            setResult(RESULT_OK, intent_);
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        tagId = findViewById(R.id.IdText);
        String IdLabel = " Ожидание...";
        tagId.setText(IdLabel);

        mImageView = findViewById(R.id.imageView);
        if (Action != null && Action.equals("com.ploal.rfidtool.NFCREAD")) {
            CurAction = "read";
            mImageView.setImageResource(R.mipmap.nfc_);
        } else {
            CurAction = "write";
            mImageView.setImageResource(R.mipmap.nfc_w);
            TextView Header = findViewById(R.id.ViewText);
            Header.setText(getResources().getString(R.string.header_write));
            WritePGNum = Integer.parseInt(intent.getStringExtra("PageNumber"));
            WriteString = intent.getStringExtra("WriteString");
        }

        IdLabel = intent.getStringExtra("IdLabel");
        if (IdLabel != null)
            tagId.setText(IdLabel);

    }

}
