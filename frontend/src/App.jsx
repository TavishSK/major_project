import { useState } from "react";
import ExperimentForm from "./components/ExperimentForm";

export default function App() {
  const [results, setResults] = useState(null);

  const getLatencyColor = (v) => {
    if (v < 140) return "from-emerald-400 to-green-500";
    if (v < 160) return "from-yellow-400 to-amber-500";
    return "from-rose-400 to-red-500";
  };

  return (
    <div className="relative min-h-screen bg-[#020617] text-white flex items-center justify-center p-6 overflow-hidden">

      {/* 🌌 BACKGROUND GLOW ORBS */}
      <div className="absolute w-[600px] h-[600px] bg-blue-600/20 blur-[140px] top-[-100px] left-[-100px]" />
      <div className="absolute w-[500px] h-[500px] bg-indigo-600/20 blur-[140px] bottom-[-120px] right-[-100px]" />

      <div className="relative w-full max-w-6xl">

        {/*  TITLE */}
        <h1 className="text-6xl font-semibold text-center mb-12 tracking-tight">
          <span className="opacity-70"></span>{" "}
          <span className="bg-gradient-to-r from-white via-blue-200 to-indigo-300 bg-clip-text text-transparent drop-shadow-lg">
            Cloud Strategy Simulator
          </span>
        </h1>

        {/* 💎 MAIN CARD */}
        <div className="
          relative
          bg-white/[0.04]
          backdrop-blur-2xl
          border border-white/10
          rounded-3xl
          shadow-[0_40px_120px_rgba(0,0,0,0.8)]
          p-10
        ">

          {/* subtle inner glow */}
          <div className="absolute inset-0 rounded-3xl border border-white/5 pointer-events-none" />

          {/* BUTTON */}
          <div className="flex justify-center mb-12">
            <ExperimentForm setResults={setResults} />
          </div>

          {/* 📊 TABLE */}
          {results && (
            <div className="overflow-x-auto">

              <table className="w-full border-separate border-spacing-y-3">

                {/* HEADER */}
                <thead>
                  <tr className="text-xs uppercase tracking-widest text-white/40">
                    <th className="px-6 py-2">Strategy</th>
                    <th className="px-6 py-2">Replicas</th>
                    <th className="px-6 py-2">Response Time</th>
                    <th className="px-6 py-2">Scaling</th>
                  </tr>
                </thead>

                {/* BODY */}
                <tbody>
                  {results.results.map((r, i) => {
                    const isBest = r.strategy === results.bestStrategy;

                    return (
                      <tr
                        key={i}
                        className={`
                          group
                          bg-white/[0.03]
                          hover:bg-white/[0.06]
                          transition-all duration-300
                          rounded-xl
                          ${
                            isBest
                              ? "ring-1 ring-emerald-400/40 shadow-[0_0_40px_rgba(16,185,129,0.25)]"
                              : ""
                          }
                        `}
                      >

                        {/* STRATEGY */}
                        <td className="px-6 py-5 font-semibold tracking-wide">
                          <span className="flex items-center gap-2">
                            {r.strategy}
                            {isBest && (
                              <span className="text-emerald-400 text-xs px-2 py-1 border border-emerald-400/40 rounded-full">
                                BEST
                              </span>
                            )}
                          </span>
                        </td>

                        {/* REPLICAS */}
                        <td className="px-6 py-5">
                          <div className="flex items-center gap-3">
                            <span className="w-8">{r.finalReplicas}</span>

                            <div className="flex-1 h-[4px] bg-white/10 rounded-full overflow-hidden">
                              <div
                                className="h-[4px] bg-gradient-to-r from-blue-400 to-indigo-500 rounded-full transition-all duration-700"
                                style={{
                                  width: `${Math.min(r.finalReplicas * 10, 100)}%`,
                                }}
                              />
                            </div>
                          </div>
                        </td>

                        {/* RESPONSE TIME */}
                        <td className="px-6 py-5">
                          <div className="flex items-center gap-3">
                            <span className="w-24">
                              {r.avgResponseTime.toFixed(2)} ms
                            </span>

                            <div className="flex-1 h-[4px] bg-white/10 rounded-full overflow-hidden">
                              <div
                                className={`h-[4px] bg-gradient-to-r ${getLatencyColor(
                                  r.avgResponseTime
                                )} rounded-full transition-all duration-700`}
                                style={{
                                  width: `${Math.min(r.avgResponseTime / 2, 100)}%`,
                                }}
                              />
                            </div>
                          </div>
                        </td>

                        {/* SCALING EVENTS */}
                        <td className="px-6 py-5">
                          <div className="flex items-center gap-3">
                            <span className="w-8">{r.scalingEvents}</span>

                            <div className="flex-1 h-[4px] bg-white/10 rounded-full overflow-hidden">
                              <div
                                className="h-[4px] bg-gradient-to-r from-emerald-400 to-green-500 rounded-full transition-all duration-700"
                                style={{
                                  width: `${Math.min(r.scalingEvents * 12, 100)}%`,
                                }}
                              />
                            </div>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>

              {/* 🏆 BEST STRATEGY FOOTER */}
              <div className="mt-10 flex justify-end">
                <div className="
                  relative
                  px-6 py-3
                  rounded-xl
                  bg-gradient-to-r from-emerald-500/10 to-green-500/10
                  border border-emerald-400/30
                  text-emerald-300
                  font-medium
                  tracking-wide
                  shadow-[0_0_30px_rgba(16,185,129,0.2)]
                ">
                  ✔ Best Strategy: {results.bestStrategy}
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}