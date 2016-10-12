package refresh.ttyy.com.simplerefreshlayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    ListView lv ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_listview);

        lv = (ListView) findViewById(R.id.lv);
        lv.setAdapter(new LVAdapter());
    }

    class LVAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null){
                TextView tv = new TextView(parent.getContext());
                tv.setText(position+"");
                tv.setGravity(Gravity.CENTER);
                tv.setPadding(200,200,200,200);
                convertView = tv;
            }

            return convertView;
        }
    }
}
