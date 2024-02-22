package io.scriptor.riscvm.app;

import imgui.ImGui;
import imgui.app.Application;
import imgui.app.Configuration;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.type.ImString;
import io.scriptor.riscvm.core.ISA;
import io.scriptor.riscvm.core.Instruction;
import io.scriptor.riscvm.vm.RiscVM;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

import static io.scriptor.riscvm.core.Util.handleT;
import static org.lwjgl.glfw.GLFW.*;

public class RiscApp extends Application {

    private static final String[] MEMORY_FORMATS = {"RAW", "INSTRUCTIONS"};
    private int mSelectedMemoryFormat = 0;

    private final RiscVM mVM;
    private File mFile;
    private final ImString mFileName = new ImString();
    private final Queue<Runnable> mTasks = new ArrayDeque<>();
    private boolean mRun = false;

    public RiscApp(RiscVM vm) {
        super();
        mVM = vm;
    }

    @Override
    protected void configure(Configuration config) {
        config.setWidth(1200);
        config.setHeight(800);
        config.setTitle("RiscVM");
    }

    @Override
    protected void initImGui(Configuration config) {
        super.initImGui(config);

        ImGui.getIO().addConfigFlags(ImGuiConfigFlags.DockingEnable | ImGuiConfigFlags.ViewportsEnable);
        glfwSetKeyCallback(handle, this::onKeyAction);
    }

    @Override
    public void process() {
        while (!mTasks.isEmpty())
            mTasks.poll().run();

        showMenuBar();
        ImGui.dockSpaceOverViewport();
        showLoadFilePopup();
        showMachine();

        if (mRun && !mVM.step()) mRun = false;
    }

