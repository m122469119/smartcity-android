package sjtu.dclab.smartcity.ui.chat;

import android.app.Activity;
import android.os.Bundle;

import android.view.KeyEvent;
import sjtu.dclab.smartcity.R;

/**
 * ChatGroupDetailsAty
 *
 * @author Jian Yang
 * @date 2015/7/24
 */
public class ChatGroupDetailsAty extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_chat_group_details);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return true;
    }
}