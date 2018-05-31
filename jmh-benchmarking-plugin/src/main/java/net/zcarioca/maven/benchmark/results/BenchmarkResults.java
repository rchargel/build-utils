package net.zcarioca.maven.benchmark.results;

import java.util.Optional;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

public class BenchmarkResults {
    private final SystemInfo systemInfo;

    public BenchmarkResults() {
        this.systemInfo = new SystemInfo();
    }

    public Optional<String> getSystemModel() {
        return Optional.of(systemInfo)
                .map(SystemInfo::getHardware)
                .map(HardwareAbstractionLayer::getComputerSystem)
                .map(ComputerSystem::getModel);
    }

    public Optional<String> getOS() {
        return Optional.ofNullable(systemInfo)
                .map(SystemInfo::getOperatingSystem)
                .map(Object::toString);
    }

    public Optional<String> getCPU() {
        return Optional.ofNullable(systemInfo)
                .map(SystemInfo::getHardware)
                .map(HardwareAbstractionLayer::getProcessor)
                .map(CentralProcessor::getName);
    }

    public Optional<String> getCPUArchitecture() {
        return Optional.ofNullable(systemInfo)
                .map(SystemInfo::getHardware)
                .map(HardwareAbstractionLayer::getProcessor)
                .map(CentralProcessor::isCpu64bit)
                .map(is64 -> is64 ? "x86_64 (64bit)" : "x86 (32bit)");
    }

    public Optional<Integer> getNumberOfPhysicalProcessors() {
        return Optional.ofNullable(systemInfo)
                .map(SystemInfo::getHardware)
                .map(HardwareAbstractionLayer::getProcessor)
                .map(CentralProcessor::getPhysicalProcessorCount);
    }

    public Optional<Integer> getNumberOfLogicalProcessors() {
        return Optional.ofNullable(systemInfo)
                .map(SystemInfo::getHardware)
                .map(HardwareAbstractionLayer::getProcessor)
                .map(CentralProcessor::getLogicalProcessorCount);
    }

    public Optional<Long> getTotalMemoryInBytes() {
        return Optional.ofNullable(systemInfo)
                .map(SystemInfo::getHardware)
                .map(HardwareAbstractionLayer::getMemory)
                .map(GlobalMemory::getTotal);
    }

    public Optional<Long> getSwapTotalInBytes() {
        return Optional.ofNullable(systemInfo)
                .map(SystemInfo::getHardware)
                .map(HardwareAbstractionLayer::getMemory)
                .map(GlobalMemory::getSwapTotal);
    }
}
