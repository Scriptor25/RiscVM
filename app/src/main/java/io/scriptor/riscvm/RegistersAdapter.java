package io.scriptor.riscvm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RegistersAdapter extends BaseAdapter {

    private final Context mContext;
    private final int mLayout;
    private final RiscVM mVM;

    public RegistersAdapter(Context context, int layout, RiscVM vm) {
        super();
        mContext = context;
        mLayout = layout;
        mVM = vm;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) convertView = LayoutInflater.from(mContext).inflate(mLayout, null);

        final TextView id = convertView.findViewById(R.id.id);
        final TextView name = convertView.findViewById(R.id.name);
        final TextView hex = convertView.findViewById(R.id.hex);
        final TextView integer = convertView.findViewById(R.id.integer);
        final TextView ascii = convertView.findViewById(R.id.ascii);

        id.setText(String.format("%02X", position));
        name.setText(String.format("%S", ISA.RegisterAlias.values()[position]));

        final int item = getItem(position);
        hex.setText(String.format("%08X", item));
        integer.setText(String.format("%d", item));
        ascii.setText(String.format("%c", item <= 0x20 || item >= 0xff ? '◇' : item));

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Integer getItem(int position) {
        return mVM.getMachine().getCPU().get(position);
    }

    @Override
    public int getCount() {
        return 33;
    }
}
