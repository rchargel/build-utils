package net.zcarioca.maven.benchmark.results;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

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

    @SuppressWarnings("unused")
    private BenchmarkResults() {
        this.results = null;
        this.systemModel = null;
        this.operatingSystem = null;
        this.cpu = null;
        this.architecture = null;
        this.physicalProcessors = null;
        this.logicalProcessors = null;
        this.totalMemoryInBytes = null;
        this.swapTotalInBytes = null;
    }

    public BenchmarkResults(final Collection<BenchmarkTestResult> results) {
        final SystemInfo systemInfo = new SystemInfo();
        this.systemModel = Optional.of(systemInfo)
                .map(SystemInfo::getHardware)
                .map(HardwareAbstractionLayer::getComputerSystem)
                .map(ComputerSystem::getModel)
                .orElse(null);
        this.operatingSystem = Optional.of(systemInfo)
                .map(SystemInfo::getOperatingSystem)
                .map(Object::toString)
                .orElse(null);
        this.cpu = Optional.of(systemInfo)
                .map(SystemInfo::getHardware)
                .map(HardwareAbstractionLayer::getProcessor)
                .map(CentralProcessor::getName)
                .orElse(null);
        this.architecture = Optional.of(systemInfo)
                .map(SystemInfo::getHardware)
                .map(HardwareAbstractionLayer::getProcessor)
                .map(CentralProcessor::isCpu64bit)
                .map(is64 -> is64 ? "x86_64 (64bit)" : "x86 (32bit)")
                .orElse(null);
        this.physicalProcessors = Optional.of(systemInfo)
                .map(SystemInfo::getHardware)
                .map(HardwareAbstractionLayer::getProcessor)
                .map(CentralProcessor::getPhysicalProcessorCount)
                .orElse(null);
        this.logicalProcessors = Optional.of(systemInfo)
                .map(SystemInfo::getHardware)
                .map(HardwareAbstractionLayer::getProcessor)
                .map(CentralProcessor::getLogicalProcessorCount)
                .orElse(null);
        this.totalMemoryInBytes = Optional.ofNullable(systemInfo)
                .map(SystemInfo::getHardware)
                .map(HardwareAbstractionLayer::getMemory)
                .map(GlobalMemory::getTotal)
                .orElse(null);
        this.swapTotalInBytes = Optional.ofNullable(systemInfo)
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
