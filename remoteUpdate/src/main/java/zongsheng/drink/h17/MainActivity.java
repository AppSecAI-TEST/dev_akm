package zongsheng.drink.h17;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import zongsheng.drink.h17.service.CheckService;

/**
 * Created by Administrator on 17/8/2.
 * 主Activity，
 */

public class MainActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this,"Run",Toast.LENGTH_SHORT).show();
        startService(new Intent(this, CheckService.class));
        //在Android 6.0以上，主题设置为Theme.NoDisplay之后，必须在onResume()之前调用finish()方法
        finish();
    }
}
