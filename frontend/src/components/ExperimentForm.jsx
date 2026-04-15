export default function ExperimentForm({ setResults }) {
  const runExperiment = async () => {
    const res = await fetch("http://localhost:8080/api/experiment", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(["CPU", "TREND", "LATENCY"]),
    });

    const data = await res.json();
    setResults(data);
  };

  return (
    <button className="btn" onClick={runExperiment}>
      Run Experiment
    </button>
  );
}