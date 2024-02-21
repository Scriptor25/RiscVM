package io.scriptor.riscvm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MemoryAdapter extends BaseAdapter {

    private final Context mContext;
    private final int mLayout;
    private final RiscVM mVM;

    public MemoryAdapter(Context context, int layout, RiscVM vm) {
        super();
        mContext = context;
        mLayout = layout;
        mVM = vm;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) convertView = LayoutInflater.from(mContext).inflate(mLayout, null);

        final TextView address = convertView.findViewById(R.id.address);
        final var d = new TextView[8];
        d[0] = convertView.findViewById(R.id.d0);
        d[1] = convertView.findViewById(R.id.d1);
        d[2] = convertView.findViewById(R.id.d2);
        d[3] = convertView.findViewById(R.id.d3);
        d[4] = convertView.findViewById(R.id.d4);
        d[5] = convertView.findViewById(R.id.d5);
        d[6] = convertView.findViewById(R.id.d6);
        d[7] = convertView.findViewById(R.id.d7);
        final TextView ascii = convertView.findViewById(R.id.ascii);
        final var inst = new TextView[2];
        inst[0] = convertView.findViewById(R.id.i0);
        inst[1] = convertView.findViewById(R.id.i1);

        address.setText(String.format("%08X", position * 8));
        String asciid = "";
        for (int i = 0; i < d.length; i++) {
            byte b = mVM.getMachine().getMemory().getByte(position * 8 + i);
            d[i].setText(String.format("%02X", b));
            if (mVM.getMachine().getCPU().get(ISA.RegisterAlias.PC) == position * 8 + i)
                d[i].setBackgroundColor(0xffff0000);
            else d[i].setBackgroundColor(0x00000000);
            asciid += b < 0x20 || b >= 0xff ? '.' : (char) b;
        }
        ascii.setText(asciid);

        for (int i = 0; i < inst.length; i++) {
            final var in =
                    Instruction.valueOf(mVM.getMachine().getMemory().getWord(position * 8 + i * 4));
            inst[i].setText(in != null ? in.toString() : String.format("%26s", ""));
            if (mVM.getMachine().getCPU().get(ISA.RegisterAlias.PC) == position * 8 + i * 4)
                inst[i].setBackgroundColor(0xffff0000);
            else inst[i].setBackgroundColor(0x00000000);
        }

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Long getItem(int position) {
        return mVM.getMachine().getMemory().getDWord(position * 8);
    }

    @Override
    public int getCount() {
        return mVM.getMachine().getMemory().getSize() / 8;
    }
}
