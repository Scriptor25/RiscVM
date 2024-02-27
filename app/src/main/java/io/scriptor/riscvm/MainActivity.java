package io.scriptor.riscvm;

import static io.scriptor.riscvm.core.Util.kb;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import io.scriptor.riscvm.databinding.ActivityMainBinding;
import io.scriptor.riscvm.vm.RiscVM;
import io.scriptor.riscvm.vm.VMConfig;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;

    private RiscVM mVM;
    private Uri mUri;
    private boolean mRun = false;

    private final ActivityResultLauncher<Intent> mLoadFile =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                mUri = result.getData().getData();
                            }
                        }
                    });

    private void loadFile() {
        final var intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        mLoadFile.launch(intent);
    }

    private void reset() {
        mRun = false;
        mVM.reset();
        updateUI();
    }

    private void assemble() {
        mRun = false;
        if (mUri == null) return;
        final InputStream stream;
        try {
            stream = getContentResolver().openInputStream(mUri);
        } catch (FileNotFoundException e) {
            System.err.println(e);
            return;
        }
        mVM.assemble(new BufferedInputStream(stream));
        try {
            stream.close();
        } catch (IOException e) {
            System.err.println(e);
        }
        updateUI();
    }

    private void updateUI() {
        ((RegistersAdapter) mBinding.registers.getAdapter()).notifyDataSetChanged();
        ((MemoryAdapter) mBinding.memory.getAdapter()).notifyDataSetChanged();
    }

    private void doRun() {
        while (mRun && mVM.step())
            ;
        runOnUiThread(this::updateUI);
    }

    private void run() {
        mRun = true;
        new Thread(this::doRun).start();
    }

    private void pause() {
        mRun = false;
    }

    private void step() {
        mRun = false;
        mVM.step();
        updateUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        final var config = new VMConfig(kb(16), "text", "rodata", "data", "bss");
        mVM = new RiscVM(config);

        mBinding.loadFile.setOnClickListener(v -> loadFile());
        mBinding.reset.setOnClickListener(v -> reset());
        mBinding.assemble.setOnClickListener(v -> assemble());
        mBinding.run.setOnClickListener(v -> run());
        mBinding.pause.setOnClickListener(v -> pause());
        mBinding.step.setOnClickListener(v -> step());

        mBinding.registers.setAdapter(new RegistersAdapter(this, R.layout.view_register, mVM));
        mBinding.memory.setAdapter(new MemoryAdapter(this, R.layout.view_memory, mVM));

        final var stream =
                new ByteArrayOutputStream() {

                    @Override
                    public void flush() throws IOException {
                        super.flush();
                        runOnUiThread(() -> mBinding.output.setText(toString()));
                        runOnUiThread(() -> mBinding.outputscroll.fullScroll(View.FOCUS_DOWN));
                    }
                };
        final var print = new PrintStream(stream, true);
        System.setOut(print);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
    }
}
