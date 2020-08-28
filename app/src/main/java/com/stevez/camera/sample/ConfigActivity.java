package com.stevez.camera.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.stevez.camerakit.Direction;

/**
 * @date: Create in 3:36 PM 2020/7/8
 * @author: zhuyuliang
 * @description 参数设置
 */
public class ConfigActivity extends AppCompatActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, ConfigActivity.class);
        context.startActivity(starter);
    }

    private TextView textView;
    private ImageView img_back;
    private RadioGroup radioGroup_face_ori;
    private RadioGroup radioGroup_mirror;
    private AppCompatEditText editText_rgbcameraid;
    private AppCompatEditText editText_ircameraid;
    private AppCompatEditText editText_vid;
    private AppCompatEditText editText_pid;
    private AppCompatEditText editText_width;
    private AppCompatEditText editText_height;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        textView = findViewById(R.id.txt_title);
        img_back = findViewById(R.id.img_back);
        radioGroup_face_ori = findViewById(R.id.face_ori);
        radioGroup_mirror = findViewById(R.id.mirror);
        editText_rgbcameraid = findViewById(R.id.edit_rgbcameraid);
        editText_ircameraid = findViewById(R.id.edit_ircameraid);
        editText_vid = findViewById(R.id.edit_rgbvid);
        editText_pid = findViewById(R.id.edit_rgbpid);
        editText_width = findViewById(R.id.edit_width);
        editText_height = findViewById(R.id.edit_height);


        if (ConstantsConfig.getInstance().getFaceOri() == Direction.UP) {
            ((RadioButton) findViewById(R.id.f_up)).setChecked(true);
        } else if (ConstantsConfig.getInstance().getFaceOri() == Direction.LEFT) {
            ((RadioButton) findViewById(R.id.f_left)).setChecked(true);
        } else if (ConstantsConfig.getInstance().getFaceOri() == Direction.RIGHT) {
            ((RadioButton) findViewById(R.id.f_right)).setChecked(true);
        } else if (ConstantsConfig.getInstance().getFaceOri() == Direction.DOWN) {
            ((RadioButton) findViewById(R.id.f_down)).setChecked(true);
        }
        radioGroup_face_ori.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (group.getCheckedRadioButtonId()) {
                    case R.id.f_up:
                        ConstantsConfig.getInstance().setFaceOri(Direction.UP);
                        break;
                    case R.id.f_left:
                        ConstantsConfig.getInstance().setFaceOri(Direction.LEFT);
                        break;
                    case R.id.f_right:
                        ConstantsConfig.getInstance().setFaceOri(Direction.RIGHT);
                        break;
                    case R.id.f_down:
                        ConstantsConfig.getInstance().setFaceOri(Direction.DOWN);
                        break;
                    default:
                        break;
                }
            }
        });
        if (ConstantsConfig.getInstance().getMirror()) {
            ((RadioButton) findViewById(R.id.mirror_true)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.mirror_false)).setChecked(true);
        }
        radioGroup_mirror.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (group.getCheckedRadioButtonId()) {
                    case R.id.mirror_false:
                        ConstantsConfig.getInstance().setMirror(false);
                        break;
                    case R.id.mirror_true:
                        ConstantsConfig.getInstance().setMirror(true);
                        break;
                    default:
                        break;
                }
            }
        });
        editText_rgbcameraid.setText(ConstantsConfig.getInstance().getRgbCamereId() + "");
        editText_ircameraid.setText(ConstantsConfig.getInstance().getIrCamereId() + "");
        editText_vid.setText(ConstantsConfig.getInstance().getVid() + "");
        editText_pid.setText(ConstantsConfig.getInstance().getPid() + "");
        editText_width.setText(ConstantsConfig.getInstance().getWidth() + "");
        editText_height.setText(ConstantsConfig.getInstance().getHeight() + "");

        textView.setText(R.string.param_config_str);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onStop() {
        if (!editText_rgbcameraid.getText().toString().isEmpty()) {
            ConstantsConfig.getInstance().setRgbCameraId(Integer.valueOf(editText_rgbcameraid.getText().toString()));
        }
        if (!editText_ircameraid.getText().toString().isEmpty()) {
            ConstantsConfig.getInstance().setIrCameraId(Integer.valueOf(editText_ircameraid.getText().toString()));
        }

        if (!editText_pid.getText().toString().isEmpty()) {
            ConstantsConfig.getInstance().setPid(Integer.valueOf(editText_pid.getText().toString()));
        }
        if (!editText_vid.getText().toString().isEmpty()) {
            ConstantsConfig.getInstance().setVid(Integer.valueOf(editText_vid.getText().toString()));
        }

        if (!editText_width.getText().toString().isEmpty()) {
            ConstantsConfig.getInstance().setWidth(Integer.valueOf(editText_width.getText().toString()));
        }
        if (!editText_height.getText().toString().isEmpty()) {
            ConstantsConfig.getInstance().setHeight(Integer.valueOf(editText_height.getText().toString()));
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
