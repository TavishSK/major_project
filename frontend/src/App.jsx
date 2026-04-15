import { useState } from "react";
import ExperimentForm from "./components/ExperimentForm";
import ResultTable from "./components/ResultTable";
import "./App.css";

function App() {
  const [results, setResults] = useState(null);

  return (
    <div className="app-container">
      <h1 className="title">🚀 Cloud Strategy Simulator</h1>

      <ExperimentForm setResults={setResults} />
      <ResultTable data={results} />
    </div>
  );
}

export default App;