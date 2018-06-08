package net.zcarioca.build.benchmark.results;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class BenchmarkResults {
    public final Collection<BenchmarkTestResult> results;
    public final String systemModel;
    public final String operatingSystem;
    public final String cpu;
    public final String architecture;
    public final Integer physicalProcessors;
    public final Integer logicalProcessors;
    public final Long totalMemoryInBytes;
    public final Long swapTotalInBytes;

    private BenchmarkResults() {
        results = null;
        systemModel = null;
        operatingSystem = null;
        cpu = null;
        architecture = null;
        physicalProcessors = null;
        logicalProcessors = null;
        totalMemoryInBytes = null;
        swapTotalInBytes = null;
    }

    public BenchmarkResults(final Collection<BenchmarkTestResult> results) {
        final SystemInfo systemInfo = new SystemInfo();
        systemModel = Optional.of(systemInfo)
                .map(SystemInfo::getHardware)
                .map(HardwareAbstractionLayer::getComputerSystem)
                .map(ComputerSystem::getModel)
                .orElse(null);
        operatingSystem = Optional.of(systemInfo)
                .map(SystemInfo::getOperatingSystem)
                .map(Object::toString)
                .orElse(null);
        cpu = Optional.of(systemInfo)
                .map(SystemInfo::getHardware)
                .map(HardwareAbstractionLayer::getProcessor)
                .map(CentralProcessor::getName)
                .orElse(null);
        architecture = Optional.of(systemInfo)
                .map(SystemInfo::getHardware)
                .map(HardwareAbstractionLayer::getProcessor)
                .map(CentralProcessor::isCpu64bit)
                .map(is64 -> is64 ? "x86_64 (64bit)" : "x86 (32bit)")
                .orElse(null);
        physicalProcessors = Optional.of(systemInfo)
                .map(SystemInfo::getHardware)
                .map(HardwareAbstractionLayer::getProcessor)
                .map(CentralProcessor::getPhysicalProcessorCount)
                .orElse(null);
        logicalProcessors = Optional.of(systemInfo)
                .map(SystemInfo::getHardware)
                .map(HardwareAbstractionLayer::getProcessor)
                .map(CentralProcessor::getLogicalProcessorCount)
                .orElse(null);
        totalMemoryInBytes = Optional.ofNullable(systemInfo)
                .map(SystemInfo::getHardware)
                .map(HardwareAbstractionLayer::getMemory)
                .map(GlobalMemory::getTotal)
                .orElse(null);
        swapTotalInBytes = Optional.ofNullable(systemInfo)
                .map(SystemInfo::getHardware)
                .map(HardwareAbstractionLayer::getMemory)
                .map(GlobalMemory::getSwapTotal)
                .orElse(null);
        this.results = Collections.unmodifiableCollection(results);
    }

    public long size() {
        return results.size();
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
