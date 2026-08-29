package com.major.cloud.service;

import com.major.cloud.model.ScalingEvent;
import com.major.cloud.strategy.ScalingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExperimentService {

    private final StrategyEngine      strategyEngine;
    private final ScalingService      scalingService;
    private final MonitoringService   monitoringService;
    private final AutoHealingEngine   autoHealingEngine;

    private static final double SLA_LATENCY_THRESHOLD_MS = 500.0;
    private static final double COST_PER_REPLICA_PER_HOUR = 0.023; // AWS t3.small

    /** Full experiment — pure simulation, no Docker required */
    public Map<String, Object> runExperimentDetailed(List<String> strategyNames, String ignored) {
        log.info("Starting simulation experiment — strategies={}", strategyNames);

        autoHealingEngine.startExperiment(null);

        // ── Sample real host metrics (10 seconds) ───────────────────────────
        int samples = 10;
        List<MonitoringService.Workload> wave = monitoringService.generateTrafficWave(samples);

        double peakCpu = wave.stream().mapToDouble(w -> w.cpuUsage).max().orElse(20);
        double peakMem = wave.stream().mapToDouble(w -> w.memoryUsage).max().orElse(40);
        double avgCpu  = wave.stream().mapToDouble(w -> w.cpuUsage).average().orElse(20);
        double avgMem  = wave.stream().mapToDouble(w -> w.memoryUsage).average().orElse(40);

        List<Double> cpuTimeline = new ArrayList<>();
        List<Double> memTimeline = new ArrayList<>();
        for (MonitoringService.Workload w : wave) {
            cpuTimeline.add(round1(w.cpuUsage));
            memTimeline.add(round1(w.memoryUsage));
        }

        // ── Run each strategy ───────────────────────────────────────────────
        List<Map<String, Object>> allResults = new ArrayList<>();
        Map<String, Object> bestResult  = null;
        double bestLatency = Double.MAX_VALUE;

        try {
            for (String name : strategyNames) {
                ScalingStrategy strategy = strategyEngine.getStrategy(name);
                if (strategy == null) continue;
                Map<String, Object> entry = simulateStrategy(strategy, wave);
                allResults.add(entry);
                double lat = (double) entry.get("averageResponseTime");
                if (lat < bestLatency) { bestLatency = lat; bestResult = entry; }
            }
        } finally {
            autoHealingEngine.stopExperiment();
        }

        allResults.sort(Comparator.comparingDouble(e -> (double) e.get("averageResponseTime")));

        // ── Winner reasoning ────────────────────────────────────────────────
        String winnerName = bestResult != null ? (String) bestResult.get("strategy") : "NONE";
        String reasoning  = buildReasoning(winnerName, allResults);

        // ── Cost savings vs worst ───────────────────────────────────────────
        double worstCost = allResults.isEmpty() ? 0 :
            ((int) allResults.get(allResults.size()-1).get("finalReplicas")) * COST_PER_REPLICA_PER_HOUR;
        double bestCost  = bestResult == null ? 0 :
            ((int) bestResult.get("finalReplicas")) * COST_PER_REPLICA_PER_HOUR;
        double savingsVsWorst = Math.max(0, (worstCost - bestCost) * 24 * 30); // monthly

        // ── Response ────────────────────────────────────────────────────────
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("strategies",        allResults);
        response.put("bestStrategy",      winnerName);
        response.put("winnerReasoning",   reasoning);
        response.put("savingsVsWorst",    round2(savingsVsWorst));
        response.put("peakCpuUsage",      round1(peakCpu));
        response.put("peakMemUsage",      round1(peakMem));
        response.put("avgCpuUsage",       round1(avgCpu));
        response.put("avgMemUsage",       round1(avgMem));
        response.put("sampleCount",       wave.size());
        response.put("timestamp",         System.currentTimeMillis());
        response.put("cpuTimeline",       cpuTimeline);
        response.put("memTimeline",       memTimeline);
        response.put("dockerMode",        "SIMULATION");
        response.put("slaThresholdMs",    SLA_LATENCY_THRESHOLD_MS);

        if (bestResult != null) {
            response.put("finalReplicas",       bestResult.get("finalReplicas"));
            response.put("averageResponseTime", bestResult.get("averageResponseTime"));
            response.put("scalingEvents",       bestResult.get("scalingEvents"));
        }
        return response;
    }

    private Map<String, Object> simulateStrategy(ScalingStrategy strategy,
                                                  List<MonitoringService.Workload> wave) {
        String stratName = strategy.getStrategyName();
        int replicas = 2;

        double totalLatency = 0;
        List<ScalingEvent> events       = new ArrayList<>();
        List<Integer>      replicaTL    = new ArrayList<>();
        List<Double>       latencyTL    = new ArrayList<>();

        for (MonitoringService.Workload w : wave) {
            double latency = monitoringService.calculateLatency(w.trafficBase, replicas);
            totalLatency += latency;
            replicaTL.add(replicas);
            latencyTL.add(round1(latency));

            Optional<ScalingEvent> ev = scalingService.applyStrategy(
                    strategy, replicas, w.cpuUsage, w.trend, latency);
            if (ev.isPresent()) {
                events.add(ev.get());
                replicas = ev.get().getNewReplicas();
            }
        }

        int n = latencyTL.size();
        List<Double> sorted = new ArrayList<>(latencyTL);
        Collections.sort(sorted);

        double avgLatency = totalLatency / n;
        double p50  = sorted.get(n / 2);
        double p95  = sorted.get(Math.max(0, (int)(n * 0.95) - 1));
        double p99  = sorted.get(Math.max(0, (int)(n * 0.99) - 1));
        double minL = sorted.get(0);
        double maxL = sorted.get(n - 1);

        double avgReplicas = replicaTL.stream().mapToInt(Integer::intValue).average().orElse(2);
        int    maxReplicas = replicaTL.stream().mapToInt(Integer::intValue).max().orElse(2);
        int    minReplicas = replicaTL.stream().mapToInt(Integer::intValue).min().orElse(2);

        double variance = replicaTL.stream()
                .mapToDouble(r -> Math.pow(r - avgReplicas, 2)).average().orElse(0);
        double stabilityScore = round1(Math.max(0, 100 - variance * 15));

        long slaViolations = latencyTL.stream().filter(l -> l > SLA_LATENCY_THRESHOLD_MS).count();
        double slaCompliance = round1(100.0 * (n - slaViolations) / n);

        double avgTraffic   = wave.stream().mapToDouble(w -> w.trafficBase).average().orElse(200);
        double throughput   = round1(avgTraffic / Math.max(1, avgReplicas));
        double cpuEfficiency = round1(Math.min(100, (avgTraffic / (avgReplicas * 150)) * 100));
        double resourceWaste = round1(Math.max(0, 100 - cpuEfficiency));

        int scaleUpCount   = (int) events.stream().filter(e -> e.getNewReplicas() > 2).count();
        int scaleDownCount = (int) events.stream().filter(e -> e.getNewReplicas() < e.getOldReplicas()).count();

        double hourlyCost = round4(replicas * COST_PER_REPLICA_PER_HOUR);
        double monthlyCost = round2(hourlyCost * 24 * 30);

        // Overall score (lower = better): weighted latency + cost + violations
        double score = round1((avgLatency * 0.4) + (hourlyCost * 1000 * 0.3) +
                               (slaViolations * 50 * 0.2) + (variance * 20 * 0.1));

        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("strategy",          stratName);
        entry.put("finalReplicas",     replicas);
        entry.put("averageResponseTime", round1(avgLatency));
        entry.put("p50Latency",        round1(p50));
        entry.put("p95Latency",        round1(p95));
        entry.put("p99Latency",        round1(p99));
        entry.put("minLatency",        round1(minL));
        entry.put("maxLatency",        round1(maxL));
        entry.put("minReplicas",       minReplicas);
        entry.put("maxReplicas",       maxReplicas);
        entry.put("avgReplicas",       round1(avgReplicas));
        entry.put("stabilityScore",    stabilityScore);
        entry.put("slaViolations",     (int) slaViolations);
        entry.put("slaCompliance",     slaCompliance);
        entry.put("throughput",        throughput);
        entry.put("cpuEfficiency",     cpuEfficiency);
        entry.put("resourceWaste",     resourceWaste);
        entry.put("scaleUpCount",      scaleUpCount);
        entry.put("scaleDownCount",    scaleDownCount);
        entry.put("scalingEventCount", events.size());
        entry.put("hourlyCost",        hourlyCost);
        entry.put("monthlyCost",       monthlyCost);
        entry.put("overallScore",      score);
        entry.put("scalingEvents",     events);
        entry.put("replicaTimeline",   replicaTL);
        entry.put("latencyTimeline",   latencyTL);
        return entry;
    }

    private String buildReasoning(String winner, List<Map<String, Object>> results) {
        if (results.isEmpty() || "NONE".equals(winner)) return "No strategies were evaluated.";
        Map<String, Object> w = results.stream()
                .filter(r -> winner.equals(r.get("strategy"))).findFirst().orElse(results.get(0));
        double lat  = (double) w.get("averageResponseTime");
        double sla  = (double) w.get("slaCompliance");
        double eff  = (double) w.get("cpuEfficiency");
        double stab = (double) w.get("stabilityScore");
        return String.format(
            "%s wins with the lowest average latency of %.0f ms, " +
            "%.1f%% SLA compliance, %.1f%% CPU efficiency, and a stability score of %.0f/100. " +
            "It made %d scaling decisions — balancing cost and performance better than the alternatives.",
            winner, lat, sla, eff, stab, (int) w.get("scalingEventCount"));
    }

    private static double round1(double v) { return Math.round(v * 10.0) / 10.0; }
    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
    private static double round4(double v) { return Math.round(v * 10000.0) / 10000.0; }
}
