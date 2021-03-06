package sjtu.dclab.smartcity.ui.fragment;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;
import sjtu.dclab.smartcity.GlobalApp;
import sjtu.dclab.smartcity.R;
import sjtu.dclab.smartcity.chat.*;
import sjtu.dclab.smartcity.community.config.Me;
import sjtu.dclab.smartcity.community.entity.Friend;
import sjtu.dclab.smartcity.community.entity.Group;
import sjtu.dclab.smartcity.community.talk.MyTalk;
import sjtu.dclab.smartcity.tools.GsonTool;
import sjtu.dclab.smartcity.transfer.GroupTransfer;
import sjtu.dclab.smartcity.ui.chat.AddContactsAty;
import sjtu.dclab.smartcity.ui.chat.ChatActivity;
import sjtu.dclab.smartcity.webservice.BasicWebService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Yang on 2015/7/22.
 */
public class ContactsFragment extends Fragment {
    final private String TAG = "ContactFragment";

    // 长意的
    final static String TOKEN = "cn.edu.sjtu.se.dclab.community_chat.ListActivity";
    private static final String SERVICE_CLASSNAME = "org.eclipse.paho.android.service.MqttService";
    private MyTalk talk;
    private List<Friend> friends = null;
    private List<Group> groups = new ArrayList<Group>();
    private View view;
    private ListView lv_groups;
    private ListView lv_contacts;
    private ImageButton ibtnAddContact;
    private ArrayList<HashMap<String, Object>> items_contacts;
    private ArrayList<HashMap<String, Object>> items_groups;
    private MqttAndroidClient client;

    private HeartbeatService hbSvc;
    private ServiceConnection conn;
    private Activity curAty;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Fragment created");

        curAty = getActivity();
        talk = ((GlobalApp) getActivity().getApplication()).getTalk();

        //for network
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // init friend list
        friends = Friends.getFriends();

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                hbSvc = ((HeartbeatService.HeartBeatBinder) iBinder).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                hbSvc = null;
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "Fragment created");
        view = inflater.inflate(R.layout.fragment_contacts, container, false);

        ibtnAddContact = (ImageButton) view.findViewById(R.id.ibtn_addNewContacts);
        ibtnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddContactsAty.class));
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        init();
//        setUpMessageAdapters();
        startMQTTService();
    }

    @Override
    public void onPause() {
        super.onPause();
//        curAty.unbindService(conn);
        Log.i(TAG, "unbindService");    // not work
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        curAty.unbindService(conn);
    }

    public void init() {

        if (friends == null || friends.size() == 0) {
            friends = (List<Friend>) talk.getFriends();
            //TODO 在好友表中去掉自己
//            for (Friend f : friends) {
//                if (f.getId() == Me.id)
//                    friends.remove(f);
//            }
            Friends.addFriends(friends);
        }

        lv_contacts = (ListView) getFragmentManager().findFragmentById(R.id.fragment_contact).getView().findViewById(R.id.lv_contacts);
        items_contacts = new ArrayList<HashMap<String, Object>>();

        for (Friend friend : friends) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            String name = friend.getName() == null ? "路人甲" : friend.getName();
            map.put("name", name);
            String pic = friend.getImage();
            pic = pic == null ? "tab_img_profile" : pic.split("\\.")[0];
            int picId = getResources().getIdentifier(pic, "drawable", getActivity().getPackageName());
            map.put("pic", picId);
            items_contacts.add(map);
        }

        SimpleAdapter adapterFriend = new SimpleAdapter(getActivity(),
                items_contacts, R.layout.list_friend,
                new String[]{"name", "pic"},
                new int[]{R.id.list_friend_name, R.id.list_friend_img});
        lv_contacts.setAdapter(adapterFriend);
        lv_contacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(String.valueOf(R.string.friend), friends.get(arg2));
                getActivity().startActivity(intent);
            }
        });

        //init group list
        lv_groups = (ListView) getFragmentManager().findFragmentById(R.id.fragment_contact).getView().findViewById(R.id.lv_groups);
        items_groups = new ArrayList<HashMap<String, Object>>();
        String url = getString(R.string.URLRoot) + "groups/0/users/" + Me.id;
        String groupStr = new BasicWebService().sendGetRequest(url, null);
        List<GroupTransfer> gts = GsonTool.getObjectList(groupStr, GroupTransfer[].class);

        for (GroupTransfer gt : gts) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("name", gt.getName());
            map.put("pic", R.drawable.group_profile);
            items_groups.add(map);

            Group g = new Group();
            g.setId(gt.getGroupId());
            g.setName(gt.getName());
//            g.setImage("");
            groups.add(g);
        }

        //  TODO test
        if (groups != null && !groups.isEmpty()) {
            Groups.addGroups(groups);
        }

        SimpleAdapter adapterGroup = new SimpleAdapter(getActivity(),
                items_groups, R.layout.list_friend,
                new String[]{"name", "pic"},
                new int[]{R.id.list_friend_name, R.id.list_friend_img});
        lv_groups.setAdapter(adapterGroup);
        lv_groups.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        //TODO group talk
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra("ISGROUP", true);
                        intent.putExtra(String.valueOf(R.string.group), groups.get(i));
                        getActivity().startActivity(intent);
                    }
                }
        );
    }

    private void startMQTTService() {
        MqttConnectOptions conOpt = new MqttConnectOptions();

        client = new MqttAndroidClient(getActivity(), Configurations.uri, Configurations.clientId);

        conOpt.setCleanSession(Configurations.cleanSession);
        conOpt.setConnectionTimeout(Configurations.timeout);
        conOpt.setKeepAliveInterval(Configurations.keepalive);
        conOpt.setUserName(Configurations.username);
        conOpt.setPassword(Configurations.password.toCharArray());

        client.setCallback(new PushCallback(getActivity()));

        try {
            client.connect(conOpt, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken arg0) {
                    try {
                        client.subscribe(Configurations.subscribeTopicPrefix + Me.id, Configurations.qos);
                        Publisher.register(client);
                        startHeartService();
                    } catch (MqttSecurityException e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    } catch (MqttException e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken arg0, Throwable arg1) {
//                    Toast.makeText(getActivity(), "MQTT 服务器连接失败", Toast.LENGTH_SHORT).show();
                    Log.e(TAG + " Failure", arg1.getMessage());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void startHeartService() {
        Intent heartbeatSvc = new Intent(curAty, HeartbeatService.class);
        curAty.bindService(heartbeatSvc, conn, Context.BIND_AUTO_CREATE);
        curAty.startService(heartbeatSvc);
    }

    private boolean serviceIsRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (SERVICE_CLASSNAME.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
