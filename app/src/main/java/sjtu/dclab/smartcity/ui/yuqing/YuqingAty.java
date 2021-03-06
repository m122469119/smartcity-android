package sjtu.dclab.smartcity.ui.yuqing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import sjtu.dclab.smartcity.R;
import sjtu.dclab.smartcity.ui.MenuPopupWindow;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by HuangZhenyu on 15/7/28.
 */
public class YuqingAty extends Activity {
    private ListView cur_tie;
    private int[] icons;
    private String[] titles;
    private String[] context;
    private String[] person;
    private String[] time;
    private String[] statue;
    private ImageButton menu;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_yuqing);

        icons = new int[] { R.drawable.minzhen,
                R.drawable.payment_record};
        context = new String[] {"关于小区路灯修建问题","关于A501随意放置XX问题"};
        titles = new String[] {"关于小区路灯修建问题","关于A501随意放置XX问题"};
        person = new String[] {"李四","李四"};
        time =new String[] {"2015-07-12","2015-07-12"};
        statue = new String[] {"屏蔽","审核"};


        cur_tie = (ListView) findViewById(R.id.listView3);

        ArrayList<HashMap<String, Object>> serviceItems = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < icons.length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("itemIcon", icons[i]);
            map.put("itemCon", context[i]);
            map.put("itemTit",titles[i]);
            map.put("itemPer",person[i]);
            map.put("itemSta",statue[i]);
            map.put("itemTim",time[i]);
            serviceItems.add(map);
        }
        String[] from = new String[] { "itemIcon", "itemCon","itemTit","itemPer","itemSta","itemTim"};
        int[] to = new int[] { R.id.itemIcon, R.id.itemCon,R.id.itemTit,R.id.itemPer,R.id.itemSta,R.id.itemTim};
        SimpleAdapter itemsAdapter = new SimpleAdapter(this, serviceItems,
                R.layout.yuqing_item, from, to);
        cur_tie.setAdapter(itemsAdapter);
        cur_tie.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getApplicationContext(), Yqdetail.class));


            }
        });

        menu = (ImageButton) findViewById(R.id.menu);

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuPopupWindow menuPopupWindow = new MenuPopupWindow(YuqingAty.this);
                menuPopupWindow.showPopupWindow(menu);
                menuPopupWindow.getContentView().findViewById(R.id.add_task_layout).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(getApplicationContext(), YuqingAty.class));

                            }
                        }
                );
                menuPopupWindow.getContentView().findViewById(R.id.pinbi_tiezi).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(getApplicationContext(),YqYPB.class));

                            }
                        }
                );
                menuPopupWindow.getContentView().findViewById(R.id.team_member_layout).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(getApplicationContext(),YqDSH.class));

                            }
                        }
                );
                menuPopupWindow.getContentView().findViewById(R.id.mingan_mana).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }
                );
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return true;
    }
}
