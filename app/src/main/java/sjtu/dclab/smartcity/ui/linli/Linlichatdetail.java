package sjtu.dclab.smartcity.ui.linli;

import android.app.Activity;
import android.os.Bundle;

import android.view.KeyEvent;
import sjtu.dclab.smartcity.R;

/**
 * Created by HuangZhenyu on 15/7/30.
 */
public class Linlichatdetail extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_linli_detail);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return true;
    }
}
