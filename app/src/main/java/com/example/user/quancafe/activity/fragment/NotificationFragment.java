package com.example.user.quancafe.activity.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.user.quancafe.R;
import com.example.user.quancafe.activity.adapter.DanhSachMonAnAdapter;
import com.example.user.quancafe.activity.model.MonAn;
import com.example.user.quancafe.activity.ultil.CheckConnect;
import com.example.user.quancafe.activity.ultil.Server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by User on 27/06/2018.
 */
// Danh sách món theo loại món
public class NotificationFragment extends Fragment {
    private View view;
    private int idloai = 0;
    private int page = 1;

    private ArrayList<MonAn> arrayMonAn;
    private ListView listViewMonAn;
    private DanhSachMonAnAdapter danhSachMonAnAdapter;
    private View footerview;

    private boolean isLoading = false;
    private boolean limitData = false;
    private mHandler mHandler;
    private Bundle bundle;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notification,null);




        AnhXa();

        bundle = this.getArguments();
        if (bundle != null) {

            // Danh sách món ăn theo từng loại
            // lấy id loại
            String myIdLoai = bundle.getString("idloai");
            idloai = Integer.parseInt(myIdLoai);
           // CheckConnect.ShowToast(getContext(),idloai+"");
            GetData(page);
            LoadMoreData();

        }

        return view;



    }

    private void LoadMoreData() {
        // bắt sự kiện click vào từng Item
        listViewMonAn.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(getApplicationContext(),ChiTietMonActivity.class);
//                intent.putExtra("thongtinmon",arrayMonAn.get(position));
//                startActivity(intent);
                DetailFoodFragment noti = new DetailFoodFragment();
                Bundle thongtinMon = new Bundle();
                thongtinMon.putSerializable("thongtinmon", arrayMonAn.get(position));

                noti.setArguments(thongtinMon);

                //Inflate the fragment
                getFragmentManager().beginTransaction().replace(R.id.frameLayoutMain, noti).commit();

            }
        });
        listViewMonAn.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // vuốt listView trả vào trong function này
                // hàm if để bắt giá trị cuối trong
                //lần đầu tiên run lên thì totalItenCount = 0 nên đặt total khác không để không bị nhảy vào function if liền
                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0 && isLoading == false && limitData == false) {
                    // nhảy vào function thì đang load dữ liệu
                    isLoading = true;
                    // Thực hiện cho Theard hoạt động
                    ThreadData threadData = new ThreadData();
                    threadData.start();
                }

            }
        });

    }

    // phương thức GetData(int page) dùng để đưa idloai lên server
    // sau đó lấy dữ liệu(là thông tin món ăn) về theo idloai
    // biến page giúp lấy đối tượng theo từng trang
    private void GetData(int page) {
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        String duongdan = Server.Duongdandanhsachmontheoloai + String.valueOf(page);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, duongdan, new Response.Listener<String>() {
            // dùng phương thức này để lấy dữ liệu về
            @Override
            public void onResponse(String response) {
                // tạo biến hứng giá trị
                int idloai = 0;
                int maMon = 0;
                String tenMon = "";
                String hinhMon = "";
                String motaMon = "";
                float dongiaMon = 0;
                if (response != null && response.length() != 2) {
                    // có dữ liệu trả về thì thanh progressBar tắt đi
                    listViewMonAn.removeFooterView(footerview);
                    try {
                        JSONArray jsonArr = new JSONArray(response);
                        // lấy từng object của Array
                        for (int i = 0; i < jsonArr.length(); i++) {
                            // lấy từng values của từng object
                            JSONObject jsonObject = jsonArr.getJSONObject(i);
                            maMon = jsonObject.getInt("mamon");
                            idloai = jsonObject.getInt("maloai");
                            tenMon = jsonObject.getString("ten");
                            hinhMon = jsonObject.getString("hinh");
                            motaMon = jsonObject.getString("mota");
                            dongiaMon = (float) jsonObject.getDouble("dongia");
                           // CheckConnect.ShowToast(getActivity(),maMon+" "+idloai+" "+" "+tenMon+" "+" "+hinhMon+" "+motaMon+" "+dongiaMon);

                            // thêm dữ liệu vào mảng
                            arrayMonAn.add(new MonAn(maMon, idloai, tenMon, hinhMon, motaMon,dongiaMon));
                            // cập nhật Adapter
                            danhSachMonAnAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    // bắt sự kiện hết dữ liệu
                    limitData = true;
                    listViewMonAn.removeFooterView(footerview);
                    CheckConnect.ShowToast(getActivity(),"Hết dữ liệu");

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            // phương thức dùng để đẩy dữ liệu lên server
            // dưới dạng 1 HashMap
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // HashMap<Key,value>
                HashMap<String, String> param = new HashMap<String, String>();
                // đưa giá trị của idloai lên server
                param.put("idloai", String.valueOf(idloai));
                return param;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void AnhXa() {
//        listViewMonAn = view.findViewById(R.id.listView_fragmentNoti_MonAn);
//        arrayMonAn = new ArrayList<>();
//        danhSachMonAnAdapter = new DanhSachMonAnAdapter(arrayMonAn,getActivity());
//        listViewMonAn.setAdapter(danhSachMonAnAdapter);
        listViewMonAn = (ListView) view.findViewById(R.id.listView_fragmentNoti_MonAn);
        arrayMonAn = new ArrayList<>();
        danhSachMonAnAdapter = new DanhSachMonAnAdapter(arrayMonAn,getActivity());
        listViewMonAn.setAdapter(danhSachMonAnAdapter);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        footerview = layoutInflater.inflate(R.layout.progressbar, null);

        // khởi tạo handler
        mHandler = new mHandler();
    }



    public class mHandler extends Handler{
        // function dùng để quản lý những Theard gửi lên
        @Override
        public void handleMessage(Message msg) {
            // những giá trị gửi lên thông qua msg
            switch (msg.what){
                case 0:
                    // Theard gửi lên biến = 0 thì add progessbar vào lisview
                    listViewMonAn.addFooterView(footerview);
                    break;
                case 1:
                    // Theard gửi lên = 1 thì cập nhật đổ dữ liệu lên
                    // page + 1 trước rồi mới thực hiên function
                    GetData(++page);
                    // trả về trạng thái chưa load dữ liệu vì đã load xong rồi
                    isLoading = false;
                    break;
            }
            super.handleMessage(msg);
        }

    }
    // Các nhân viên là các dạng luồng (Threard)
    public  class ThreadData extends Thread{
        // để cho thực hiện các luồng gọi run
        @Override
        public void run() {
            // gửi tin nhắn = 0  trước
            mHandler.sendEmptyMessage(0);
            try {
                // cho nó ngủ 1s
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // sau 3s gửi tiếp tin nhắn
            // obtainMessage liên kết các Theard và Handler
            // muốn liên kết tiếp tục thì gọi obtainMessage
            // gửi tiếp cho Handler giá trị 1
            Message message = mHandler.obtainMessage(1);
            mHandler.sendMessage(message);
            super.run();
        }
    }
}
