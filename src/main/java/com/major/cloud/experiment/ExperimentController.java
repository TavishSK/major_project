package com.major.cloud.experiment;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/experiment")
public class ExperimentController {

    private final ExperimentService experimentService;

    public ExperimentController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @PostMapping
    public Map<String, Object> runExperiment(@RequestBody List<String> strategies) {

        List<ExperimentResult> results = experimentService.runExperiment(strategies);
        String bestStrategy = experimentService.findBestStrategy(results);

        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        response.put("bestStrategy", bestStrategy);

        return response;
    }
}