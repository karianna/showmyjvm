package com.microsoft.showmyjvm.core;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

public class ShowJVM {

    public static void main(String args[]) {
        System.out.println(new ShowJVM().getJVMDetails());
    }

    private StringBuilder buffer = new StringBuilder();

    protected StringBuilder append(Object s) {
        return buffer.append(s).append("\n");
    }

    protected StringBuilder append(String msg, Object... s) {
        return buffer.append(String.format(msg, s)).append("\n");
    }

    private String bytesToMBString(long bytes) {
        return Long.toString(bytes / 1024 / 1024) + " MB";
    }

    public String getJVMDetails() {
        buffer = new StringBuilder();

        // Runtime Properties
        append("");
        append("## Runtime Properties");
        var runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        append(runtimeMXBean.getVmName() + " " + runtimeMXBean.getVmVersion());
        append("PID @ Hostname: " + runtimeMXBean.getName());
        runtimeMXBean.getInputArguments().forEach(arg -> append("RuntimeMXBean input: " + arg));

        // Memory Settings
        final int mb = 1024 * 1024;
        Runtime runtime = Runtime.getRuntime();
        append("");
        append("## Memory Settings [MB]");
        append("Used Memory: " + (runtime.totalMemory() - runtime.freeMemory()) / mb + "MB");
        append("Free Memory: " + runtime.freeMemory() / mb + "MB");
        append("Total Memory: " + runtime.totalMemory() / mb + "MB");
        append("Max Memory: " + runtime.maxMemory() / mb + "MB");

        // Loaded Classes
        var classLoading = ManagementFactory.getClassLoadingMXBean();
        append("");
        append("## Loaded Classes");
        append("Total # of loaded classes (from the RuntimeInfo start): " + classLoading.getTotalLoadedClassCount());
        append("Total # of unloaded classes: " + classLoading.getUnloadedClassCount());
        append("Current # of loaded classes: " + classLoading.getLoadedClassCount());

        // Compilation
        var compiler = ManagementFactory.getCompilationMXBean();
        append("");
        append("## Compiler");
        append("Compiler Name: " + compiler.getName());
        append("Total Compilation Time: " + compiler.getTotalCompilationTime());

        // Garbage Collector
        append("");
        append("## Garbage Collectors");
        var gcMxBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (var gcBean : gcMxBeans) {
            append(gcBean.getName() + ": " + gcBean.getObjectName().toString());
        }

        // CPU Usage
        append("");
        append("## CPU");

        var osBean = ManagementFactory.getOperatingSystemMXBean();
        append("osMXBean.getSystemLoadAverage: %s", Double.toString(osBean.getSystemLoadAverage()));
        append("osMXBean.getAvailableProcessors: %s", Integer.toString(osBean.getAvailableProcessors()));

        var _osBean = (com.sun.management.OperatingSystemMXBean) osBean;
        append("osMXBean.getCommittedVirtualMemorySize: %s", bytesToMBString(_osBean.getCommittedVirtualMemorySize()));
        append("osMXBean.getTotalPhysicalMemorySize: %s", bytesToMBString(_osBean.getTotalPhysicalMemorySize()));
        append("osMXBean.getFreePhysicalMemorySize: %s", bytesToMBString(_osBean.getFreePhysicalMemorySize()));
        append("osMXBean.getTotalSwapSpaceSize: %s", bytesToMBString(_osBean.getTotalSwapSpaceSize()));
        append("osMXBean.getFreeSwapSpaceSize: %s", bytesToMBString(_osBean.getFreeSwapSpaceSize()));
        append("osMXBean.getSystemCpuLoad: %s", Double.toString(_osBean.getSystemCpuLoad()));
        append("osMXBean.getProcessCpuLoad: %s", Double.toString(_osBean.getProcessCpuLoad()));
        append("osMXBean.getProcessCpuTime: %s", Double.toString(_osBean.getProcessCpuTime()));

        // Threads
        var threads = ManagementFactory.getThreadMXBean();
        append("");
        append("## Threads");

        append("Threads / Started Threads / Peak: " + threads.getThreadCount() + " / "
                + threads.getTotalStartedThreadCount() + " / " + threads.getPeakThreadCount());
        append("CPU Time / User Time: " + threads.getCurrentThreadCpuTime() + " / "
                + threads.getCurrentThreadUserTime());

        Arrays.stream(threads.dumpAllThreads(true, true))
                .forEach(thread -> append("Thread Name: " + thread.getThreadName()));

        // System Properties
        append("");
        append("## All System Properties:");
        System.getProperties().entrySet().forEach(entry -> jvmprop(entry.getKey(), entry.getValue()));

        // Environment Variables
        append("");
        append("## Environment variables:");
        System.getenv().entrySet().forEach(entry -> jvmprop(entry.getKey(), entry.getValue()));

        return buffer.toString();
    }

    private void jvmprop(Object key, Object value) {
        String v = value == null ? "" : value.toString();
        String line = new StringBuilder().append("\"" + key + "\": \"")
                .append(v.replace("\\", "\\\\")).append("\"").toString();
        append(line);
    }

}
