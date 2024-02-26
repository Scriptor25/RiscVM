package io.scriptor.riscvm.app;

import imgui.ImGui;
import imgui.app.Application;
import imgui.app.Configuration;
import imgui.flag.ImGuiConfigFlags;
import io.scriptor.riscvm.vm.RiscVM;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import static io.scriptor.riscvm.core.Util.handle;
import static io.scriptor.riscvm.core.Util.handleT;
import static org.lwjgl.glfw.GLFW.*;

public class RiscApp extends Application {

    private final RiscVM mVM;
    private boolean mRun = false;
    private boolean mReset = false;

    private final MainMenuBar mMainMenuBar = new MainMenuBar();
    private final FileBrowser mFileBrowser = new FileBrowser();
    private final MemoryView mMemoryView;
    private final CPUView mCPUView;

    public RiscApp(RiscVM vm) {
        super();
        mVM = vm;
        mMemoryView = new MemoryView(vm.getMachine().getMemory().getBuffer());
        mCPUView = new CPUView(vm.getMachine().getCPU().getRegisters());
    }

    @Override
    protected void configure(Configuration config) {
        config.setWidth(1200);
        config.setHeight(800);
        config.setTitle("RiscVM");

        mMainMenuBar.addMenu("File",
                        new MenuItem("Load File", this::menuLoadFile, new Shortcut(true, false, false, false, GLFW_KEY_L)),
                        new MenuItem("Exit", this::menuExit, new Shortcut(false, false, false, false, GLFW_KEY_ESCAPE)))
                .addMenu("Edit",
                        new MenuItem("Reset", this::menuReset, new Shortcut(true, false, false, false, GLFW_KEY_Q)))
                .addMenu("Build",
                        new MenuItem("Assemble", this::menuAssemble, new Shortcut(true, false, false, false, GLFW_KEY_S)))
                .addMenu("Run",
                        new MenuItem("Run", this::menuRun, new Shortcut(true, false, false, false, GLFW_KEY_R)),
                        new MenuItem("Pause", this::menuPause, new Shortcut(true, false, false, false, GLFW_KEY_W)),
                        new MenuItem("Step", this::menuStep, new Shortcut(true, false, false, false, GLFW_KEY_E)));
    }

    @Override
    protected void initImGui(Configuration config) {
        super.initImGui(config);

        ImGui.getIO().addConfigFlags(ImGuiConfigFlags.DockingEnable | ImGuiConfigFlags.ViewportsEnable);
        final var callback = glfwSetKeyCallback(handle, mMainMenuBar::onKeyAction);
        if (callback != null)
            callback.close();
    }

    @Override
    public void process() {
        ImGui.dockSpaceOverViewport();

        mMainMenuBar.show();
        mFileBrowser.show();
        mMemoryView.show(mVM.getMachine().getCPU().programCounter());
        mCPUView.show();

        if (mRun && !mVM.step()) mRun = false;
        if (mReset) {
            mRun = false;
            mReset = false;
            mVM.reset();
        }
    }

    private void menuLoadFile() {
        mFileBrowser.open();
    }

    private void menuExit() {
        menuPause();
        glfwSetWindowShouldClose(getHandle(), true);
    }

    private void menuReset() {
        menuPause();
        mReset = true;
    }

    private void menuAssemble() {
        menuPause();
        final var file = mFileBrowser.getFile();
        if (file == null)
            return;
        handle(() -> handleT(() -> new BufferedInputStream(new FileInputStream(file))).ifPresent(mVM::assemble));
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
}
