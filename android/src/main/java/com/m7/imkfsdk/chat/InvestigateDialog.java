package com.m7.imkfsdk.chat;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.reactlibrary.R;
import com.moor.imkf.IMChatManager;
import com.moor.imkf.SubmitInvestigateListener;
import com.moor.imkf.model.entity.Investigate;

import java.util.ArrayList;
import java.util.List;

/**
 * 评价列表界面
 */
public class InvestigateDialog extends DialogFragment {

    private ListView investigateListView;
    private TextView investigateTitleTextView;

    private List<Investigate> investigates = new ArrayList<Investigate>();

    private InvestigateAdapter adapter;
    private SharedPreferences sp;
    String satisfyTitle;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setTitle("提交评价");
        getDialog().setCanceledOnTouchOutside(false);

        sp = getActivity().getSharedPreferences("moordata", 0);

        // Get the layout inflater
        View view = inflater.inflate(R.layout.kf_dialog_investigate, null);
        investigateListView = (ListView) view.findViewById(R.id.investigate_list);
        investigateTitleTextView = view.findViewById(R.id.investigate_title);

        investigates = IMChatManager.getInstance().getInvestigate();

        adapter = new InvestigateAdapter(getActivity(), investigates);
        satisfyTitle = sp.getString("satisfyTitle", "感谢您使用我们的服务，请为此次服务评价！");
        if (satisfyTitle.equals("")) {
            satisfyTitle = "感谢您使用我们的服务，请为此次服务评价！";
        }
        investigateListView.setAdapter(adapter);
        investigateTitleTextView.setText(satisfyTitle);
        String satifyThank = sp.getString("satisfyThank", "感谢您对此次服务做出评价，祝您生活愉快，再见！");
        if (satifyThank.equals("")) {
            satifyThank = "感谢您对此次服务做出评价，祝您生活愉快，再见！";
        }

        final String finalSatifyThank = satifyThank;

        investigateListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Investigate investigate = (Investigate) parent.getAdapter().getItem(position);
                IMChatManager.getInstance().submitInvestigate(investigate, new SubmitInvestigateListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getActivity(), finalSatifyThank, Toast.LENGTH_SHORT).show();
                        dismiss();
                    }

                    @Override
                    public void onFailed() {
//                        Toast.makeText(getActivity(), "评价提交失败", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                });
            }
        });
        return view;
    }

    @Override
    public void show(android.app.FragmentManager manager, String tag) {
        if (!this.isAdded()) {
            try {
                super.show(manager, tag);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void dismiss() {
        try {
            super.dismiss();
        } catch (Exception e) {
        }

    }

}