    private void onKeyAction(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_RELEASE) {
            if (key == GLFW_KEY_ESCAPE) menuExit();
            if ((mods & GLFW_MOD_ALT) != 0) {
                if (key == GLFW_KEY_R) menuRun();
                if (key == GLFW_KEY_E) menuStep();
                if (key == GLFW_KEY_W) menuPause();
                if (key == GLFW_KEY_Q) menuReset();
                if (key == GLFW_KEY_L) menuLoadFile();
                if (key == GLFW_KEY_S) menuAssemble();
            }
        }
    }

    private void schedule(Runnable r) {
        mTasks.add(r);
    }

    private void menuLoadFile() {
        schedule(() -> ImGui.openPopup("load_file"));
    }

    private void menuExit() {
        menuPause();
        glfwSetWindowShouldClose(getHandle(), true);
    }

    private void menuReset() {
        menuPause();
        schedule(mVM::reset);
    }

    private void menuAssemble() {
        menuPause();
        mVM.assemble(handleT(() -> new BufferedInputStream(new FileInputStream(mFile))));
    }

    private void menuRun() {
        mRun = true;
    }

    private void menuPause() {
        mRun = false;
    }

    private void menuStep() {
        menuPause();
        mVM.step();
    }

    private void showMenuBar() {
        boolean file_load_file = false,
                file_exit = false,
                edit_reset = false,
                build_assemble = false,
                run_run = false,
                run_pause = false,
                run_step = false;

        if (ImGui.beginMainMenuBar()) {

            if (ImGui.beginMenu("File")) {
                file_load_file = ImGui.menuItem("Load File", "alt+L");
                file_exit = ImGui.menuItem("Exit", "Esc");
                ImGui.endMenu();
            }

            if (ImGui.beginMenu("Edit")) {
                edit_reset = ImGui.menuItem("Reset", "alt+Q");
                ImGui.endMenu();
            }

            if (ImGui.beginMenu("Build")) {
                build_assemble = ImGui.menuItem("Assemble", "alt+S");
                ImGui.endMenu();
            }

            if (ImGui.beginMenu("Run")) {
                run_run = ImGui.menuItem("Run", "alt+R");
                run_pause = ImGui.menuItem("Pause", "alt+W");
                run_step = ImGui.menuItem("Step", "alt+E");
                ImGui.endMenu();
            }

            ImGui.endMainMenuBar();
        }

        if (file_exit) menuExit();
        if (file_load_file) menuLoadFile();
        if (edit_reset) menuReset();
        if (build_assemble) menuAssemble();
        if (run_run) menuRun();
        if (run_pause) menuPause();
        if (run_step) menuStep();
    }

    private void showLoadFilePopup() {
        if (ImGui.beginPopup("load_file")) {
            ImGui.inputTextWithHint("##filename", "Enter filepath", mFileName);

            if (ImGui.button("Load")) {
                mFile = new File(mFileName.get());
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }
    }

    private void showMachine() {
        showMemory();
        showCPU();
    }

    private void showMemory() {
        if (ImGui.begin("Memory")) {
            if (ImGui.beginCombo("##memory_format", MEMORY_FORMATS[mSelectedMemoryFormat])) {
                for (int i = 0; i < MEMORY_FORMATS.length; i++) {
                    boolean isSelected = mSelectedMemoryFormat == i;
                    if (ImGui.selectable(MEMORY_FORMATS[i], isSelected))
                        mSelectedMemoryFormat = i;
                    if (isSelected)
                        ImGui.setItemDefaultFocus();
                }
                ImGui.endCombo();
            }

            final var buffer = mVM.getMachine().getMemory().getBuffer();
            final var pc = mVM.getMachine().getCPU().get(ISA.RegisterAlias.PC);
            final var N = 16;

            switch (mSelectedMemoryFormat) {
                case 0 -> showMemoryRaw(buffer, pc, N);
                case 1 -> showMemoryInstructions(buffer, pc, N);
            }
        }
        ImGui.end();
    }

    private void showMemoryRaw(ByteBuffer buffer, int pc, int N) {
        if (ImGui.beginTable("Memory", N + 3, ImGuiTableFlags.SizingFixedFit | ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollX | ImGuiTableFlags.ScrollY)) {
            for (int i = 0; i < ImGui.tableGetColumnCount() - 1; i++)
                ImGui.tableSetupColumn(i == 0 ? "Address" : i == N + 1 ? "ASCII" : String.format("%01X", i - 1));
            ImGui.tableHeadersRow();

            buffer.position(0);
            for (int i = 0; buffer.hasRemaining(); i += N) {
                if (ImGui.tableNextColumn()) {
                    final var text = String.format("%08X:", i);
                    if (i / N == pc / N) ImGui.textColored(0xff0000ff, text);
                    else ImGui.text(text);
                }

                for (int j = 0; j < N; j++) {
                    final var b = buffer.get();
                    final var text = String.format("%02X", b);
                    if (ImGui.tableNextColumn()) {
                        if ((i + j) >= pc && (i + j) <= pc + 3) ImGui.textColored(0xff0000ff, text);
                        else if (b == 0) ImGui.textColored(0x55ffffff, text);
                        else ImGui.text(text);
                    }
                }

                if (ImGui.tableNextColumn()) {
                    buffer.position(buffer.position() - N);
                    final var builder = new StringBuilder();
                    for (int j = 0; j < N; j++) {
                        final var c = buffer.get();
                        builder.append(String.format("%c", 0x20 <= c && c <= 0x7E ? c : '.'));
                    }

                    ImGui.text(builder.toString());
                }

                ImGui.tableNextColumn();
            }
            ImGui.endTable();
        }
    }

    private void showMemoryInstructions(ByteBuffer buffer, int pc, int N) {
        final var n = N / 4;
        if (ImGui.beginTable("Memory", n + 2, ImGuiTableFlags.SizingFixedFit | ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollX | ImGuiTableFlags.ScrollY)) {
            for (int i = 0; i < ImGui.tableGetColumnCount() - 1; i++)
                ImGui.tableSetupColumn(i == 0 ? "Address" : String.format("%01X", (i - 1) * 4));
            ImGui.tableHeadersRow();

            buffer.position(0);
            for (int i = 0; buffer.hasRemaining(); i += N) {
                if (ImGui.tableNextColumn()) {
                    final var text = String.format("%08X:", i);
                    if (i / N == pc / N) ImGui.textColored(0xff0000ff, text);
                    else ImGui.text(text);
                }

                for (int j = 0; j < N; j += 4) {
                    final var c = buffer.getInt();
                    if (ImGui.tableNextColumn()) {
                        final var inst = Instruction.valueOf(c);
                        final var text = String.format("%26s", inst == null ? "" : inst);
                        if (i + j == pc) ImGui.textColored(0xff0000ff, text);
                        else ImGui.text(text);
                    }
                }

                ImGui.tableNextColumn();
            }
            ImGui.endTable();
        }
    }

    private void showCPU() {
        if (ImGui.begin("CPU")) {
            final var registers = mVM.getMachine().getCPU().getRegisters();

            if (ImGui.beginTable("Registers", 5, ImGuiTableFlags.SizingFixedFit | ImGuiTableFlags.Borders | ImGuiTableFlags.ScrollX | ImGuiTableFlags.ScrollY)) {
                ImGui.tableSetupColumn("Register");
                ImGui.tableSetupColumn("Hexadecimal");
                ImGui.tableSetupColumn("Decimal");
                ImGui.tableSetupColumn("ASCII");
                ImGui.tableHeadersRow();

                for (int i = 0; i < registers.length; i++) {
                    ImGui.tableNextColumn();
                    ImGui.text(String.format("%3s:", ISA.RegisterAlias.values()[i + 1]));

                    ImGui.tableNextColumn();
                    ImGui.text(String.format("%08X", registers[i]));

                    ImGui.tableNextColumn();
                    ImGui.text(String.format("%d", registers[i]));

                    ImGui.tableNextColumn();
                    ImGui.text(String.format("%c", registers[i]));

                    ImGui.tableNextColumn();
                }
                ImGui.endTable();
            }
        }
        ImGui.end();
    }
}
