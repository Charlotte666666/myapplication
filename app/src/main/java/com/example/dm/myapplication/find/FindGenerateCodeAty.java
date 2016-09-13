package com.example.dm.myapplication.find;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.dm.myapplication.R;
import com.example.dm.myapplication.find.zxing.encoding.EncodingUtils;
import com.example.dm.myapplication.utiltools.AbsentMConstants;
import com.example.dm.myapplication.utiltools.BimpUtil;
import com.example.dm.myapplication.utiltools.FileUtil;
import com.example.dm.myapplication.utiltools.SystemUtils;

/**
 * FindGenerateCodeAty
 * Created by dm on 16-9-13.
 */
public class FindGenerateCodeAty extends Activity implements View.OnClickListener {
    private static final String LOG_TAG = "FindGenerateCodeAty";
    private static final int REQUEST_CODE_ALBUM_1 = 1;
    private static final int REQUEST_CODE_CROP_2 = 2;

    private ImageButton mBackIbtn;
    private ImageButton mAddSettingsIbtn;
    private EditText mQrCodeInfoEt;
    private ImageView mQrLogoImv;
    private ToggleButton mToggleButton;
    private Button mGenereteBtn;
    private ImageView mGenerateQrImv;

    private int mForeColor;     // 二维码前景色：默认黑色
    private int mBackColor;     // 二维码背景色：默认白色
    private String mLogoPathStr;    // 二维码片保存地址
    private boolean isQRcodeGenerated;  // 是否生成
    private String qrSettingsItemStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_layout);

        initQrBaseValues();
        initView();
    }

    /**
     * 获取保存的自定义二维码logo地址、前景色、背景色
     */
    private void initQrBaseValues() {
        SharedPreferences share = getSharedPreferences(
                AbsentMConstants.EXTRA_ABSENTM_SHARE, Activity.MODE_PRIVATE);
        mLogoPathStr = share.getString(AbsentMConstants.QRCODE_LOGO_PATH, null);
        mForeColor = share.getInt(AbsentMConstants.FORE_COLOR, 0xff000000);
        mBackColor = share.getInt(AbsentMConstants.BACK_COLOR, 0xffffffff);
    }

    private void initView() {
        mBackIbtn = (ImageButton) findViewById(R.id.generate_title_left_ibtn);
        mAddSettingsIbtn = (ImageButton) findViewById(R.id.generate_title_right_ibtn);
        mQrCodeInfoEt = (EditText) findViewById(R.id.generate_qr_code_et);
        mQrLogoImv = (ImageView) findViewById(R.id.generate_logo_imv);
        mToggleButton = (ToggleButton) findViewById(R.id.generate_logo_tgbtn);
        mGenereteBtn = (Button) findViewById(R.id.generate_qr_code_btn);
        mGenerateQrImv = (ImageView) findViewById(R.id.generate_result_imv);

        if (mLogoPathStr != null) {
            mQrLogoImv.setImageBitmap(BitmapFactory.decodeFile(mLogoPathStr));
        }

        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isQRcodeGenerated) {
                    generateQRcode();
                }
            }
        });

        mBackIbtn.setOnClickListener(FindGenerateCodeAty.this);
        mAddSettingsIbtn.setOnClickListener(FindGenerateCodeAty.this);
        mGenereteBtn.setOnClickListener(FindGenerateCodeAty.this);
        mGenereteBtn.setClickable(false);

        mQrCodeInfoEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @SuppressWarnings("deprecation")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    mGenereteBtn.setClickable(false);
                    mGenereteBtn.setBackgroundResource(R.drawable.item_teal_selector);
                    mGenereteBtn.setTextColor(getResources().getColor(R.color.gray700));
                } else {
                    mGenereteBtn.setClickable(true);
                    mGenereteBtn.setBackgroundResource(R.drawable.item_teal_selector);
                    mGenereteBtn.setTextColor(getResources().getColor(R.color.white));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void generateQRcode() {
        String contentString = mQrCodeInfoEt.getText().toString();
        Bitmap logoBitmap = null;

        if (mToggleButton.isChecked()) {
            if (mLogoPathStr != null) {
                // 自定义logo图标
                Log.i(LOG_TAG, "mLogoPathStr >>> " + mLogoPathStr);
                logoBitmap = BitmapFactory.decodeFile(mLogoPathStr);
                Log.i(LOG_TAG, "logoBitmap >>> " + logoBitmap);
            }

            if (logoBitmap == null) {
                // 默认logo为应用图标
                logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.app_icon);
            }

            // 生成圆形Logo
            logoBitmap = BimpUtil.toRoundBitmap(logoBitmap);
        }

        int size = SystemUtils.dip2px(this, 320);
        //根据字符串生成二维码图片并显示在界面上，第2, 3个参数为图片宽高
        Bitmap qrCodeBitmap = EncodingUtils.createQRCode(contentString,
                size, size, logoBitmap, mForeColor, mBackColor);
        mGenerateQrImv.setImageBitmap(qrCodeBitmap);
        isQRcodeGenerated = true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.generate_title_left_ibtn:
                FindGenerateCodeAty.this.finish();
                break;
            case R.id.generate_title_right_ibtn:
                operateQrSettins();
                break;
            case R.id.generate_qr_code_btn:
                generateQRcode();
                break;
        }
    }

    private void operateQrSettins() {
        new MaterialDialog.Builder(FindGenerateCodeAty.this)
                .title("二维码片设置")
                .items(R.array.qr_settings_values)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog,
                                            View itemView,
                                            int position,
                                            CharSequence text) {

                        qrSettingsItemStr = (String) text;
                        Log.d(LOG_TAG, "qrSettingsItemStr >>> " + qrSettingsItemStr);

                        dealChooseItemEvent(qrSettingsItemStr);
                    }
                }).show();
    }

    private void dealChooseItemEvent(String itemStr) {
        switch (itemStr) {
            case "从相册选择Logo":
                Intent intentAlbum = new Intent(Intent.ACTION_GET_CONTENT);
                intentAlbum.addCategory(Intent.CATEGORY_OPENABLE);
                intentAlbum.setType("image/*");
                intentAlbum.putExtra("return-data", true);
                startActivityForResult(intentAlbum, REQUEST_CODE_ALBUM_1);
                break;
            case "保存二维码至手机":
                if (isQRcodeGenerated) {
                    Bitmap bitmap = FileUtil.imageView2Bitmap(
                            FindGenerateCodeAty.this, mGenerateQrImv);

                    String savePath = FileUtil.saveBitmapToJpg(
                            FindGenerateCodeAty.this, bitmap);

                    SystemUtils.showHandlerToast(FindGenerateCodeAty.this,
                            "保存至 -> " + savePath);
                } else {
                    SystemUtils.showHandlerToast(FindGenerateCodeAty.this,
                            "请先制作二维码片");
                }

                break;
            case "二维码前景色":
                break;
            case "二维码背景色":
                break;
            case "恢复默认":
                break;
        }
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri uri
     */
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");  // crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("aspectX", 1);   // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);  // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputY", 300);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQUEST_CODE_CROP_2);
    }

    /**
     * 保存裁剪之后的图片数据
     *
     * @param picdata intent
     */
    private void setPicToView(Intent picdata) {
        Bundle extras = picdata.getExtras();
        Log.i(LOG_TAG, "extras.toString >> " + extras.toString());

        Bitmap qrLogoBitmap = extras.getParcelable("data");
        mQrLogoImv.setImageBitmap(qrLogoBitmap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_ALBUM_1:
                    try {
                        // 从uri获取选择相册返回的图片地址
                        Uri uri = data.getData();
                        String path = uri.getEncodedPath();
                        Log.i(LOG_TAG, "path1 >> " + path);

                        if (path != null) {
                            path = uri.getPath();
                            Log.i(LOG_TAG, "path2 >> " + path);
                        }

                        mLogoPathStr = path;
                        startPhotoZoom(data.getData());
                    } catch (NullPointerException e) {
                        Log.i(LOG_TAG, e.getMessage());
                    }

                    break;
                case REQUEST_CODE_CROP_2:   // 取得裁剪后的图片
                    if (data != null) {
                        setPicToView(data);
                        Log.i(LOG_TAG, ">> " + REQUEST_CODE_CROP_2);
                    }

                    break;
            }
        }
    }
}
