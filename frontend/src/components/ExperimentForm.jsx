import { useState } from "react";

export default function ExperimentForm({ setResults }) {
  const [loading, setLoading] = useState(false);

  const runExperiment = async () => {
    try {
      setLoading(true);

      const res = await fetch("http://localhost:8080/api/experiment", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(["CPU", "TREND", "LATENCY"]),
      });

      if (!res.ok) {
        throw new Error("Server error");
      }

      const data = await res.json();
      setResults(data);
    } catch (err) {
      console.error(err);
      alert("Backend connection failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <button
      onClick={runExperiment}
      disabled={loading}
      className="
        flex items-center justify-center gap-2
        px-6 py-3
        rounded-lg
        font-medium
        bg-blue-600 hover:bg-blue-700
        text-white
        shadow-md
        transition duration-200
        disabled:opacity-60 disabled:cursor-not-allowed
      "
    >
      {loading ? (
        <>
          <span className="h-4 w-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
          Running...
        </>
      ) : (
        "Run Experiment"
      )}
    </button>
  );
}